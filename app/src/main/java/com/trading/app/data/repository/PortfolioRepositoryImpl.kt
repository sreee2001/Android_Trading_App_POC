package com.trading.app.data.repository

import com.trading.app.data.simulator.MarketSimulator
import com.trading.app.domain.model.Portfolio
import com.trading.app.domain.model.Position
import com.trading.app.domain.repository.PortfolioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val simulator: MarketSimulator
) : PortfolioRepository {

    override fun getPortfolio(): Flow<Portfolio> = flow {
        while (true) {
            emit(simulator.getPortfolio())
            delay(2000)
        }
    }

    override fun getPositions(): Flow<List<Position>> = flow {
        while (true) {
            emit(simulator.getPositions())
            delay(2000)
        }
    }

    override fun getPosition(symbol: String): Flow<Position?> = flow {
        while (true) {
            emit(simulator.getPositions().find { it.symbol == symbol })
            delay(2000)
        }
    }
}
