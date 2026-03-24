package com.trading.app.presentation.orderentry

import com.trading.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class OrderEntryUiStateTest {

    private fun state(
        quantity: String = "100",
        type: OrderType = OrderType.MARKET,
        currentPrice: Double = 150.0,
        limitPrice: String = "",
        side: OrderSide = OrderSide.BUY
    ) = OrderEntryUiState(
        symbol = "AAPL",
        companyName = "Apple Inc.",
        currentPrice = currentPrice,
        side = side,
        type = type,
        quantity = quantity,
        limitPrice = limitPrice,
        buyingPower = 100_000.0
    )

    @Test
    fun `estimatedCost for market order uses current price`() {
        val s = state(quantity = "100", type = OrderType.MARKET, currentPrice = 150.0)
        assertEquals(15_000.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost for limit order uses limit price`() {
        val s = state(quantity = "100", type = OrderType.LIMIT, currentPrice = 150.0, limitPrice = "145.50")
        assertEquals(14_550.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost for limit order falls back to current price when limit empty`() {
        val s = state(quantity = "100", type = OrderType.LIMIT, currentPrice = 150.0, limitPrice = "")
        assertEquals(15_000.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost for stop order uses current price`() {
        val s = state(quantity = "50", type = OrderType.STOP, currentPrice = 200.0)
        assertEquals(10_000.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost for stop-limit order uses limit price`() {
        val s = state(quantity = "50", type = OrderType.STOP_LIMIT, currentPrice = 200.0, limitPrice = "195.00")
        assertEquals(9_750.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost is zero when quantity is empty`() {
        val s = state(quantity = "")
        assertEquals(0.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost is zero when quantity is non-numeric`() {
        val s = state(quantity = "abc")
        assertEquals(0.0, s.estimatedCost, 0.001)
    }

    @Test
    fun `estimatedCost handles trailing stop`() {
        val s = state(quantity = "100", type = OrderType.TRAILING_STOP, currentPrice = 300.0)
        assertEquals(30_000.0, s.estimatedCost, 0.001)
    }
}
