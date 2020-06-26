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
 * @param ask
 * @param bid
 * @param timestamp
 * @param askAveragePrice Ask average price for specified volume. Optional (only for volume requests)
 * @param bidAveragePrice Bid average price for specified volume. Optional (only for volume requests)
 */

data class Orderbook(
        @Json(name = "ask")
        val ask: kotlin.Array<OrderbookAsk>? = null,
        @Json(name = "bid")
        val bid: kotlin.Array<OrderbookAsk>? = null,
        @Json(name = "timestamp")
        val timestamp: java.util.Date? = null,
        /* Ask average price for specified volume. Optional (only for volume requests) */
        @Json(name = "askAveragePrice")
        val askAveragePrice: kotlin.String? = null,
        /* Bid average price for specified volume. Optional (only for volume requests) */
        @Json(name = "bidAveragePrice")
        val bidAveragePrice: kotlin.String? = null
)

