package com.trading.app.data

import org.junit.Assert.*
import org.junit.Test

class DataSourceConfigTest {

    @Test
    fun `default mode is HYBRID`() {
        assertEquals(DataSourceMode.HYBRID, DataSourceConfig.mode)
    }

    @Test
    fun `mode can be changed to SIMULATED`() {
        val original = DataSourceConfig.mode
        DataSourceConfig.mode = DataSourceMode.SIMULATED
        assertEquals(DataSourceMode.SIMULATED, DataSourceConfig.mode)
        DataSourceConfig.mode = original // restore
    }

    @Test
    fun `mode can be changed to LIVE`() {
        val original = DataSourceConfig.mode
        DataSourceConfig.mode = DataSourceMode.LIVE
        assertEquals(DataSourceMode.LIVE, DataSourceConfig.mode)
        DataSourceConfig.mode = original
    }

    @Test
    fun `all modes are defined`() {
        val modes = DataSourceMode.entries
        assertEquals(3, modes.size)
        assertTrue(modes.contains(DataSourceMode.SIMULATED))
        assertTrue(modes.contains(DataSourceMode.LIVE))
        assertTrue(modes.contains(DataSourceMode.HYBRID))
    }
}
