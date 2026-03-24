package com.trading.app.domain.repository

import com.trading.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(order: Order): Result<Order>
    suspend fun cancelOrder(orderId: String): Result<Order>
    fun getOpenOrders(): Flow<List<Order>>
    fun getOrderHistory(): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
}
