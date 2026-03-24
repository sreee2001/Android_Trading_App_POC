package com.trading.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class OrderTest {

    private fun order(
        side: OrderSide = OrderSide.BUY,
        type: OrderType = OrderType.MARKET,
        status: OrderStatus = OrderStatus.NEW,
        quantity: Int = 100,
        filled: Int = 0
    ) = Order(
        id = "ORD-1",
        symbol = "AAPL",
        side = side,
        type = type,
        timeInForce = TimeInForce.DAY,
        quantity = quantity,
        filledQuantity = filled,
        status = status
    )

    @Test
    fun `remainingQuantity is quantity minus filledQuantity`() {
        val o = order(quantity = 100, filled = 30)
        assertEquals(70, o.remainingQuantity)
    }

    @Test
    fun `remainingQuantity is zero when fully filled`() {
        val o = order(quantity = 100, filled = 100)
        assertEquals(0, o.remainingQuantity)
    }

    @Test
    fun `isFilled returns true only for FILLED status`() {
        assertTrue(order(status = OrderStatus.FILLED).isFilled)
        assertFalse(order(status = OrderStatus.NEW).isFilled)
        assertFalse(order(status = OrderStatus.CANCELLED).isFilled)
        assertFalse(order(status = OrderStatus.PARTIALLY_FILLED).isFilled)
    }

    @Test
    fun `isActive returns true for NEW status`() {
        assertTrue(order(status = OrderStatus.NEW).isActive)
    }

    @Test
    fun `isActive returns true for PARTIALLY_FILLED`() {
        assertTrue(order(status = OrderStatus.PARTIALLY_FILLED).isActive)
    }

    @Test
    fun `isActive returns true for PENDING_CANCEL`() {
        assertTrue(order(status = OrderStatus.PENDING_CANCEL).isActive)
    }

    @Test
    fun `isActive returns false for FILLED`() {
        assertFalse(order(status = OrderStatus.FILLED).isActive)
    }

    @Test
    fun `isActive returns false for CANCELLED`() {
        assertFalse(order(status = OrderStatus.CANCELLED).isActive)
    }

    @Test
    fun `isActive returns false for REJECTED`() {
        assertFalse(order(status = OrderStatus.REJECTED).isActive)
    }

    @Test
    fun `isActive returns false for ACKNOWLEDGED`() {
        assertFalse(order(status = OrderStatus.ACKNOWLEDGED).isActive)
    }

    @Test
    fun `all order types are defined`() {
        val types = OrderType.entries
        assertEquals(5, types.size)
        assertTrue(types.contains(OrderType.MARKET))
        assertTrue(types.contains(OrderType.LIMIT))
        assertTrue(types.contains(OrderType.STOP))
        assertTrue(types.contains(OrderType.STOP_LIMIT))
        assertTrue(types.contains(OrderType.TRAILING_STOP))
    }

    @Test
    fun `all time in force options are defined`() {
        val tifs = TimeInForce.entries
        assertEquals(4, tifs.size)
        assertTrue(tifs.contains(TimeInForce.DAY))
        assertTrue(tifs.contains(TimeInForce.GTC))
        assertTrue(tifs.contains(TimeInForce.IOC))
        assertTrue(tifs.contains(TimeInForce.FOK))
    }
}
