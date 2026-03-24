package com.trading.app.domain.model

data class Order(
    val id: String,
    val symbol: String,
    val side: OrderSide,
    val type: OrderType,
    val timeInForce: TimeInForce,
    val quantity: Int,
    val filledQuantity: Int = 0,
    val price: Double? = null,       // for limit orders
    val stopPrice: Double? = null,   // for stop orders
    val trailAmount: Double? = null, // for trailing stop
    val status: OrderStatus = OrderStatus.NEW,
    val avgFillPrice: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val remainingQuantity: Int get() = quantity - filledQuantity
    val isFilled: Boolean get() = status == OrderStatus.FILLED
    val isActive: Boolean get() = status in listOf(
        OrderStatus.NEW,
        OrderStatus.PARTIALLY_FILLED,
        OrderStatus.PENDING_CANCEL
    )
}

enum class OrderSide { BUY, SELL }

enum class OrderType {
    MARKET, LIMIT, STOP, STOP_LIMIT, TRAILING_STOP
}

enum class TimeInForce {
    DAY, GTC, IOC, FOK
}

enum class OrderStatus {
    NEW, ACKNOWLEDGED, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED, PENDING_CANCEL
}
