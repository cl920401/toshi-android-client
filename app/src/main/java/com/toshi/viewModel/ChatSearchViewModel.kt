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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.toshi.manager.RecipientManager
import com.toshi.model.network.SearchResult
import com.toshi.model.network.user.UserType
import com.toshi.model.network.user.UserV2
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
    private val querySubject by lazy { PublishSubject.create<Pair<String, UserType>>() }

    val userSearchResults by lazy { MutableLiveData<List<UserV2>>() }
    val botsSearchResults by lazy { MutableLiveData<List<UserV2>>() }
    val groupSearchResults by lazy { MutableLiveData<List<UserV2>>() }

    init {
        subscribeForQueryChanges()
    }

    private fun subscribeForQueryChanges() {
        val sub = querySubject
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter { it.first.length > 1 }
                .subscribe(
                        { runSearchQuery(it.first, it.second) },
                        { LogUtil.w("Error while listening for query changes $it") }
                )

        subscriptions.add(sub)
    }

    private fun runSearchQuery(query: String, userType: UserType) {
        val searchSub = searchForUsers(query, userType)
                .observeOn(scheduler)
                .subscribe(
                        { handleResponse(it) },
                        { LogUtil.w("Error while searching for users $it") }
                )

        subscriptions.add(searchSub)
    }

    private fun handleResponse(searchResult: SearchResult<UserV2>) {
        val searchQuery = searchResult.query
        val result = searchResult.results
        when {
            searchQuery.contains(UserType.USER.name.toLowerCase()) -> userSearchResults.value = result
            searchQuery.contains(UserType.BOT.name.toLowerCase()) -> botsSearchResults.value = result
            searchQuery.contains(UserType.GROUP.name.toLowerCase()) -> groupSearchResults.value = result
        }
    }

    private fun searchForUsers(query: String, userType: UserType): Single<SearchResult<UserV2>> {
        return recipientManager
                .searchForUsers(query, userType.name.toLowerCase())
    }

    fun search(query: String, userType: UserType) = querySubject.onNext(Pair(query, userType))

    fun getTypeFromPosition(viewPosition: Int): UserType {
        return when (viewPosition) {
            0 -> UserType.USER
            1 -> UserType.BOT
            else -> UserType.GROUP
        }
    }

    fun getPositionFromType(type: UserType): Int {
        return when (type) {
            UserType.USER -> 0
            UserType.BOT -> 1
            else -> 2
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}