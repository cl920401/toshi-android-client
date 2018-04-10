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

package com.toshi.util.coinbaseDappVerifier

import android.content.Context
import com.toshi.R
import com.toshi.managers.dappManager.DappMocker
import com.toshi.util.dappUtil.CoinbaseDappVerifier
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class CoinbaseDappVerifierTests {

    private val paymentAddress = "0x0"
    private val dappMocker by lazy { DappMocker() }

    private lateinit var coinbaseDappVerifier: CoinbaseDappVerifier

    @Before
    fun before() {
        coinbaseDappVerifier = CoinbaseDappVerifier(context = mockContext())
    }

    private fun mockContext(): Context {
        val context = Mockito.mock(Context::class.java)
        Mockito.`when`(context.getString(R.string.coinbase_dapp_host))
                .thenReturn(dappMocker.validHost)
        Mockito.`when`(context.getString(R.string.coinbase_dapp_url))
                .thenReturn(dappMocker.replacementUrl)
        return context
    }

    @Test
    fun testValidCoinbaseDappSectionUrl() {
        val validDappSections = dappMocker.buildProtocolDappSections()
        coinbaseDappVerifier.updateUrlIfValid(validDappSections, paymentAddress)
        val expectedUrl = "${dappMocker.replacementUrl}$paymentAddress"
        val actualUrl = validDappSections.sections[1].dapps[1].url
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun testCoinbaseDappSectionInvalidHost() {
        val invalidDappSections = dappMocker.buildInvalidHostDappSections()
        coinbaseDappVerifier.updateUrlIfValid(invalidDappSections, paymentAddress)
        val expectedUrl = dappMocker.invalidHostUrl
        val actualUrl = invalidDappSections.sections[1].dapps[1].url
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun testCoinbaseDappSectionWithNullUrl() {
        val invalidDappSections = dappMocker.buildDappSectionsWithNullUrl()
        coinbaseDappVerifier.updateUrlIfValid(invalidDappSections, paymentAddress)
        val actualUrl = invalidDappSections.sections[1].dapps[1].url
        assertEquals(null, actualUrl)
    }

    @Test
    fun testValidCoinbaseDappSearchResultUrl() {
        val validDappSearchResult = dappMocker.buildProtocolDappSearchResult()
        coinbaseDappVerifier.updateUrlIfValid(validDappSearchResult, paymentAddress)
        val expectedUrl = "${dappMocker.replacementUrl}$paymentAddress"
        val actualUrl = validDappSearchResult.results.dapps[1].url
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun testCoinbaseDappSearchResultInvalidHost() {
        val invalidDappSearchResult = dappMocker.buildInvalidHostDappSearchResult()
        coinbaseDappVerifier.updateUrlIfValid(invalidDappSearchResult, paymentAddress)
        val expectedUrl = dappMocker.invalidHostUrl
        val actualUrl = invalidDappSearchResult.results.dapps[1].url
        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun testCoinbaseDappSearchResultWithNullUrl() {
        val invalidDappSearchResult = dappMocker.buildDappSearchResultsWithNullUrl()
        coinbaseDappVerifier.updateUrlIfValid(invalidDappSearchResult, paymentAddress)
        val actualUrl = invalidDappSearchResult.results.dapps[1].url
        assertEquals(null, actualUrl)
    }
}