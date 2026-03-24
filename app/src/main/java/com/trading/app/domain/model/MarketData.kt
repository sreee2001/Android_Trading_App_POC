package com.trading.app.domain.model

data class OrderBookEntry(
    val price: Double,
    val quantity: Int
)

data class OrderBook(
    val symbol: String,
    val bids: List<OrderBookEntry>,
    val asks: List<OrderBookEntry>,
    val timestamp: Long = System.currentTimeMillis()
) {
    val spread: Double get() = if (asks.isNotEmpty() && bids.isNotEmpty()) {
        asks.first().price - bids.first().price
    } else 0.0

    val spreadPercent: Double get() = if (asks.isNotEmpty()) {
        (spread / asks.first().price) * 100
    } else 0.0
}

data class MarketIndex(
    val name: String,
    val symbol: String,
    val value: Double,
    val change: Double,
    val changePercent: Double
) {
    val isPositive: Boolean get() = change >= 0
}

data class SectorPerformance(
    val name: String,
    val changePercent: Double
) {
    val isPositive: Boolean get() = changePercent >= 0
}
