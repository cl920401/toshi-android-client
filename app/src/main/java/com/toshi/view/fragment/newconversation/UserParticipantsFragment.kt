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

package com.toshi.view.fragment.newconversation

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toshi.R
import com.toshi.extensions.startActivity
import com.toshi.extensions.startExternalActivity
import com.toshi.util.KeyboardUtil
import com.toshi.view.activity.ChatSearchActivity
import com.toshi.view.activity.ChatSearchActivity.Companion.CHAT
import com.toshi.view.activity.ChatSearchActivity.Companion.TYPE
import com.toshi.view.activity.ConversationSetupActivity
import kotlinx.android.synthetic.main.fragment_user_participants.closeButton
import kotlinx.android.synthetic.main.fragment_user_participants.inviteFriend
import kotlinx.android.synthetic.main.fragment_user_participants.newGroup
import kotlinx.android.synthetic.main.fragment_user_participants.search

class UserParticipantsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = init()

    private fun init() {
        initClickListeners()
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { handleCloseClicked(it) }
        newGroup.setOnClickListener { handleNewGroupClicked() }
        inviteFriend.setOnClickListener { handleInviteFriends() }
        search.setOnClickListener { startActivity<ChatSearchActivity> { putExtra(TYPE, CHAT) } }
    }

    private fun handleCloseClicked(v: View?) {
        KeyboardUtil.hideKeyboard(v)
        activity?.onBackPressed()
    }

    private fun handleNewGroupClicked() = (this.activity as ConversationSetupActivity).openNewGroupFlow()

    private fun handleInviteFriends() = startExternalActivity {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_friends_intent_message))
    }
}