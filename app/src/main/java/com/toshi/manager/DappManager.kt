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

package com.toshi.manager

import android.content.Context
import com.toshi.crypto.HDWallet
import com.toshi.manager.network.DirectoryInterface
import com.toshi.manager.network.DirectoryService
import com.toshi.model.network.dapp.DappResult
import com.toshi.model.network.dapp.DappSearchResult
import com.toshi.model.network.dapp.DappSections
import com.toshi.util.dappUtil.CoinbaseDappVerifier
import com.toshi.view.BaseApplication
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DappManager(
        private val directoryService: DirectoryInterface = DirectoryService.get(),
        context: Context = BaseApplication.get(),
        private val coinbaseDappVerifier: CoinbaseDappVerifier = CoinbaseDappVerifier(context),
        private val scheduler: Scheduler = Schedulers.io()
) {

    private var wallet: HDWallet? = null

    fun init(wallet: HDWallet) {
        this.wallet = wallet
    }

    fun getFrontPageDapps(): Single<DappSections> {
        return directoryService
                .getFrontpageDapps()
                .flatMap { verifyCoinbaseDapp(it) }
                .subscribeOn(scheduler)
    }

    private fun verifyCoinbaseDapp(dappSections: DappSections): Single<DappSections> {
        return getWallet()
                .map {
                    coinbaseDappVerifier.updateUrlIfValid(dappSections, it.paymentAddress)
                    dappSections
                }
    }

    fun search(input: String): Single<DappSearchResult> {
        return directoryService
                .search(input)
                .flatMap { verifyCoinbaseDapp(it) }
                .subscribeOn(scheduler)
    }

    fun getAllDapps(): Single<DappSearchResult> {
        return directoryService
                .getAllDapps()
                .flatMap { verifyCoinbaseDapp(it) }
                .subscribeOn(scheduler)
    }

    fun getAllDappsWithOffset(offset: Int): Single<DappSearchResult> {
        return directoryService
                .getAllDappsWithOffset(offset, 20)
                .flatMap { verifyCoinbaseDapp(it) }
                .subscribeOn(scheduler)
    }

    fun getAllDappsInCategoryWithOffset(categoryId: Int, offset: Int): Single<DappSearchResult> {
        return directoryService
                .getAllDappsInCategory(categoryId, offset, 20)
                .flatMap { verifyCoinbaseDapp(it) }
                .subscribeOn(scheduler)
    }

    private fun verifyCoinbaseDapp(searchResult: DappSearchResult): Single<DappSearchResult> {
        return getWallet()
                .map {
                    coinbaseDappVerifier.updateUrlIfValid(searchResult, it.paymentAddress)
                    searchResult
                }
    }

    fun getDapp(dappId: Long): Single<DappResult> {
        return directoryService
                .getDapp(dappId)
                .subscribeOn(scheduler)
    }

    private fun getWallet(): Single<HDWallet> {
        return Single.fromCallable {
            while (wallet == null) Thread.sleep(100)
            return@fromCallable wallet ?: throw IllegalStateException("Wallet is null")
        }
        .subscribeOn(scheduler)
        .timeout(20, TimeUnit.SECONDS)
    }
}