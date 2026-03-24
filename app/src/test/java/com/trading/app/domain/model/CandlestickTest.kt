package com.trading.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class CandlestickTest {

    @Test
    fun `isBullish returns true when close greater than or equal to open`() {
        val candle = Candlestick(0, 100.0, 110.0, 95.0, 105.0, 1000)
        assertTrue(candle.isBullish)
    }

    @Test
    fun `isBullish returns true when close equals open`() {
        val candle = Candlestick(0, 100.0, 110.0, 95.0, 100.0, 1000)
        assertTrue(candle.isBullish)
    }

    @Test
    fun `isBullish returns false when close less than open`() {
        val candle = Candlestick(0, 100.0, 110.0, 90.0, 95.0, 1000)
        assertFalse(candle.isBullish)
    }

    @Test
    fun `body is absolute difference between close and open`() {
        val candle = Candlestick(0, 100.0, 110.0, 90.0, 95.0, 1000)
        assertEquals(5.0, candle.body, 0.001)
    }

    @Test
    fun `body is correct for bullish candle`() {
        val candle = Candlestick(0, 100.0, 115.0, 95.0, 110.0, 1000)
        assertEquals(10.0, candle.body, 0.001)
    }

    @Test
    fun `upperWick is high minus max of open and close`() {
        val candle = Candlestick(0, 100.0, 115.0, 90.0, 110.0, 1000)
        assertEquals(5.0, candle.upperWick, 0.001)
    }

    @Test
    fun `lowerWick is min of open and close minus low`() {
        val candle = Candlestick(0, 100.0, 115.0, 90.0, 110.0, 1000)
        assertEquals(10.0, candle.lowerWick, 0.001)
    }

    @Test
    fun `doji candle has zero body`() {
        val candle = Candlestick(0, 100.0, 110.0, 90.0, 100.0, 500)
        assertEquals(0.0, candle.body, 0.001)
        assertEquals(10.0, candle.upperWick, 0.001)
        assertEquals(10.0, candle.lowerWick, 0.001)
    }
}
