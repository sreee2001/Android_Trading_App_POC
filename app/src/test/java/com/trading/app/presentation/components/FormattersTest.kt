package com.trading.app.presentation.components

import org.junit.Assert.*
import org.junit.Test

class FormattersTest {

    @Test
    fun `formatPrice formats with two decimals and commas`() {
        assertEquals("1,234.56", Formatters.formatPrice(1234.56))
    }

    @Test
    fun `formatPrice handles small numbers`() {
        assertEquals("0.50", Formatters.formatPrice(0.5))
    }

    @Test
    fun `formatPrice handles zero`() {
        assertEquals("0.00", Formatters.formatPrice(0.0))
    }

    @Test
    fun `formatChange positive has plus sign`() {
        val result = Formatters.formatChange(5.25)
        assertTrue(result.startsWith("+"))
    }

    @Test
    fun `formatChange negative has minus sign`() {
        val result = Formatters.formatChange(-3.50)
        assertTrue(result.startsWith("-"))
    }

    @Test
    fun `formatPercent positive has plus sign and percent symbol`() {
        val result = Formatters.formatPercent(2.5)
        assertTrue(result.contains("+"))
        assertTrue(result.contains("%"))
    }

    @Test
    fun `formatPercent negative has minus sign and percent symbol`() {
        val result = Formatters.formatPercent(-1.8)
        assertTrue(result.contains("-"))
        assertTrue(result.contains("%"))
    }

    @Test
    fun `formatPercent zero shows plus zero`() {
        val result = Formatters.formatPercent(0.0)
        assertTrue(result.contains("0.00%"))
    }

    @Test
    fun `formatVolume millions`() {
        assertEquals("5.0M", Formatters.formatVolume(5_000_000))
    }

    @Test
    fun `formatVolume billions`() {
        assertEquals("2.5B", Formatters.formatVolume(2_500_000_000))
    }

    @Test
    fun `formatVolume thousands`() {
        assertEquals("500.0K", Formatters.formatVolume(500_000))
    }

    @Test
    fun `formatVolume small number no suffix`() {
        assertEquals("999", Formatters.formatVolume(999))
    }

    @Test
    fun `formatMarketCap trillions`() {
        assertEquals("2.5T", Formatters.formatMarketCap(2_500_000_000_000))
    }

    @Test
    fun `formatMarketCap billions`() {
        assertEquals("500.0B", Formatters.formatMarketCap(500_000_000_000))
    }

    @Test
    fun `formatMarketCap millions`() {
        assertEquals("250.0M", Formatters.formatMarketCap(250_000_000))
    }

    @Test
    fun `formatCurrency includes dollar sign`() {
        val result = Formatters.formatCurrency(1234.56)
        assertTrue(result.startsWith("$"))
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun `formatTimestamp produces non-empty string`() {
        val ts = System.currentTimeMillis()
        val result = Formatters.formatTimestamp(ts)
        assertTrue(result.isNotEmpty())
        // HH:mm:ss format should have colons
        assertTrue(result.contains(":"))
    }

    @Test
    fun `formatTimestamp with custom pattern`() {
        val ts = System.currentTimeMillis()
        val result = Formatters.formatTimestamp(ts, "yyyy-MM-dd")
        assertTrue(result.contains("-"))
        assertTrue(result.length == 10) // yyyy-MM-dd
    }
}
