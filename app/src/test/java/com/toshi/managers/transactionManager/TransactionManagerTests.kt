/*
 *
 *  * 	Copyright (c) 2018. Toshi Inc
 *  *
 *  * 	This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.toshi.managers.transactionManager

import com.toshi.manager.TransactionManager
import com.toshi.manager.network.EthereumInterface
import com.toshi.manager.store.PendingTransactionStore
import com.toshi.manager.transaction.IncomingTransactionManager
import com.toshi.manager.transaction.OutgoingTransactionManager
import com.toshi.manager.transaction.TransactionSigner
import com.toshi.manager.transaction.UpdateTransactionManager
import com.toshi.masterSeed
import com.toshi.mockWallet
import com.toshi.model.network.SentTransaction
import com.toshi.model.network.ServerTime
import com.toshi.model.network.SignedTransaction
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import rx.Single
import rx.schedulers.Schedulers

class TransactionManagerTests {
    private lateinit var transactionManager: TransactionManager

    @Before
    fun before() {
        initTransactionManagerWithoutWallet()
    }

    private fun mockEthApi(): EthereumInterface {
        val ethApi = Mockito.mock(EthereumInterface::class.java)
        Mockito.`when`(ethApi.timestamp)
                .thenReturn(Single.just(ServerTime(1L)))
        Mockito.`when`(ethApi.sendSignedTransaction(any(Long::class.java), any(SignedTransaction::class.java)))
                .thenReturn(Single.just(SentTransaction()))
        return ethApi
    }

    private fun mockIncomingTransactionManager(): IncomingTransactionManager {
        return Mockito.mock(IncomingTransactionManager::class.java)
    }

    private fun mockOutgoingTransactionManager(): OutgoingTransactionManager {
        return Mockito.mock(OutgoingTransactionManager::class.java)
    }

    private fun mockUpdateTransactionManager(): UpdateTransactionManager {
        return Mockito.mock(UpdateTransactionManager::class.java)
    }

    @Test
    fun `test signing a transaction when wallet is null`() {
        initTransactionManagerWithoutWallet()
        val w3PaymentTask = W3PaymentTaskBuilder().createW3PaymentTask()
        try {
            transactionManager.signW3Transaction(w3PaymentTask).toBlocking().value()
        } catch (e: Exception) {
            assertTrue(true)
            return
        }
        assertTrue(false)
    }

    @Test
    fun `test signing a transaction when wallet is not null`() {
        initTransactionManagerWithWallet()
        val w3PaymentTask = W3PaymentTaskBuilder().createW3PaymentTask()
        val signedW3Transaction = transactionManager.signW3Transaction(w3PaymentTask).toBlocking().value()
        assertNotNull(signedW3Transaction)
    }

    private fun initTransactionManagerWithWallet() {
        initTransactionManagerWithoutWallet()
        transactionManager.init(mockWallet(masterSeed))
    }

    private fun initTransactionManagerWithoutWallet() {
        val ethApi = mockEthApi()
        transactionManager = TransactionManager(
                ethService = ethApi,
                pendingTransactionStore = PendingTransactionStore(),
                transactionSigner = TransactionSigner(ethApi),
                incomingTransactionManager = mockIncomingTransactionManager(),
                outgoingTransactionManager = mockOutgoingTransactionManager(),
                updateTransactionManager = mockUpdateTransactionManager(),
                scheduler = Schedulers.trampoline()
        )
    }
}