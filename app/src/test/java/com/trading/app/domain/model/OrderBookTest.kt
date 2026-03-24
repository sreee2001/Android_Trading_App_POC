package com.trading.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class OrderBookTest {

    @Test
    fun `spread is ask minus bid when both exist`() {
        val book = OrderBook(
            symbol = "AAPL",
            bids = listOf(OrderBookEntry(149.90, 100)),
            asks = listOf(OrderBookEntry(150.10, 200))
        )
        assertEquals(0.20, book.spread, 0.001)
    }

    @Test
    fun `spread is zero when asks are empty`() {
        val book = OrderBook(
            symbol = "AAPL",
            bids = listOf(OrderBookEntry(149.90, 100)),
            asks = emptyList()
        )
        assertEquals(0.0, book.spread, 0.001)
    }

    @Test
    fun `spread is zero when bids are empty`() {
        val book = OrderBook(
            symbol = "AAPL",
            bids = emptyList(),
            asks = listOf(OrderBookEntry(150.10, 200))
        )
        assertEquals(0.0, book.spread, 0.001)
    }

    @Test
    fun `spreadPercent calculated correctly`() {
        val book = OrderBook(
            symbol = "AAPL",
            bids = listOf(OrderBookEntry(100.0, 100)),
            asks = listOf(OrderBookEntry(100.5, 200))
        )
        val expectedPct = (0.5 / 100.5) * 100
        assertEquals(expectedPct, book.spreadPercent, 0.001)
    }

    @Test
    fun `spreadPercent is zero when asks empty`() {
        val book = OrderBook(
            symbol = "AAPL",
            bids = listOf(OrderBookEntry(100.0, 100)),
            asks = emptyList()
        )
        assertEquals(0.0, book.spreadPercent, 0.001)
    }
}
