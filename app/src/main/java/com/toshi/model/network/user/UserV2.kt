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

package com.toshi.model.network.user

import com.squareup.moshi.Json

data class UserV2(
        @Json(name = "toshi_id")
        val toshiId: String?,
        val username: String = "",
        val name: String?,
        val avatar: String?,
        val description: String = "",
        val location: String?,
        val type: UserType,
        val public: Boolean = false,
        @Json(name = "payment_address")
        val paymentAddress: String?,
        @Json(name = "reputation_score")
        val reputationScore: Double = 0.0,
        @Json(name = "average_rating")
        val averageRating: Double = 0.0,
        @Json(name = "review_count")
        val reviewCount: Int = 0
) {
    // Defaults to the username if no name is set.
    fun getDisplayName(): String {
        return if (name?.isNotEmpty() == true) name
        else username
    }
}