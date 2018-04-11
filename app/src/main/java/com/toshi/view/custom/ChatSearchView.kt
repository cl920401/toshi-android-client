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

package com.toshi.view.custom

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.widget.FrameLayout
import com.toshi.R
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.model.network.user.UserV2
import com.toshi.view.adapter.UserAdapter
import kotlinx.android.synthetic.main.view_chat_search.view.searchList

class ChatSearchView : FrameLayout {
    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private lateinit var userAdapter: UserAdapter
    var onUserClickListener: ((UserV2) -> Unit)? = null

    private fun init() {
        inflate(context, R.layout.view_chat_search, this)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        userAdapter = UserAdapter(
                onItemClickListener = { onUserClickListener?.invoke(it) }
        )
        searchList.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context)
            addHorizontalLineDivider()
        }
    }

    fun setUsers(users: List<UserV2>) = userAdapter.setUsers(users)
}