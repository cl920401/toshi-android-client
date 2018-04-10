/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.manager;


import android.widget.Toast;

import com.toshi.R;
import com.toshi.crypto.HDWallet;
import com.toshi.crypto.signal.SignalPreferences;
import com.toshi.manager.store.DbMigration;
import com.toshi.util.ImageUtil;
import com.toshi.util.logging.LogUtil;
import com.toshi.util.sharedPrefs.AppPrefs;
import com.toshi.view.BaseApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Completable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class ToshiManager {

    public static final long CACHE_TIMEOUT = 1000 * 60 * 5;

    private final BehaviorSubject<HDWallet> walletSubject = BehaviorSubject.create();

    private BalanceManager balanceManager;
    private HDWallet wallet;
    private SofaMessageManager sofaMessageManager;
    private TransactionManager transactionManager;
    private UserManager userManager;
    private RecipientManager recipientManager;
    private ReputationManager reputationManager;
    private DappManager dappManager;
    private ExecutorService singleExecutor;
    private boolean areManagersInitialised = false;
    private RealmConfiguration realmConfig;

    public ToshiManager() {
        this.singleExecutor = Executors.newSingleThreadExecutor();
        this.balanceManager = new BalanceManager();
        this.userManager = new UserManager();
        this.reputationManager = new ReputationManager();
        this.sofaMessageManager = new SofaMessageManager();
        this.transactionManager = new TransactionManager();
        this.recipientManager = new RecipientManager();
        this.dappManager = new DappManager();
        this.walletSubject.onNext(null);

        tryInit()
                .subscribe(
                        () -> {},
                        ex -> LogUtil.i("Early init failed."));
    }

    // Ignores any data that may be stored on disk and always creates a new wallet.
    public Completable initNewWallet() {
        if (this.wallet != null && this.areManagersInitialised) {
            return Completable.complete();
        }

        return new HDWallet()
                .createWallet()
                .doOnSuccess(this::setWallet)
                .doOnSuccess(__ -> AppPrefs.INSTANCE.setHasOnboarded(false))
                .flatMapCompletable(__ -> initManagers())
                .doOnError(__ -> signOut())
                .doOnError(throwable -> LogUtil.exception("Error while initiating new wallet", throwable))
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    public Completable init(final HDWallet wallet) {
        this.setWallet(wallet);
        return initManagers()
                .doOnError(__ -> signOut())
                .doOnError(throwable -> LogUtil.exception("Error while initiating wallet", throwable))
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    public Completable tryInit() {
        if (this.wallet != null && this.areManagersInitialised) {
            return Completable.complete();
        }
        return new HDWallet()
                .getExistingWallet()
                .doOnSuccess(this::setWallet)
                .doOnError(__ -> clearUserSession())
                .flatMapCompletable(__ -> initManagers())
                .doOnError(throwable -> LogUtil.exception("Error while trying to init wallet", throwable))
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    private void setWallet(final HDWallet wallet) {
        this.wallet = wallet;
        this.walletSubject.onNext(wallet);
    }

    private Completable initManagers() {
        if (this.areManagersInitialised) return Completable.complete();
        return Completable.fromAction(() -> {
            initRealm();
            this.transactionManager.init(this.wallet);
            this.dappManager.init(this.wallet);
            this.reputationManager = new ReputationManager();
        })
        .onErrorComplete()
        .andThen(Completable.mergeDelayError(
                this.balanceManager.init(this.wallet),
                this.sofaMessageManager.init(this.wallet),
                this.userManager.init(this.wallet)
        ))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::handleInitManagersError)
        .doOnCompleted(() -> this.areManagersInitialised = true);
    }

    private void handleInitManagersError(final Throwable throwable) {
        LogUtil.exception("Error while initiating managers " + throwable);
        Toast.makeText(
                BaseApplication.get(),
                R.string.init_manager_error,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void initRealm() {
        if (this.realmConfig != null) return;

        final byte[] key = this.wallet.generateDatabaseEncryptionKey();
        Realm.init(BaseApplication.get());
        this.realmConfig = new RealmConfiguration
                .Builder()
                .schemaVersion(21)
                .migration(new DbMigration(this.wallet))
                .name(this.wallet.getOwnerAddress())
                .encryptionKey(key)
                .build();
        Realm.setDefaultConfiguration(this.realmConfig);
    }

    public final Single<Realm> getRealm() {
        return Single.fromCallable(() -> {
            while (this.realmConfig == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return Realm.getDefaultInstance();
        });

    }

    public final SofaMessageManager getSofaMessageManager() {
        return this.sofaMessageManager;
    }

    public final TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public final UserManager getUserManager() {
        return this.userManager;
    }

    public final RecipientManager getRecipientManager() {
        return this.recipientManager;
    }

    public final BalanceManager getBalanceManager() {
        return this.balanceManager;
    }

    public final ReputationManager getReputationManager() {
        return this.reputationManager;
    }

    public final DappManager getDappManager() {
        return this.dappManager;
    }

    public Single<HDWallet> getWallet() {
        return
                this.walletSubject
                .filter(wallet -> wallet != null)
                .doOnError(t -> LogUtil.exception("Wallet is null", t))
                .onErrorReturn(__ -> null)
                .first()
                .toSingle();
    }

    public void signOut() {
        clearWalletAndSignal();
        clearMessageSession();
        clearUserSession();
        setSignedOutAndClearUserPrefs();
    }

    private void clearWalletAndSignal() {
        this.wallet.clear();
        SignalPreferences.clear();
    }

    private void clearMessageSession() {
        this.sofaMessageManager.deleteSession();
    }

    private void clearUserSession() {
        this.sofaMessageManager.clear();
        this.userManager.clear();
        this.recipientManager.clear();
        this.balanceManager.clear();
        this.transactionManager.clear();
        this.areManagersInitialised = false;
        closeDatabase();
        ImageUtil.clear();
        setWallet(null);
    }

    private void closeDatabase() {
        this.realmConfig = null;
        Realm.removeDefaultConfiguration();
    }

    private void setSignedOutAndClearUserPrefs() {
        AppPrefs.INSTANCE.setSignedOut();
        AppPrefs.INSTANCE.clear();
    }
}
