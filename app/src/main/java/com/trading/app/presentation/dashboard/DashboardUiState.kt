package com.trading.app.presentation.dashboard

import com.trading.app.domain.model.MarketIndex
import com.trading.app.domain.model.SectorPerformance
import com.trading.app.domain.model.StockQuote

data class DashboardUiState(
    val indices: List<MarketIndex> = emptyList(),
    val topMovers: List<StockQuote> = emptyList(),
    val sectors: List<SectorPerformance> = emptyList(),
    val isLoading: Boolean = true
)
