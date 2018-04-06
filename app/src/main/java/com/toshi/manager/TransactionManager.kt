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

package com.toshi.manager

import com.toshi.crypto.HDWallet
import com.toshi.manager.model.ERC20TokenPaymentTask
import com.toshi.manager.model.ExternalPaymentTask
import com.toshi.manager.model.PaymentTask
import com.toshi.manager.model.ResendToshiPaymentTask
import com.toshi.manager.model.ToshiPaymentTask
import com.toshi.manager.model.W3PaymentTask
import com.toshi.manager.network.EthereumInterface
import com.toshi.manager.network.EthereumService
import com.toshi.manager.store.PendingTransactionStore
import com.toshi.manager.transaction.IncomingTransactionManager
import com.toshi.manager.transaction.OutgoingTransactionManager
import com.toshi.manager.transaction.TransactionSigner
import com.toshi.manager.transaction.UpdateTransactionManager
import com.toshi.model.local.OutgoingPaymentResult
import com.toshi.model.local.PendingTransaction
import com.toshi.model.local.UnsignedW3Transaction
import com.toshi.model.local.User
import com.toshi.model.network.SentTransaction
import com.toshi.model.network.SignedTransaction
import com.toshi.model.network.TransactionRequest
import com.toshi.model.network.UnsignedTransaction
import com.toshi.model.sofa.PaymentRequest
import com.toshi.model.sofa.SofaMessage
import com.toshi.model.sofa.payment.ERC20TokenPayment
import com.toshi.model.sofa.payment.Payment
import com.toshi.util.logging.LogUtil
import com.toshi.util.paymentTask.PaymentTaskBuilder
import com.toshi.util.paymentTask.TransactionRequestBuilder
import com.toshi.view.BaseApplication
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

class TransactionManager(
        private val ethService: EthereumInterface = EthereumService.getApi(),
        private val pendingTransactionStore: PendingTransactionStore = PendingTransactionStore(),
        private val transactionSigner: TransactionSigner = TransactionSigner(ethService),
        private val incomingTransactionManager: IncomingTransactionManager = IncomingTransactionManager(pendingTransactionStore),
        private val outgoingTransactionManager: OutgoingTransactionManager = OutgoingTransactionManager(pendingTransactionStore, transactionSigner),
        private val updateTransactionManager: UpdateTransactionManager = UpdateTransactionManager(pendingTransactionStore),
        private val scheduler: Scheduler = Schedulers.io()
) {

    // Todo: Pass BalanceManager and RecipientManager in constructor
    private val paymentTaskBuilder by lazy {
        PaymentTaskBuilder(
                this,
                BaseApplication.get().balanceManager,
                BaseApplication.get().recipientManager,
                TransactionRequestBuilder()
        )
    }
    private val subscriptions by lazy { CompositeSubscription() }

    fun init(wallet: HDWallet): TransactionManager {
        transactionSigner.wallet = wallet
        initEverything()
        return this
    }

    private fun initEverything() {
        updatePendingTransactions()
        attachSubscribers()
    }

    private fun updatePendingTransactions() = updateTransactionManager.updatePendingTransactions()

    fun signW3Transaction(paymentTask: W3PaymentTask): Single<SignedTransaction> {
        return transactionSigner.signW3Transaction(paymentTask)
    }

    fun sendSignedTransaction(signedTransaction: SignedTransaction): Single<SentTransaction> {
        return transactionSigner.sendSignedTransaction(signedTransaction)
    }

    private fun attachSubscribers() {
        attachNewOutgoingPaymentSubscriber()
        attachNewIncomingPaymentSubscriber()
        attachUpdatePaymentSubscriber()
    }

    private fun attachNewIncomingPaymentSubscriber() = incomingTransactionManager.attachNewIncomingPaymentSubscriber()

    private fun attachNewOutgoingPaymentSubscriber() = outgoingTransactionManager.attachNewOutgoingPaymentSubscriber()

    private fun attachUpdatePaymentSubscriber() = updateTransactionManager.attachUpdatePaymentSubscriber()

    fun sendPayment(paymentTask: ToshiPaymentTask) = outgoingTransactionManager.addOutgoingToshiPaymentTask(paymentTask)

    fun resendPayment(paymentTask: ResendToshiPaymentTask) = outgoingTransactionManager.addOutgoingResendPaymentTask(paymentTask)

    fun sendExternalPayment(paymentTask: ExternalPaymentTask) = outgoingTransactionManager.addOutgoingExternalPaymentTask(paymentTask)

    fun sendERC20TokenPayment(paymentTask: ERC20TokenPaymentTask) = outgoingTransactionManager.addOutgoingERC20PaymentTask(paymentTask)

    fun getOutgoingPaymentResultObservable(): Observable<OutgoingPaymentResult> {
        return outgoingTransactionManager
                .outgoingPaymentResultSubject
                .asObservable()
                .subscribeOn(scheduler)
    }

    fun updatePayment(payment: Payment) = updateTransactionManager.updatePayment(payment)

    fun updatePaymentRequestState(remoteUser: User,
                                  sofaMessage: SofaMessage,
                                  @PaymentRequest.State newState: Int) {
        updateTransactionManager.updatePaymentRequestState(remoteUser, sofaMessage, newState)
    }

    fun addIncomingPayment(payment: Payment) = incomingTransactionManager.addIncomingPayment(payment)

    fun getPendingTransactionObservable(): PublishSubject<PendingTransaction> {
        return pendingTransactionStore.pendingTransactionObservable
    }

    fun buildPaymentTask(fromPaymentAddress: String,
                         toPaymentAddress: String,
                         ethAmount: String,
                         sendMaxAmount: Boolean): Single<PaymentTask> {
        return paymentTaskBuilder
                .buildPaymentTask(fromPaymentAddress, toPaymentAddress, ethAmount, sendMaxAmount)
    }

    fun buildPaymentTask(fromPaymentAddress: String,
                         toPaymentAddress: String,
                         ethAmount: String,
                         tokenAddress: String,
                         tokenSymbol: String,
                         tokenDecimals: Int): Single<ERC20TokenPaymentTask> {
        return paymentTaskBuilder
                .buildERC20PaymentTask(
                        fromPaymentAddress,
                        toPaymentAddress,
                        ethAmount,
                        tokenAddress,
                        tokenSymbol,
                        tokenDecimals
                )
    }

    fun buildPaymentTask(callbackId: String, unsignedW3Transaction: UnsignedW3Transaction): Single<W3PaymentTask> {
        return paymentTaskBuilder
                .buildW3PaymentTask(callbackId, unsignedW3Transaction)
    }

    fun listenForNewIncomingTokenPayments(): Observable<ERC20TokenPayment> {
        return incomingTransactionManager
                .incomingTokenPaymentsSubject
                .asObservable()
                .ofType(ERC20TokenPayment::class.java)
    }

    fun createTransaction(transactionRequest: TransactionRequest): Single<UnsignedTransaction> {
        return ethService
                .createTransaction(transactionRequest)
                .subscribeOn(scheduler)
                .doOnError { LogUtil.exception("Error while creating transaction", it) }
    }

    fun clear() {
        subscriptions.clear()
        incomingTransactionManager.clearSubscriptions()
        outgoingTransactionManager.clearSubscriptions()
        updateTransactionManager.clearSubscription()
    }
}