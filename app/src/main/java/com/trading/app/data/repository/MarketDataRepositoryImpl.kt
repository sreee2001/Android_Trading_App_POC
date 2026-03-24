package com.trading.app.data.repository

import com.trading.app.data.simulator.MarketSimulator
import com.trading.app.domain.model.*
import com.trading.app.domain.repository.MarketDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketDataRepositoryImpl @Inject constructor(
    private val simulator: MarketSimulator
) : MarketDataRepository {

    override fun getLiveQuote(symbol: String): Flow<StockQuote> = simulator.streamQuote(symbol)

    override fun getLiveQuotes(symbols: List<String>): Flow<List<StockQuote>> = simulator.streamQuotes(symbols)

    override fun getCandlestickData(symbol: String, interval: String): Flow<List<Candlestick>> =
        simulator.generateCandlesticks(symbol, interval)

    override fun getOrderBook(symbol: String): Flow<OrderBook> = simulator.generateOrderBook(symbol)

    override fun getMarketIndices(): Flow<List<MarketIndex>> = simulator.streamIndices()

    override fun getSectorPerformance(): Flow<List<SectorPerformance>> = simulator.streamSectorPerformance()

    override fun getTopMovers(): Flow<List<StockQuote>> = simulator.getTopMovers()

    override fun searchSymbols(query: String): Flow<List<StockQuote>> = flow {
        emit(simulator.searchSymbols(query))
    }
}
