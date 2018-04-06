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

package com.toshi.model.network;

import com.squareup.moshi.Json;

public class UnsignedTransaction {
    private String tx;
    private String gas;
    @Json(name = "gas_price")
    private String gasPrice;
    private String nonce;
    private String value;

    public UnsignedTransaction(final String tx, final String gas, final String gasPrice,
                               final String nonce, final String value) {
        this.tx = tx;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.nonce = nonce;
        this.value = value;
    }

    public String getTransaction() {
        return this.tx;
    }

    public String getGas() {
        return gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getNonce() {
        return nonce;
    }

    public String getValue() {
        return value;
    }
}
