package com.trading.app.domain.model

data class Candlestick(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val isBullish: Boolean get() = close >= open
    val body: Double get() = kotlin.math.abs(close - open)
    val upperWick: Double get() = high - maxOf(open, close)
    val lowerWick: Double get() = minOf(open, close) - low
}
