package com.tokenbrowser.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.AboutActivity;
import com.tokenbrowser.view.activity.BackupPhraseInfoActivity;
import com.tokenbrowser.view.activity.ProfileActivity;
import com.tokenbrowser.view.activity.TrustedFriendsActivity;
import com.tokenbrowser.view.adapter.SettingsAdapter;
import com.tokenbrowser.view.custom.RecyclerViewDivider;
import com.tokenbrowser.view.fragment.children.SettingsFragment;
import com.bumptech.glide.Glide;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class SettingsPresenter implements
        Presenter<SettingsFragment> {

    private User localUser;
    private SettingsFragment fragment;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final SettingsFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        fetchUser();
        initRecyclerView();
        updateUi();
        setSecurityState();
        initClickListeners();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void fetchUser() {
        final Subscription sub = BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded);

        this.subscriptions.add(sub);
    }

    private void handleUserLoaded(final User user) {
        this.localUser = user;
        updateUi();
    }

    private void initRecyclerView() {
        final SettingsAdapter adapter = new SettingsAdapter();
        adapter.setOnItemClickListener(this::handleItemClickListener);
        final RecyclerView recyclerView = this.fragment.getBinding().settings;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerViewDivider(this.fragment.getContext(), 0));
    }

    private void handleItemClickListener(final String option) {
        switch (option) {
            case "About": {
                final Intent intent = new Intent(this.fragment.getContext(), AboutActivity.class);
                this.fragment.getContext().startActivity(intent);
                break;
            }
            default: {
                Toast.makeText(this.fragment.getContext(), "This option is not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUi() {
        if (this.localUser == null || this.fragment == null) {
            return;
        }

        this.fragment.getBinding().name.setText(this.localUser.getDisplayName());
        this.fragment.getBinding().username.setText(this.localUser.getUsername());
        final double reputationScore = this.localUser.getReputationScore() != null
                ? this.localUser.getReputationScore()
                : 0;
        this.fragment.getBinding().ratingView.setStars(reputationScore);

        Glide.with(this.fragment.getBinding().avatar.getContext())
                .load(this.localUser.getAvatar())
                .into(this.fragment.getBinding().avatar);
    }

    private void setSecurityState() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.BACKUP_PHRASE_STATE, Context.MODE_PRIVATE);
        final boolean isBackedUp = prefs.getBoolean(BackupPhraseVerifyPresenter.BACKUP_PHRASE_STATE, false);

        if (isBackedUp) {
            this.fragment.getBinding().checkboxBackupPhrase.setChecked(true);
            this.fragment.getBinding().securityStatus.setVisibility(View.GONE);
        }
    }

    private void initClickListeners() {
        this.fragment.getBinding().myProfileCard.setOnClickListener(this.handleMyProfileClicked);
        this.fragment.getBinding().trustedFriends.setOnClickListener(this::handleTrustedFriendsClicked);
        this.fragment.getBinding().backupPhrase.setOnClickListener(this::handleBackupPhraseClicked);
    }

    private final OnSingleClickListener handleMyProfileClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(fragment.getActivity(), ProfileActivity.class);
            fragment.startActivity(intent);
        }
    };

    private void handleTrustedFriendsClicked(final View view) {
        final Intent intent = new Intent(this.fragment.getContext(), TrustedFriendsActivity.class);
        this.fragment.getContext().startActivity(intent);
    }

    private void handleBackupPhraseClicked(final View view) {
        final Intent intent = new Intent(this.fragment.getContext(), BackupPhraseInfoActivity.class);
        this.fragment.getContext().startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {}
}