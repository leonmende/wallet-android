/**
 * API
 * Create API keys in your profile and use public API key as username and secret as password to authorize.
 *
 * The version of the OpenAPI document: 2.19.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.mycelium.bequant.remote.trading.model


import com.squareup.moshi.Json

/**
 *
 * @param id
 * @param clientOrderId
 * @param orderId
 * @param symbol
 * @param side
 * @param quantity
 * @param fee
 * @param price
 * @param timestamp
 * @param positionId Margin position
 * @param pnl Margin profit or loss in currency
 * @param liquidation
 */

data class Trade(
        @Json(name = "id")
        val id: kotlin.Long,
        @Json(name = "clientOrderId")
        val clientOrderId: kotlin.String,
        @Json(name = "orderId")
        val orderId: kotlin.Long,
        @Json(name = "symbol")
        val symbol: kotlin.String,
        @Json(name = "side")
        val side: Trade.Side,
        @Json(name = "quantity")
        val quantity: kotlin.String,
        @Json(name = "fee")
        val fee: kotlin.String,
        @Json(name = "price")
        val price: kotlin.String,
        @Json(name = "timestamp")
        val timestamp: java.util.Date,
        /* Margin position */
        @Json(name = "positionId")
        val positionId: java.math.BigDecimal? = null,
        /* Margin profit or loss in currency */
        @Json(name = "pnl")
        val pnl: kotlin.Double? = null,
        @Json(name = "liquidation")
        val liquidation: kotlin.Boolean? = null
) {

    /**
     *
     * Values: sell,buy
     */

    enum class Side(val value: kotlin.String) {
        @Json(name = "sell")
        sell("sell"),
        @Json(name = "buy")
        buy("buy");
    }
}

