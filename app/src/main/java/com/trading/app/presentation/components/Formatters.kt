package com.trading.app.presentation.components

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    private val priceFormat = DecimalFormat("#,##0.00")
    private val changeFormat = DecimalFormat("+#,##0.00;-#,##0.00")
    private val percentFormat = DecimalFormat("+#,##0.00%;-#,##0.00%")
    private val volumeFormat = DecimalFormat("#,##0")
    private val compactFormat = DecimalFormat("#,##0.0")

    fun formatPrice(price: Double): String = priceFormat.format(price)
    fun formatChange(change: Double): String = changeFormat.format(change)
    fun formatPercent(pct: Double): String = "${if (pct >= 0) "+" else ""}${String.format("%.2f", pct)}%"
    fun formatVolume(volume: Long): String {
        return when {
            volume >= 1_000_000_000 -> "${compactFormat.format(volume / 1_000_000_000.0)}B"
            volume >= 1_000_000 -> "${compactFormat.format(volume / 1_000_000.0)}M"
            volume >= 1_000 -> "${compactFormat.format(volume / 1_000.0)}K"
            else -> volumeFormat.format(volume)
        }
    }

    fun formatMarketCap(cap: Long): String {
        return when {
            cap >= 1_000_000_000_000 -> "${compactFormat.format(cap / 1_000_000_000_000.0)}T"
            cap >= 1_000_000_000 -> "${compactFormat.format(cap / 1_000_000_000.0)}B"
            cap >= 1_000_000 -> "${compactFormat.format(cap / 1_000_000.0)}M"
            else -> volumeFormat.format(cap)
        }
    }

    fun formatCurrency(value: Double): String = "$${priceFormat.format(value)}"

    fun formatTimestamp(ts: Long, pattern: String = "HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.US)
        return sdf.format(Date(ts))
    }
}
