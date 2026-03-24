package com.trading.app.data.remote

import android.util.Log
import com.google.gson.Gson
import com.trading.app.data.remote.model.FinnhubTrade
import com.trading.app.data.remote.model.FinnhubTradeMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the Finnhub WebSocket connection for real-time price streaming.
 *
 * Finnhub WebSocket URL: wss://ws.finnhub.io?token=<API_KEY>
 *
 * Subscribe by sending: {"type":"subscribe","symbol":"AAPL"}
 * Unsubscribe by sending: {"type":"unsubscribe","symbol":"AAPL"}
 */
@Singleton
class FinnhubWebSocket @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val apiKey: String
) {
    private val gson = Gson()
    private val tag = "FinnhubWebSocket"

    /**
     * Returns a Flow that emits trade updates for the given symbols.
     * The WebSocket connection is opened when the flow is collected and closed when cancelled.
     */
    fun streamTrades(symbols: List<String>): Flow<FinnhubTrade> = callbackFlow {
        val request = Request.Builder()
            .url("wss://ws.finnhub.io?token=$apiKey")
            .build()

        val ws = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "WebSocket connected, subscribing to ${symbols.size} symbols")
                symbols.forEach { symbol ->
                    val msg = """{"type":"subscribe","symbol":"$symbol"}"""
                    webSocket.send(msg)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, FinnhubTradeMessage::class.java)
                    if (message.type == "trade" && message.data != null) {
                        message.data.forEach { trade ->
                            trySend(trade)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing WebSocket message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket failure: ${t.message}", t)
                close(t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closing: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closed: $code $reason")
                close()
            }
        })

        awaitClose {
            Log.d(tag, "Flow cancelled, unsubscribing and closing WebSocket")
            symbols.forEach { symbol ->
                val msg = """{"type":"unsubscribe","symbol":"$symbol"}"""
                ws.send(msg)
            }
            ws.close(1000, "Flow cancelled")
        }
    }
}
