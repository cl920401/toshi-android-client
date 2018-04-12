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

package com.toshi.view.activity

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.getViewModel
import com.toshi.extensions.startActivity
import com.toshi.model.network.user.UserType
import com.toshi.model.network.user.UserV2
import com.toshi.view.adapter.ChatSearchTabAdapter
import com.toshi.view.adapter.listeners.TextChangedListener
import com.toshi.view.custom.ChatSearchView
import com.toshi.viewModel.ChatSearchViewModel
import kotlinx.android.synthetic.main.activity_chat_search.closeButton
import kotlinx.android.synthetic.main.activity_chat_search.search
import kotlinx.android.synthetic.main.activity_chat_search.tabLayout
import kotlinx.android.synthetic.main.activity_chat_search.viewPager

class ChatSearchActivity : AppCompatActivity() {

    companion object {
        const val TYPE = "type"
        const val CHAT = "chat"
        const val VIEW_PROFILE = "viewProfile"
    }

    private lateinit var viewModel: ChatSearchViewModel
    private lateinit var tabAdapter: ChatSearchTabAdapter

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        setContentView(R.layout.activity_chat_search)
        init()
    }

    private fun init() {
        initViewModel()
        initAdapter()
        initClickListeners()
        initTextListener()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initAdapter() {
        val tabs = listOf(
                getString(R.string.users),
                getString(R.string.bots),
                getString(R.string.groups)
        )
        tabAdapter = ChatSearchTabAdapter(
                context = this,
                tabs = tabs,
                onUserClickedListener = { handleUserClicked(it) },
                onItemUpdatedListener = { handleAdapterUpdated(it) }
        )
        viewPager.adapter = tabAdapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun handleUserClicked(user: UserV2) {
        val viewType = getViewType()
        when (viewType) {
            CHAT -> startActivity<ChatActivity> { putExtra(ChatActivity.EXTRA__THREAD_ID, user.toshiId) }
            else -> startActivity<ViewUserActivity> { putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, user.toshiId) }
        }
    }

    private fun getViewType() = intent.getStringExtra(TYPE) ?: VIEW_PROFILE

    private fun handleAdapterUpdated(position: Int) {
        when (position) {
            0 -> {
                val userSearchResult = viewModel.userSearchResults.value ?: emptyList()
                if (userSearchResult.isNotEmpty()) addSearchResult(userSearchResult, UserType.USER)
            }
            1 -> {
                val botSearchResult = viewModel.botsSearchResults.value ?: emptyList()
                if (botSearchResult.isNotEmpty()) addSearchResult(botSearchResult, UserType.BOT)
            }
            2 -> {
                val groupSearchResult = viewModel.groupSearchResults.value ?: emptyList()
                if (groupSearchResult.isNotEmpty()) addSearchResult(groupSearchResult, UserType.GROUP)
            }
        }
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
    }

    private fun initTextListener() {
        search.addTextChangedListener(object : TextChangedListener() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handleSearchQuery(s.toString())
            }
        })
    }

    private fun handleSearchQuery(query: String) {
        val currentViewPosition = viewPager.currentItem
        val type = viewModel.getTypeFromPosition(currentViewPosition)
        viewModel.search(query, type)
    }

    private fun initObservers() {
        viewModel.userSearchResults.observe(this, Observer {
            if (it != null) addSearchResult(it, UserType.USER)
        })
        viewModel.botsSearchResults.observe(this, Observer {
            if (it != null) addSearchResult(it, UserType.BOT)
        })
        viewModel.groupSearchResults.observe(this, Observer {
            if (it != null) addSearchResult(it, UserType.GROUP)
        })
    }

    private fun addSearchResult(users: List<UserV2>, type: UserType) {
        val positionOfView = viewModel.getPositionFromType(type)
        val view = viewPager.findViewById<ChatSearchView>(positionOfView)
        view?.setUsers(users)
    }
}