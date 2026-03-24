package com.trading.app.data

/**
 * Controls whether the app uses live Finnhub data or the local simulator.
 *
 * In production: switch to LIVE and provide a real Finnhub API key in NetworkModule.
 * For demo/POC: SIMULATED runs entirely offline with realistic fake data.
 * HYBRID uses live data when available and falls back to simulator on failure.
 */
enum class DataSourceMode {
    /** Use only the local market simulator — no network calls */
    SIMULATED,
    /** Use Finnhub API for market data — requires valid API key and network */
    LIVE,
    /** Try Finnhub first, fall back to simulator on failure */
    HYBRID
}

/**
 * Current data source configuration.
 * Change this to LIVE or HYBRID when a real Finnhub API key is configured.
 */
object DataSourceConfig {
    var mode: DataSourceMode = DataSourceMode.HYBRID
}
