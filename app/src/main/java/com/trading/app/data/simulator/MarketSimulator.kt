package com.trading.app.data.simulator

import com.trading.app.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*
import kotlin.random.Random

@Singleton
class MarketSimulator @Inject constructor() {

    private val javaRandom = java.util.Random()

    private val stocks = mapOf(
        "AAPL" to StockSeed("Apple Inc.", 189.50, 2_980_000_000_000),
        "GOOGL" to StockSeed("Alphabet Inc.", 141.80, 1_780_000_000_000),
        "MSFT" to StockSeed("Microsoft Corp.", 378.90, 2_810_000_000_000),
        "AMZN" to StockSeed("Amazon.com Inc.", 178.25, 1_860_000_000_000),
        "TSLA" to StockSeed("Tesla Inc.", 248.50, 790_000_000_000),
        "NVDA" to StockSeed("NVIDIA Corp.", 875.30, 2_150_000_000_000),
        "META" to StockSeed("Meta Platforms Inc.", 505.75, 1_290_000_000_000),
        "JPM" to StockSeed("JPMorgan Chase & Co.", 195.40, 565_000_000_000),
        "V" to StockSeed("Visa Inc.", 279.60, 575_000_000_000),
        "JNJ" to StockSeed("Johnson & Johnson", 156.80, 378_000_000_000),
        "WMT" to StockSeed("Walmart Inc.", 165.20, 445_000_000_000),
        "BAC" to StockSeed("Bank of America Corp.", 35.80, 280_000_000_000),
        "DIS" to StockSeed("The Walt Disney Co.", 112.40, 205_000_000_000),
        "NFLX" to StockSeed("Netflix Inc.", 628.90, 272_000_000_000),
        "AMD" to StockSeed("Advanced Micro Devices", 178.60, 288_000_000_000),
    )

    private val currentPrices = mutableMapOf<String, Double>().apply {
        stocks.forEach { (symbol, seed) -> put(symbol, seed.basePrice) }
    }

    private val openPrices = mutableMapOf<String, Double>().apply {
        stocks.forEach { (symbol, seed) -> put(symbol, seed.basePrice * (1 + Random.nextDouble(-0.01, 0.01))) }
    }

    private val indices = listOf(
        MarketIndex("S&P 500", "SPX", 5_021.84, 0.0, 0.0),
        MarketIndex("NASDAQ", "IXIC", 15_996.82, 0.0, 0.0),
        MarketIndex("DOW", "DJI", 39_131.53, 0.0, 0.0),
    )

    private val sectors = listOf(
        "Technology", "Healthcare", "Financials", "Consumer Discretionary",
        "Communication Services", "Industrials", "Consumer Staples",
        "Energy", "Utilities", "Real Estate", "Materials"
    )

    // --- Portfolio state ---
    private var cashBalance = 100_000.0
    private val positions = mutableMapOf<String, MutablePosition>()
    private val orders = mutableListOf<Order>()
    private var orderIdCounter = 1

    data class StockSeed(val name: String, val basePrice: Double, val marketCap: Long)
    data class MutablePosition(val symbol: String, val companyName: String, var quantity: Int, var averageCost: Double)

    // ---- Price simulation -----

    private fun simulatePriceMove(symbol: String): Double {
        val current = currentPrices[symbol] ?: return 0.0
        val volatility = when (symbol) {
            "TSLA", "NVDA", "AMD" -> 0.003
            "AAPL", "MSFT", "GOOGL" -> 0.0015
            else -> 0.002
        }
        val drift = Random.nextDouble(-0.0001, 0.0002)
        val shock = javaRandom.nextGaussian() * volatility
        val newPrice = current * (1 + drift + shock)
        val clamped = maxOf(newPrice, current * 0.95)  // prevent crash >5%
        currentPrices[symbol] = clamped
        return clamped
    }

    fun getAllSymbols(): List<String> = stocks.keys.toList()

    fun getStockName(symbol: String): String = stocks[symbol]?.name ?: symbol

    fun generateQuote(symbol: String): StockQuote {
        val price = simulatePriceMove(symbol)
        val seed = stocks[symbol] ?: return StockQuote(symbol, symbol, price, 0.0, 0.0, price, price, price, 0, 0, price, price, 0, 0)
        val open = openPrices[symbol] ?: price
        val change = price - open
        val changePct = if (open != 0.0) (change / open) * 100 else 0.0
        val high = maxOf(open, price) * (1 + Random.nextDouble(0.0, 0.005))
        val low = minOf(open, price) * (1 - Random.nextDouble(0.0, 0.005))
        val spread = price * Random.nextDouble(0.0001, 0.001)
        return StockQuote(
            symbol = symbol,
            companyName = seed.name,
            lastPrice = price,
            change = change,
            changePercent = changePct,
            open = open,
            high = high,
            low = low,
            volume = Random.nextLong(1_000_000, 80_000_000),
            marketCap = seed.marketCap,
            bid = price - spread / 2,
            ask = price + spread / 2,
            bidSize = Random.nextInt(100, 5000),
            askSize = Random.nextInt(100, 5000)
        )
    }

    fun streamQuote(symbol: String): Flow<StockQuote> = flow {
        while (true) {
            emit(generateQuote(symbol))
            delay(Random.nextLong(500, 1500))
        }
    }

    fun streamQuotes(symbols: List<String>): Flow<List<StockQuote>> = flow {
        while (true) {
            emit(symbols.map { generateQuote(it) })
            delay(1000)
        }
    }

    fun streamIndices(): Flow<List<MarketIndex>> = flow {
        val baseValues = indices.map { it.value }.toMutableList()
        while (true) {
            val result = indices.mapIndexed { i, index ->
                val change = baseValues[i] * Random.nextDouble(-0.002, 0.002)
                baseValues[i] += change
                val totalChange = baseValues[i] - index.value
                index.copy(
                    value = baseValues[i],
                    change = totalChange,
                    changePercent = (totalChange / index.value) * 100
                )
            }
            emit(result)
            delay(2000)
        }
    }

    fun streamSectorPerformance(): Flow<List<SectorPerformance>> = flow {
        while (true) {
            emit(sectors.map { SectorPerformance(it, Random.nextDouble(-3.0, 3.5)) })
            delay(5000)
        }
    }

    fun generateCandlesticks(symbol: String, interval: String): Flow<List<Candlestick>> = flow {
        val count = when (interval) {
            "1m", "5m" -> 60
            "15m" -> 48
            "1h" -> 24
            "1D" -> 90
            "1W" -> 52
            "1M" -> 24
            else -> 60
        }
        val basePrice = currentPrices[symbol] ?: 100.0
        var price = basePrice * 0.95
        val now = System.currentTimeMillis()
        val intervalMs = when (interval) {
            "1m" -> 60_000L
            "5m" -> 300_000L
            "15m" -> 900_000L
            "1h" -> 3_600_000L
            "1D" -> 86_400_000L
            "1W" -> 604_800_000L
            "1M" -> 2_592_000_000L
            else -> 60_000L
        }
        val candles = (0 until count).map { i ->
            val open = price
            val change = price * javaRandom.nextGaussian() * 0.015
            val close = price + change
            val high = maxOf(open, close) + abs(price * javaRandom.nextGaussian() * 0.005)
            val low = minOf(open, close) - abs(price * javaRandom.nextGaussian() * 0.005)
            price = close
            Candlestick(
                timestamp = now - (count - i) * intervalMs,
                open = open,
                high = high,
                low = low,
                close = close,
                volume = Random.nextLong(500_000, 10_000_000)
            )
        }
        emit(candles)

        // Continuously add new candles
        while (true) {
            delay(if (interval == "1m") 3000 else 5000)
            val last = price
            val change = last * javaRandom.nextGaussian() * 0.008
            val close = last + change
            val high = maxOf(last, close) + abs(last * javaRandom.nextGaussian() * 0.003)
            val low = minOf(last, close) - abs(last * javaRandom.nextGaussian() * 0.003)
            price = close
            val newCandle = Candlestick(
                timestamp = System.currentTimeMillis(),
                open = last, high = high, low = low, close = close,
                volume = Random.nextLong(500_000, 10_000_000)
            )
            emit(candles + newCandle)
        }
    }

    fun generateOrderBook(symbol: String): Flow<OrderBook> = flow {
        while (true) {
            val mid = currentPrices[symbol] ?: 100.0
            val tickSize = mid * 0.0001
            val bids = (1..15).map { i ->
                OrderBookEntry(
                    price = mid - i * tickSize * Random.nextDouble(1.0, 3.0),
                    quantity = Random.nextInt(100, 10000)
                )
            }.sortedByDescending { it.price }
            val asks = (1..15).map { i ->
                OrderBookEntry(
                    price = mid + i * tickSize * Random.nextDouble(1.0, 3.0),
                    quantity = Random.nextInt(100, 10000)
                )
            }.sortedBy { it.price }
            emit(OrderBook(symbol, bids, asks))
            delay(800)
        }
    }

    fun getTopMovers(): Flow<List<StockQuote>> = flow {
        while (true) {
            val quotes = stocks.keys.map { generateQuote(it) }
                .sortedByDescending { abs(it.changePercent) }
                .take(10)
            emit(quotes)
            delay(3000)
        }
    }

    fun searchSymbols(query: String): List<StockQuote> {
        val q = query.uppercase()
        return stocks.filter { (symbol, seed) ->
            symbol.contains(q) || seed.name.uppercase().contains(q)
        }.map { (symbol, _) -> generateQuote(symbol) }
    }

    // ---- Order & Portfolio ----

    fun placeOrder(order: Order): Result<Order> {
        val seed = stocks[order.symbol] ?: return Result.failure(Exception("Unknown symbol"))
        val price = currentPrices[order.symbol] ?: return Result.failure(Exception("No price"))

        // Validation
        if (order.quantity <= 0) return Result.failure(Exception("Quantity must be positive"))
        if (order.side == OrderSide.BUY) {
            val cost = order.quantity * (order.price ?: price)
            if (cost > cashBalance) return Result.failure(Exception("Insufficient buying power"))
        }
        if (order.side == OrderSide.SELL) {
            val held = positions[order.symbol]?.quantity ?: 0
            if (order.quantity > held) return Result.failure(Exception("Insufficient shares"))
        }

        val fillPrice = when (order.type) {
            OrderType.MARKET -> price
            OrderType.LIMIT -> order.price ?: price
            else -> order.price ?: price
        }

        val filledOrder = order.copy(
            id = "ORD-${orderIdCounter++}",
            status = OrderStatus.FILLED,
            filledQuantity = order.quantity,
            avgFillPrice = fillPrice,
            updatedAt = System.currentTimeMillis()
        )

        // Update portfolio
        if (order.side == OrderSide.BUY) {
            cashBalance -= order.quantity * fillPrice
            val existing = positions[order.symbol]
            if (existing != null) {
                val totalQty = existing.quantity + order.quantity
                val totalCost = existing.quantity * existing.averageCost + order.quantity * fillPrice
                existing.quantity = totalQty
                existing.averageCost = totalCost / totalQty
            } else {
                positions[order.symbol] = MutablePosition(order.symbol, seed.name, order.quantity, fillPrice)
            }
        } else {
            cashBalance += order.quantity * fillPrice
            val existing = positions[order.symbol]
            if (existing != null) {
                existing.quantity -= order.quantity
                if (existing.quantity <= 0) positions.remove(order.symbol)
            }
        }

        orders.add(filledOrder)
        return Result.success(filledOrder)
    }

    fun cancelOrder(orderId: String): Result<Order> {
        val idx = orders.indexOfFirst { it.id == orderId && it.isActive }
        if (idx == -1) return Result.failure(Exception("Order not found or not cancellable"))
        val cancelled = orders[idx].copy(status = OrderStatus.CANCELLED, updatedAt = System.currentTimeMillis())
        orders[idx] = cancelled
        return Result.success(cancelled)
    }

    fun getOpenOrders(): List<Order> = orders.filter { it.isActive }
    fun getOrderHistory(): List<Order> = orders.toList()

    fun getPortfolio(): Portfolio {
        val positionsList = positions.values.map { mp ->
            Position(
                symbol = mp.symbol,
                companyName = mp.companyName,
                quantity = mp.quantity,
                averageCost = mp.averageCost,
                currentPrice = currentPrices[mp.symbol] ?: mp.averageCost
            )
        }
        val dayPnL = positionsList.sumOf {
            val open = openPrices[it.symbol] ?: it.currentPrice
            (it.currentPrice - open) * it.quantity
        }
        return Portfolio(positionsList, cashBalance, dayPnL)
    }

    fun getPositions(): List<Position> {
        return positions.values.map { mp ->
            Position(mp.symbol, mp.companyName, mp.quantity, mp.averageCost, currentPrices[mp.symbol] ?: mp.averageCost)
        }
    }

    fun getCashBalance(): Double = cashBalance
}
