package com.trading.app.presentation.stockdetail

import com.trading.app.domain.model.Candlestick
import com.trading.app.domain.model.OrderBook
import com.trading.app.domain.model.StockQuote

data class StockDetailUiState(
    val quote: StockQuote? = null,
    val candles: List<Candlestick> = emptyList(),
    val orderBook: OrderBook? = null,
    val selectedInterval: String = "1D",
    val isLoading: Boolean = true
)
