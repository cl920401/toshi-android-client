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

package com.toshi.view.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.toshi.model.network.user.UserV2
import com.toshi.view.custom.ChatSearchView

class ChatSearchTabAdapter(
        private val context: Context,
        private val tabs: List<String>,
        private val onItemUpdatedListener: (Int) -> Unit,
        private val onUserClickedListener: (UserV2) -> Unit
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = ChatSearchView(context)
        view.onUserClickListener = onUserClickedListener
        view.id = position
        container.addView(view)
        onItemUpdatedListener(position)
        return view
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        val view = any as? View ?: return
        container.removeView(view)
    }

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence {
        return if (position > tabs.size) return ""
        else tabs[position]
    }
}