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

import com.toshi.manager.model.W3PaymentTask
import com.toshi.model.local.EthAndFiat
import com.toshi.model.network.UnsignedTransaction
import com.toshi.model.sofa.payment.Payment
import java.math.BigDecimal

class W3PaymentTaskBuilder {
    fun createW3PaymentTask(): W3PaymentTask {
        val unsignedTransaction = UnsignedTransaction(
                "0xef85746f6b656e843b9aca0082520894011c6dd9565b8b83e6a9ee3f06e89ece3251ef2f87600247e0d368fc80748080",
                "0x5208",
                "0x3b9aca00",
                "0x746f6b656e",
                "0x600247e0d368fc"
        )
        val payment = Payment()
                .setFromAddress("0x28731183c0229b19263cbfd928d445a25034ba4a")
                .setToAddress("0x011c6dd9565b8b83e6a9ee3f06e89ece3251ef2f")
                .setValue("0x600247e0d368fc")
        return W3PaymentTask(
                paymentAmount = EthAndFiat(BigDecimal("0.0270241055021079"), "$10.00 USD"),
                gasPrice = EthAndFiat(BigDecimal("0.0000210000000000"), "$0.01 USD"),
                totalAmount = EthAndFiat(BigDecimal("0.0270451055021079"), "$10.01 USD"),
                unsignedTransaction = unsignedTransaction,
                callbackId = "1",
                payment = payment
        )
    }
}