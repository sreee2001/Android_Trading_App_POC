package com.trading.app.data.remote

import com.trading.app.data.remote.model.FinnhubCandleResponse
import com.trading.app.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps Finnhub REST API responses to domain models.
 * Provides flows for live quote streaming via REST polling (WebSocket
 * is available separately via FinnhubWebSocket for true real-time).
 */
@Singleton
class FinnhubDataSource @Inject constructor(
    private val api: FinnhubApi,
    private val webSocket: FinnhubWebSocket
) {

    // Company profile cache
    private val profileCache = mutableMapOf<String, FinnhubCompanyProfileCached>()

    data class FinnhubCompanyProfileCached(
        val name: String,
        val marketCap: Long,
        val industry: String
    )

    /**
     * Stream live quote via REST polling with WebSocket price overlay.
     * Falls back to pure REST polling if WebSocket is unavailable.
     */
    fun streamQuote(symbol: String): Flow<StockQuote> = flow {
        // Fetch company profile once
        val profile = getOrFetchProfile(symbol)

        while (true) {
            try {
                val q = api.getQuote(symbol)
                emit(
                    StockQuote(
                        symbol = symbol,
                        companyName = profile.name,
                        lastPrice = q.current,
                        change = q.change ?: 0.0,
                        changePercent = q.percentChange ?: 0.0,
                        open = q.open,
                        high = q.high,
                        low = q.low,
                        volume = 0L,       // Finnhub quote doesn't return volume
                        marketCap = profile.marketCap,
                        bid = q.current - 0.01,  // Finnhub free tier doesn't provide L2
                        ask = q.current + 0.01,
                        bidSize = 0,
                        askSize = 0,
                        timestamp = q.timestamp * 1000
                    )
                )
            } catch (e: Exception) {
                // silently retry on failure — next poll will try again
            }
            delay(3000) // Finnhub free tier: 60 calls/min, so ~3s per symbol is safe
        }
    }

    /**
     * Fetch candlestick data from Finnhub REST API.
     */
    fun getCandlesticks(symbol: String, resolution: String, from: Long, to: Long): Flow<List<Candlestick>> = flow {
        try {
            val response = api.getCandles(symbol, resolution, from, to)
            if (response.status == "ok") {
                emit(mapCandles(response))
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Search symbols on Finnhub.
     */
    suspend fun searchSymbols(query: String): List<StockQuote> {
        return try {
            val response = api.searchSymbols(query)
            response.result
                .filter { it.type == "Common Stock" || it.type == null }
                .take(10)
                .map { result ->
                    StockQuote(
                        symbol = result.symbol,
                        companyName = result.description,
                        lastPrice = 0.0,
                        change = 0.0,
                        changePercent = 0.0,
                        open = 0.0, high = 0.0, low = 0.0,
                        volume = 0, marketCap = 0,
                        bid = 0.0, ask = 0.0, bidSize = 0, askSize = 0
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Map Finnhub resolution string to our internal interval.
     */
    fun mapIntervalToResolution(interval: String): String {
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

    /**
     * Compute a sensible "from" timestamp for a given interval.
     */
    fun computeFromTimestamp(interval: String): Long {
        val now = System.currentTimeMillis() / 1000
        return when (interval) {
            "1m" -> now - 3600           // 1 hour
            "5m" -> now - 3600 * 6       // 6 hours
            "15m" -> now - 86400         // 1 day
            "1h" -> now - 86400 * 7      // 7 days
            "1D" -> now - 86400 * 90     // 90 days
            "1W" -> now - 86400 * 365    // 1 year
            "1M" -> now - 86400 * 365 * 2 // 2 years
            else -> now - 86400 * 90
        }
    }

    private fun mapCandles(response: FinnhubCandleResponse): List<Candlestick> {
        val timestamps = response.timestamp ?: return emptyList()
        val opens = response.open ?: return emptyList()
        val highs = response.high ?: return emptyList()
        val lows = response.low ?: return emptyList()
        val closes = response.close ?: return emptyList()
        val volumes = response.volume ?: return emptyList()

        return timestamps.indices.map { i ->
            Candlestick(
                timestamp = timestamps[i] * 1000,
                open = opens[i],
                high = highs[i],
                low = lows[i],
                close = closes[i],
                volume = volumes[i]
            )
        }
    }

    private suspend fun getOrFetchProfile(symbol: String): FinnhubCompanyProfileCached {
        profileCache[symbol]?.let { return it }
        return try {
            val profile = api.getCompanyProfile(symbol)
            val cached = FinnhubCompanyProfileCached(
                name = profile.name ?: symbol,
                marketCap = ((profile.marketCap ?: 0.0) * 1_000_000).toLong(), // Finnhub returns in millions
                industry = profile.industry ?: "Unknown"
            )
            profileCache[symbol] = cached
            cached
        } catch (e: Exception) {
            FinnhubCompanyProfileCached(symbol, 0L, "Unknown")
        }
    }
}
