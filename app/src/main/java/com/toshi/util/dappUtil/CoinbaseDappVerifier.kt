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

package com.toshi.util.dappUtil

import android.content.Context
import com.toshi.R
import com.toshi.model.network.dapp.Dapp
import com.toshi.model.network.dapp.DappSearchResult
import com.toshi.model.network.dapp.DappSections
import java.net.URI
import java.net.URISyntaxException

class CoinbaseDappVerifier(
        private val context: Context
) {

    fun updateUrlIfValid(searchResult: DappSearchResult, paymentAddress: String) {
        val validCoinbaseApp = findValidCoinbaseDapp(searchResult.results.dapps)
        if (validCoinbaseApp != null) updateUrlWithPaymentAddress(searchResult.results.dapps, validCoinbaseApp, paymentAddress)
    }

    fun updateUrlIfValid(dappSections: DappSections, paymentAddress: String) {
        for (section in dappSections.sections) {
            val validCoinbaseDapp = findValidCoinbaseDapp(section.dapps)
            if (validCoinbaseDapp != null) updateUrlWithPaymentAddress(section.dapps, validCoinbaseDapp, paymentAddress)
        }
    }

    private fun findValidCoinbaseDapp(dapps: List<Dapp>): Dapp? = dapps.firstOrNull { isValidHost(it.url) }

    private fun updateUrlWithPaymentAddress(dapps: MutableList<Dapp>, validCoinbaseDapp: Dapp, paymentAddress: String) {
        val index = dapps.indexOf(validCoinbaseDapp)
        val coinbaseDappUrl = getCoinbaseDappUrl()
        dapps[index] = validCoinbaseDapp.copy(url = coinbaseDappUrl + paymentAddress)
    }

    private fun isValidHost(url: String?): Boolean {
        if (url == null) return false
        return try {
            val uri = URI(url)
            val expectedHost = context.getString(R.string.coinbase_dapp_host)
            uri.host == expectedHost
        } catch (e: URISyntaxException) {
            false
        }
    }

    private fun getCoinbaseDappUrl() = context.getString(R.string.coinbase_dapp_url)
}