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

package com.toshi.managers.dappManager

import com.toshi.model.network.dapp.Dapp
import com.toshi.model.network.dapp.DappSearchResult
import com.toshi.model.network.dapp.DappSection
import com.toshi.model.network.dapp.DappSections
import com.toshi.model.network.dapp.Dapps

class DappMocker {

    val replacementUrl = "https://buy.coinbase.com?code=9a6fb1e3-af41-5677-9b67-8c8a0e365771\\u0026address="
    val validUrl = "https://buy.coinbase.com"
    val invalidHostUrl = "https://buy.coinbae.com"
    val validHost = "buy.coinbase.com"

    private val gamesList by lazy {
        mutableListOf(
                Dapp(
                        dappId = 1L,
                        name = "Game dapp",
                        url = "https://www.gamedapp.com",
                        categories = listOf(1),
                        description = null,
                        icon = null,
                        cover = null
                ),
                Dapp(
                        dappId = 2L,
                        name = "Tower Defense",
                        url = "www.towerdefense.com",
                        categories = listOf(1),
                        description = null,
                        icon = null,
                        cover = null
                )
        )
    }

    private val socialMediaList by lazy {
        mutableListOf(
                Dapp(
                        dappId = 1L,
                        name = "FaceDapp",
                        url = "https://www.facedapp.com",
                        categories = listOf(2),
                        description = null,
                        icon = null,
                        cover = null
                ),
                Dapp(
                        dappId = 2L,
                        name = "TwitDapp",
                        url = "https://www.twitdapp.com",
                        categories = listOf(2),
                        description = null,
                        icon = null,
                        cover = null
                )
        )
    }

    private fun createExhangeList(url: String?): MutableList<Dapp> {
        return mutableListOf(
                Dapp(
                        dappId = 1L,
                        name = "ERC DEX",
                        url = "https://www.ercdex.com",
                        categories = listOf(3),
                        description = null,
                        icon = null,
                        cover = null
                ),
                Dapp(
                        dappId = 2L,
                        name = "Get ETH",
                        url = url,
                        categories = listOf(3),
                        description = null,
                        icon = null,
                        cover = null
                )
        )
    }

    fun buildProtocolDappSections(): DappSections {
        val sections = mutableListOf(
                DappSection(
                        categoryId = 10,
                        dapps = gamesList
                ),
                DappSection(
                        categoryId = 20,
                        dapps = createExhangeList(validUrl)
                ),
                DappSection(
                        categoryId = 30,
                        dapps = socialMediaList
                )
        )
        return DappSections(sections = sections)
    }

    fun buildInvalidHostDappSections(): DappSections {
        val sections = mutableListOf(
                DappSection(
                        categoryId = 10,
                        dapps = gamesList
                ),
                DappSection(
                        categoryId = 20,
                        dapps = createExhangeList(invalidHostUrl)
                ),
                DappSection(
                        categoryId = 30,
                        dapps = socialMediaList
                )
        )
        return DappSections(sections = sections)
    }

    fun buildDappSectionsWithNullUrl(): DappSections {
        val sections = mutableListOf(
                DappSection(
                        categoryId = 10,
                        dapps = gamesList
                ),
                DappSection(
                        categoryId = 20,
                        dapps = createExhangeList(null)
                ),
                DappSection(
                        categoryId = 30,
                        dapps = socialMediaList
                )
        )
        return DappSections(sections = sections)
    }

    fun buildProtocolDappSearchResult(): DappSearchResult {
        return DappSearchResult(results = Dapps(
                dapps = createExhangeList(validUrl)
        ))
    }

    fun buildInvalidHostDappSearchResult(): DappSearchResult {
        return DappSearchResult(results = Dapps(
                dapps = createExhangeList(invalidHostUrl)
        ))
    }

    fun buildDappSearchResultsWithNullUrl(): DappSearchResult {
        return DappSearchResult(results = Dapps(
                dapps = createExhangeList(null)
        ))
    }
}