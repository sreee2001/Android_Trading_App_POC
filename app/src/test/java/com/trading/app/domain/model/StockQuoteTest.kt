package com.trading.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class StockQuoteTest {

    private fun quote(price: Double = 150.0, change: Double = 2.5, pct: Double = 1.5) =
        StockQuote(
            symbol = "AAPL", companyName = "Apple Inc.",
            lastPrice = price, change = change, changePercent = pct,
            open = 148.0, high = 152.0, low = 147.0,
            volume = 50_000_000, marketCap = 2_500_000_000_000,
            bid = 149.95, ask = 150.05, bidSize = 100, askSize = 200
        )

    @Test
    fun `isPositive returns true when change is positive`() {
        assertTrue(quote(change = 1.0).isPositive)
    }

    @Test
    fun `isPositive returns true when change is zero`() {
        assertTrue(quote(change = 0.0).isPositive)
    }

    @Test
    fun `isPositive returns false when change is negative`() {
        assertFalse(quote(change = -1.0).isPositive)
    }

    @Test
    fun `spread is computed as ask minus bid`() {
        val q = quote()
        assertEquals(0.10, q.spread, 0.001)
    }

    @Test
    fun `timestamp defaults to current time`() {
        val before = System.currentTimeMillis()
        val q = quote()
        val after = System.currentTimeMillis()
        assertTrue(q.timestamp in before..after)
    }
}
