package com.trading.app.data.repository

import com.trading.app.data.simulator.MarketSimulator
import com.trading.app.domain.model.Order
import com.trading.app.domain.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val simulator: MarketSimulator
) : OrderRepository {

    override suspend fun placeOrder(order: Order): Result<Order> = simulator.placeOrder(order)

    override suspend fun cancelOrder(orderId: String): Result<Order> = simulator.cancelOrder(orderId)

    override fun getOpenOrders(): Flow<List<Order>> = flow {
        while (true) {
            emit(simulator.getOpenOrders())
            delay(2000)
        }
    }

    override fun getOrderHistory(): Flow<List<Order>> = flow {
        while (true) {
            emit(simulator.getOrderHistory())
            delay(2000)
        }
    }

    override fun getOrderById(orderId: String): Flow<Order?> = flow {
        emit(simulator.getOrderHistory().find { it.id == orderId })
    }
}
