package com.trading.app.domain.model

data class WatchlistItem(
    val symbol: String,
    val companyName: String,
    val lastPrice: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val sparklineData: List<Double> = emptyList(),
    val addedAt: Long = System.currentTimeMillis()
) {
    val isPositive: Boolean get() = change >= 0
}
