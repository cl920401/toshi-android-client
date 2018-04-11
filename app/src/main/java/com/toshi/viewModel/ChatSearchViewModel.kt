/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 *  This program is free software: you can redistribute it and/or modify
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

package com.toshi.viewModel

import android.arch.lifecycle.ViewModel
import com.toshi.manager.RecipientManager
import com.toshi.model.local.User
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.Scheduler
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class ChatSearchViewModel(
        private val recipientManager: RecipientManager = BaseApplication.get().recipientManager,
        private val scheduler: Scheduler = AndroidSchedulers.mainThread()
) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    private val querySubject by lazy { PublishSubject.create<String>() }

    val searchResults by lazy { SingleLiveEvent<List<User>>() }

    init {
        subscribeForQueryChanges()
    }

    private fun subscribeForQueryChanges() {
        val sub = querySubject.debounce(500, TimeUnit.MILLISECONDS)
                .filter { it.length > 1 }
                .subscribe(
                        { runSearchQuery(it) },
                        { LogUtil.w("Error while listening for query changes $it") }
                )

        subscriptions.add(sub)
    }

    private fun runSearchQuery(query: String) {
        val searchSub = searchOnlineUsers(query)
                .observeOn(scheduler)
                .subscribe(
                        { searchResults.value = it },
                        { LogUtil.w("Error while search for user $it") }
                )

        subscriptions.add(searchSub)
    }

    private fun searchOnlineUsers(query: String): Single<List<User>> {
        return recipientManager.searchOnlineUsersAndApps(query)
    }

    fun search(query: CharSequence) = querySubject.onNext(query.toString())

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}