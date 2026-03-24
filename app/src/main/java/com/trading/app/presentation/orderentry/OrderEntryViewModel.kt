package com.trading.app.presentation.orderentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.app.domain.model.*
import com.trading.app.domain.repository.MarketDataRepository
import com.trading.app.domain.repository.OrderRepository
import com.trading.app.domain.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OrderEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val marketDataRepository: MarketDataRepository,
    private val orderRepository: OrderRepository,
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val symbol: String = savedStateHandle.get<String>("symbol") ?: "AAPL"

    private val _uiState = MutableStateFlow(OrderEntryUiState(symbol = symbol))
    val uiState: StateFlow<OrderEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            marketDataRepository.getLiveQuote(symbol).collect { quote ->
                _uiState.update {
                    it.copy(
                        currentPrice = quote.lastPrice,
                        companyName = quote.companyName
                    )
                }
            }
        }
        viewModelScope.launch {
            portfolioRepository.getPortfolio().collect { portfolio ->
                val pos = portfolio.positions.find { it.symbol == symbol }
                _uiState.update {
                    it.copy(buyingPower = portfolio.buyingPower, currentPosition = pos?.quantity ?: 0)
                }
            }
        }
    }

    fun setSide(side: OrderSide) = _uiState.update { it.copy(side = side) }
    fun setType(type: OrderType) = _uiState.update { it.copy(type = type) }
    fun setTimeInForce(tif: TimeInForce) = _uiState.update { it.copy(timeInForce = tif) }
    fun setQuantity(qty: String) = _uiState.update { it.copy(quantity = qty) }
    fun setLimitPrice(price: String) = _uiState.update { it.copy(limitPrice = price) }
    fun setStopPrice(price: String) = _uiState.update { it.copy(stopPrice = price) }

    fun setQuantityPercent(percent: Double) {
        val state = _uiState.value
        val qty = if (state.side == OrderSide.BUY) {
            ((state.buyingPower * percent) / state.currentPrice).toInt()
        } else {
            (state.currentPosition * percent).toInt()
        }
        _uiState.update { it.copy(quantity = qty.toString()) }
    }

    fun showConfirmation() {
        val state = _uiState.value
        // Validation
        val qty = state.quantity.toIntOrNull()
        if (qty == null || qty <= 0) {
            _uiState.update { it.copy(error = "Enter a valid quantity") }
            return
        }
        if (state.side == OrderSide.BUY && state.estimatedCost > state.buyingPower) {
            _uiState.update { it.copy(error = "Insufficient buying power") }
            return
        }
        if (state.side == OrderSide.SELL && qty > state.currentPosition) {
            _uiState.update { it.copy(error = "Insufficient shares (you own ${state.currentPosition})") }
            return
        }
        if (state.type == OrderType.LIMIT || state.type == OrderType.STOP_LIMIT) {
            if (state.limitPrice.toDoubleOrNull() == null) {
                _uiState.update { it.copy(error = "Enter a valid limit price") }
                return
            }
        }
        _uiState.update { it.copy(showConfirmation = true, error = null) }
    }

    fun dismissConfirmation() = _uiState.update { it.copy(showConfirmation = false) }
    fun clearResult() = _uiState.update { it.copy(orderResult = null, error = null) }

    fun submitOrder() {
        val state = _uiState.value
        val order = Order(
            id = UUID.randomUUID().toString(),
            symbol = state.symbol,
            side = state.side,
            type = state.type,
            timeInForce = state.timeInForce,
            quantity = state.quantity.toIntOrNull() ?: 0,
            price = state.limitPrice.toDoubleOrNull(),
            stopPrice = state.stopPrice.toDoubleOrNull()
        )
        viewModelScope.launch {
            val result = orderRepository.placeOrder(order)
            result.fold(
                onSuccess = { filled ->
                    _uiState.update {
                        it.copy(
                            showConfirmation = false,
                            orderResult = "${filled.side} ${filled.filledQuantity} ${filled.symbol} @ ${filled.avgFillPrice?.let { p -> String.format("%.2f", p) } ?: "MKT"} — ${filled.status}",
                            quantity = "",
                            limitPrice = "",
                            stopPrice = ""
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(showConfirmation = false, error = e.message) }
                }
            )
        }
    }
}
