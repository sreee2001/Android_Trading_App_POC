package com.trading.app.domain.repository

import com.trading.app.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlist(): Flow<List<WatchlistItem>>
    suspend fun addToWatchlist(symbol: String, companyName: String)
    suspend fun removeFromWatchlist(symbol: String)
    suspend fun isInWatchlist(symbol: String): Boolean
}
