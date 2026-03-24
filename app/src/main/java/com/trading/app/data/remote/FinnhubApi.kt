package com.trading.app.data.remote

import com.trading.app.data.remote.model.*
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Finnhub REST API service.
 * Base URL: https://finnhub.io/api/v1/
 * All endpoints require the "token" query parameter (added by interceptor).
 */
interface FinnhubApi {

    /**
     * Get real-time quote data for US stocks.
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String
    ): FinnhubQuoteResponse

    /**
     * Get candlestick (OHLCV) data.
     * @param resolution Supported: 1, 5, 15, 30, 60, D, W, M
     * @param from UNIX timestamp start
     * @param to UNIX timestamp end
     */
    @GET("stock/candle")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): FinnhubCandleResponse

    /**
     * Get company profile.
     */
    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String
    ): FinnhubCompanyProfile

    /**
     * Search for symbols by query.
     */
    @GET("search")
    suspend fun searchSymbols(
        @Query("q") query: String
    ): FinnhubSearchResponse
}
