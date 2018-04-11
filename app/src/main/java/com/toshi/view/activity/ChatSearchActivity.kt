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
import com.toshi.model.network.user.UserType
import com.toshi.model.network.user.UserV2
import com.toshi.view.adapter.ChatSearchTabAdapter
import com.toshi.view.adapter.listeners.TextChangedListener
import com.toshi.view.custom.ChatSearchView
import com.toshi.viewModel.ChatSearchViewModel
import kotlinx.android.synthetic.main.activity_chat_search.search
import kotlinx.android.synthetic.main.activity_chat_search.tabLayout
import kotlinx.android.synthetic.main.activity_chat_search.viewPager

class ChatSearchActivity : AppCompatActivity() {

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
        tabAdapter = ChatSearchTabAdapter(tabs, this)
        viewPager.adapter = tabAdapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)
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
        val type = getTypeFromPosition(currentViewPosition)
        viewModel.search(query, type)
    }

    private fun getTypeFromPosition(viewPosition: Int): UserType {
        return when (viewPosition) {
            0 -> UserType.USER
            1 -> UserType.BOT
            else -> UserType.GROUP
        }
    }

    private fun initObservers() {
        viewModel.searchResults.observe(this, Observer {
            if (it != null) addSearchResult(it)
        })
    }

    private fun addSearchResult(users: List<UserV2>) {
        val positionOfCurrentView = viewPager.currentItem
        val view = viewPager.findViewById<ChatSearchView>(positionOfCurrentView)
        view.setUsers(users)
    }
}