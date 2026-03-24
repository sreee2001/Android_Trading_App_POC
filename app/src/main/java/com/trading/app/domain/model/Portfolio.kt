package com.trading.app.domain.model

data class Position(
    val symbol: String,
    val companyName: String,
    val quantity: Int,
    val averageCost: Double,
    val currentPrice: Double
) {
    val marketValue: Double get() = quantity * currentPrice
    val costBasis: Double get() = quantity * averageCost
    val unrealizedPnL: Double get() = marketValue - costBasis
    val unrealizedPnLPercent: Double get() = if (costBasis != 0.0) (unrealizedPnL / costBasis) * 100 else 0.0
    val isProfit: Boolean get() = unrealizedPnL >= 0
}

data class Portfolio(
    val positions: List<Position>,
    val cashBalance: Double,
    val dayPnL: Double
) {
    val totalMarketValue: Double get() = positions.sumOf { it.marketValue }
    val totalCostBasis: Double get() = positions.sumOf { it.costBasis }
    val totalUnrealizedPnL: Double get() = positions.sumOf { it.unrealizedPnL }
    val totalValue: Double get() = totalMarketValue + cashBalance
    val buyingPower: Double get() = cashBalance
}
