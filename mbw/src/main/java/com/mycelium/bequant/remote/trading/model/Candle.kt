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
 * @param timestamp
 * @param open
 * @param close
 * @param min
 * @param max
 * @param volume
 * @param volumeQuote
 */

data class Candle(
        @Json(name = "timestamp")
        val timestamp: java.util.Date,
        @Json(name = "open")
        val open: kotlin.String,
        @Json(name = "close")
        val close: kotlin.String,
        @Json(name = "min")
        val min: kotlin.String,
        @Json(name = "max")
        val max: kotlin.String,
        @Json(name = "volume")
        val volume: kotlin.String,
        @Json(name = "volumeQuote")
        val volumeQuote: kotlin.String
)

