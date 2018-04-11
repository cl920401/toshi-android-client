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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.view.adapter.ChatSearchTabAdapter
import kotlinx.android.synthetic.main.fragment_wallet.tabLayout
import kotlinx.android.synthetic.main.fragment_wallet.viewPager

class ChatSearchActivity : AppCompatActivity() {

    private lateinit var tabAdapter: ChatSearchTabAdapter

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        setContentView(R.layout.activity_chat_search)
        init()
    }

    private fun init() {
        initAdapter()
    }

    private fun initAdapter() {
        val tabs = listOf(
                getString(R.string.users),
                getString(R.string.bots),
                getString(R.string.groups)
        )
        tabAdapter = ChatSearchTabAdapter(tabs, this)
        viewPager.adapter = tabAdapter
        tabLayout.setupWithViewPager(viewPager)
    }
}