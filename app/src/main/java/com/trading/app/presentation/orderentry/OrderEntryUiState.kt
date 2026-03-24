package com.trading.app.presentation.orderentry

import com.trading.app.domain.model.*

data class OrderEntryUiState(
    val symbol: String = "",
    val companyName: String = "",
    val currentPrice: Double = 0.0,
    val side: OrderSide = OrderSide.BUY,
    val type: OrderType = OrderType.MARKET,
    val timeInForce: TimeInForce = TimeInForce.DAY,
    val quantity: String = "",
    val limitPrice: String = "",
    val stopPrice: String = "",
    val buyingPower: Double = 0.0,
    val currentPosition: Int = 0,
    val showConfirmation: Boolean = false,
    val orderResult: String? = null,
    val error: String? = null
) {
    val estimatedCost: Double get() {
        val qty = quantity.toIntOrNull() ?: 0
        val price = when (type) {
            OrderType.MARKET -> currentPrice
            OrderType.LIMIT, OrderType.STOP_LIMIT -> limitPrice.toDoubleOrNull() ?: currentPrice
            else -> currentPrice
        }
        return qty * price
    }
}
