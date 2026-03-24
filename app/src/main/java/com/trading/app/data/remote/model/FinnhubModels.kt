package com.trading.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Finnhub real-time trade data from WebSocket.
 * WebSocket message type: "trade"
 */
data class FinnhubTradeMessage(
    @SerializedName("data") val data: List<FinnhubTrade>?,
    @SerializedName("type") val type: String?
)

data class FinnhubTrade(
    @SerializedName("s") val symbol: String,
    @SerializedName("p") val price: Double,
    @SerializedName("v") val volume: Double,
    @SerializedName("t") val timestamp: Long,
    @SerializedName("c") val conditions: List<String>?
)

/**
 * Finnhub REST: /quote
 */
data class FinnhubQuoteResponse(
    @SerializedName("c") val current: Double,       // Current price
    @SerializedName("d") val change: Double?,        // Change
    @SerializedName("dp") val percentChange: Double?,// Percent change
    @SerializedName("h") val high: Double,           // High price of the day
    @SerializedName("l") val low: Double,            // Low price of the day
    @SerializedName("o") val open: Double,           // Open price of the day
    @SerializedName("pc") val previousClose: Double, // Previous close price
    @SerializedName("t") val timestamp: Long         // Timestamp
)

/**
 * Finnhub REST: /stock/candle
 */
data class FinnhubCandleResponse(
    @SerializedName("c") val close: List<Double>?,   // Close prices
    @SerializedName("h") val high: List<Double>?,    // High prices
    @SerializedName("l") val low: List<Double>?,     // Low prices
    @SerializedName("o") val open: List<Double>?,    // Open prices
    @SerializedName("v") val volume: List<Long>?,    // Volume
    @SerializedName("t") val timestamp: List<Long>?, // Timestamps
    @SerializedName("s") val status: String?         // "ok" or "no_data"
)

/**
 * Finnhub REST: /stock/profile2
 */
data class FinnhubCompanyProfile(
    @SerializedName("ticker") val ticker: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("marketCapitalization") val marketCap: Double?,
    @SerializedName("finnhubIndustry") val industry: String?,
    @SerializedName("exchange") val exchange: String?,
    @SerializedName("logo") val logo: String?,
    @SerializedName("weburl") val webUrl: String?
)

/**
 * Finnhub REST: /search
 */
data class FinnhubSearchResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("result") val result: List<FinnhubSearchResult>
)

data class FinnhubSearchResult(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String?,
    @SerializedName("displaySymbol") val displaySymbol: String?
)
