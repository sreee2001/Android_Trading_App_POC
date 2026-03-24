package com.trading.app.data.repository

import com.trading.app.data.simulator.MarketSimulator
import com.trading.app.domain.model.WatchlistItem
import com.trading.app.domain.repository.WatchlistRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val simulator: MarketSimulator
) : WatchlistRepository {

    private val watchlist = mutableListOf(
        WatchlistItem("AAPL", "Apple Inc."),
        WatchlistItem("GOOGL", "Alphabet Inc."),
        WatchlistItem("MSFT", "Microsoft Corp."),
        WatchlistItem("TSLA", "Tesla Inc."),
        WatchlistItem("NVDA", "NVIDIA Corp."),
    )

    override fun getWatchlist(): Flow<List<WatchlistItem>> = flow {
        while (true) {
            val updated = watchlist.map { item ->
                val quote = simulator.generateQuote(item.symbol)
                item.copy(
                    lastPrice = quote.lastPrice,
                    change = quote.change,
                    changePercent = quote.changePercent,
                    sparklineData = generateSparkline(quote.lastPrice)
                )
            }
            emit(updated)
            delay(1500)
        }
    }

    override suspend fun addToWatchlist(symbol: String, companyName: String) {
        if (watchlist.none { it.symbol == symbol }) {
            watchlist.add(WatchlistItem(symbol, companyName))
        }
    }

    override suspend fun removeFromWatchlist(symbol: String) {
        watchlist.removeAll { it.symbol == symbol }
    }

    override suspend fun isInWatchlist(symbol: String): Boolean {
        return watchlist.any { it.symbol == symbol }
    }

    private fun generateSparkline(currentPrice: Double): List<Double> {
        var price = currentPrice * 0.98
        return (0..19).map {
            price += price * (kotlin.random.Random.nextDouble() - 0.48) * 0.01
            price
        }
    }
}
