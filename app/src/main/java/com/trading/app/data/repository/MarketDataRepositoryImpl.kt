package com.trading.app.data.repository

import android.util.Log
import com.trading.app.data.DataSourceConfig
import com.trading.app.data.DataSourceMode
import com.trading.app.data.remote.FinnhubDataSource
import com.trading.app.data.simulator.MarketSimulator
import com.trading.app.domain.model.*
import com.trading.app.domain.repository.MarketDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Market data repository implementing the Strategy Pattern.
 * Routes to Finnhub (live) or MarketSimulator (offline) based on DataSourceConfig.
 *
 * In HYBRID mode: attempts Finnhub first, falls back to simulator on any error.
 * This ensures the app always has data — a critical requirement for trading UIs.
 */
@Singleton
class MarketDataRepositoryImpl @Inject constructor(
    private val simulator: MarketSimulator,
    private val finnhubDataSource: FinnhubDataSource
) : MarketDataRepository {

    private val tag = "MarketDataRepo"

    override fun getLiveQuote(symbol: String): Flow<StockQuote> {
        return when (DataSourceConfig.mode) {
            DataSourceMode.SIMULATED -> simulator.streamQuote(symbol)
            DataSourceMode.LIVE -> finnhubDataSource.streamQuote(symbol)
            DataSourceMode.HYBRID -> finnhubDataSource.streamQuote(symbol)
                .catch { e ->
                    Log.w(tag, "Finnhub quote failed for $symbol, falling back to simulator: ${e.message}")
                    emitAll(simulator.streamQuote(symbol))
                }
        }
    }

    override fun getLiveQuotes(symbols: List<String>): Flow<List<StockQuote>> {
        // For batch quotes, simulator is more efficient (single flow)
        // Finnhub free tier doesn't have a batch quote endpoint
        return when (DataSourceConfig.mode) {
            DataSourceMode.SIMULATED -> simulator.streamQuotes(symbols)
            DataSourceMode.LIVE, DataSourceMode.HYBRID -> simulator.streamQuotes(symbols)
        }
    }

    override fun getCandlestickData(symbol: String, interval: String): Flow<List<Candlestick>> {
        return when (DataSourceConfig.mode) {
            DataSourceMode.SIMULATED -> simulator.generateCandlesticks(symbol, interval)
            DataSourceMode.LIVE -> {
                val resolution = finnhubDataSource.mapIntervalToResolution(interval)
                val from = finnhubDataSource.computeFromTimestamp(interval)
                val to = System.currentTimeMillis() / 1000
                finnhubDataSource.getCandlesticks(symbol, resolution, from, to)
            }
            DataSourceMode.HYBRID -> {
                val resolution = finnhubDataSource.mapIntervalToResolution(interval)
                val from = finnhubDataSource.computeFromTimestamp(interval)
                val to = System.currentTimeMillis() / 1000
                finnhubDataSource.getCandlesticks(symbol, resolution, from, to)
                    .onEach { candles ->
                        if (candles.isEmpty()) throw Exception("No candle data from Finnhub")
                    }
                    .catch { e ->
                        Log.w(tag, "Finnhub candles failed for $symbol, falling back: ${e.message}")
                        emitAll(simulator.generateCandlesticks(symbol, interval))
                    }
            }
        }
    }

    override fun getOrderBook(symbol: String): Flow<OrderBook> {
        // Finnhub free tier doesn't provide Level 2 data — always use simulator
        return simulator.generateOrderBook(symbol)
    }

    override fun getMarketIndices(): Flow<List<MarketIndex>> {
        // Finnhub free tier doesn't have index quotes — use simulator
        return simulator.streamIndices()
    }

    override fun getSectorPerformance(): Flow<List<SectorPerformance>> {
        // Sector perf is derived/simulated
        return simulator.streamSectorPerformance()
    }

    override fun getTopMovers(): Flow<List<StockQuote>> {
        return simulator.getTopMovers()
    }

    override fun searchSymbols(query: String): Flow<List<StockQuote>> {
        return when (DataSourceConfig.mode) {
            DataSourceMode.SIMULATED -> flow { emit(simulator.searchSymbols(query)) }
            DataSourceMode.LIVE -> flow { emit(finnhubDataSource.searchSymbols(query)) }
            DataSourceMode.HYBRID -> flow {
                try {
                    val results = finnhubDataSource.searchSymbols(query)
                    if (results.isNotEmpty()) {
                        emit(results)
                    } else {
                        emit(simulator.searchSymbols(query))
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Finnhub search failed, falling back: ${e.message}")
                    emit(simulator.searchSymbols(query))
                }
            }
        }
    }

    /**
     * Helper to emit all items from another flow within a catch block.
     */
    private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(flow: Flow<T>) {
        flow.collect { emit(it) }
    }
}
