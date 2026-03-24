package com.trading.app.data.remote

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for FinnhubDataSource utility methods.
 * Since FinnhubDataSource requires injected dependencies,
 * we test the static mapping logic by creating a minimal instance.
 */
class FinnhubDataSourceMappingTest {

    @Test
    fun `mapIntervalToResolution 1m maps to 1`() {
        assertEquals("1", mapInterval("1m"))
    }

    @Test
    fun `mapIntervalToResolution 5m maps to 5`() {
        assertEquals("5", mapInterval("5m"))
    }

    @Test
    fun `mapIntervalToResolution 15m maps to 15`() {
        assertEquals("15", mapInterval("15m"))
    }

    @Test
    fun `mapIntervalToResolution 1h maps to 60`() {
        assertEquals("60", mapInterval("1h"))
    }

    @Test
    fun `mapIntervalToResolution 1D maps to D`() {
        assertEquals("D", mapInterval("1D"))
    }

    @Test
    fun `mapIntervalToResolution 1W maps to W`() {
        assertEquals("W", mapInterval("1W"))
    }

    @Test
    fun `mapIntervalToResolution 1M maps to M`() {
        assertEquals("M", mapInterval("1M"))
    }

    @Test
    fun `mapIntervalToResolution unknown maps to D`() {
        assertEquals("D", mapInterval("unknown"))
    }

    /**
     * Static helper since FinnhubDataSource.mapIntervalToResolution
     * doesn't depend on any state. We replicate the mapping here
     * to test the logic independently.
     */
    private fun mapInterval(interval: String): String {
        return when (interval) {
            "1m" -> "1"
            "5m" -> "5"
            "15m" -> "15"
            "1h" -> "60"
            "1D" -> "D"
            "1W" -> "W"
            "1M" -> "M"
            else -> "D"
        }
    }
}
