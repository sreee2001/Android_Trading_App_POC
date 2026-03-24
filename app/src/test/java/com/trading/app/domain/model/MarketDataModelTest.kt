package com.trading.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class MarketDataModelTest {

    @Test
    fun `MarketIndex isPositive returns true for positive change`() {
        val index = MarketIndex("S&P 500", "SPX", 5000.0, 25.0, 0.5)
        assertTrue(index.isPositive)
    }

    @Test
    fun `MarketIndex isPositive returns true for zero change`() {
        val index = MarketIndex("S&P 500", "SPX", 5000.0, 0.0, 0.0)
        assertTrue(index.isPositive)
    }

    @Test
    fun `MarketIndex isPositive returns false for negative change`() {
        val index = MarketIndex("S&P 500", "SPX", 5000.0, -30.0, -0.6)
        assertFalse(index.isPositive)
    }

    @Test
    fun `SectorPerformance isPositive returns true for positive change`() {
        val sector = SectorPerformance("Technology", 2.5)
        assertTrue(sector.isPositive)
    }

    @Test
    fun `SectorPerformance isPositive returns false for negative change`() {
        val sector = SectorPerformance("Energy", -1.3)
        assertFalse(sector.isPositive)
    }

    @Test
    fun `WatchlistItem isPositive returns true for positive change`() {
        val item = WatchlistItem("AAPL", "Apple", 150.0, 2.0, 1.3)
        assertTrue(item.isPositive)
    }

    @Test
    fun `WatchlistItem isPositive returns false for negative change`() {
        val item = WatchlistItem("AAPL", "Apple", 150.0, -3.0, -2.0)
        assertFalse(item.isPositive)
    }
}
