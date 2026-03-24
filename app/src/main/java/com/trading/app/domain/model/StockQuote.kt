package com.trading.app.domain.model

data class StockQuote(
    val symbol: String,
    val companyName: String,
    val lastPrice: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val volume: Long,
    val marketCap: Long,
    val bid: Double,
    val ask: Double,
    val bidSize: Int,
    val askSize: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isPositive: Boolean get() = change >= 0
    val spread: Double get() = ask - bid
}
