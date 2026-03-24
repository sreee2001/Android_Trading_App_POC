package com.trading.app.domain.repository

import com.trading.app.domain.model.Portfolio
import com.trading.app.domain.model.Position
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    fun getPortfolio(): Flow<Portfolio>
    fun getPositions(): Flow<List<Position>>
    fun getPosition(symbol: String): Flow<Position?>
}
