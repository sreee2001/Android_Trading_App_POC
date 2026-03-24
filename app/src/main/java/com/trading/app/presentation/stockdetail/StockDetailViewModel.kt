package com.trading.app.presentation.stockdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.app.domain.repository.MarketDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val marketDataRepository: MarketDataRepository
) : ViewModel() {

    val symbol: String = savedStateHandle.get<String>("symbol") ?: "AAPL"

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    private var candleJob: Job? = null

    init {
        loadQuote()
        loadCandles("1D")
        loadOrderBook()
    }

    private fun loadQuote() {
        viewModelScope.launch {
            marketDataRepository.getLiveQuote(symbol).collect { quote ->
                _uiState.update { it.copy(quote = quote, isLoading = false) }
            }
        }
    }

    fun selectInterval(interval: String) {
        _uiState.update { it.copy(selectedInterval = interval) }
        loadCandles(interval)
    }

    private fun loadCandles(interval: String) {
        candleJob?.cancel()
        candleJob = viewModelScope.launch {
            marketDataRepository.getCandlestickData(symbol, interval).collect { candles ->
                _uiState.update { it.copy(candles = candles) }
            }
        }
    }

    private fun loadOrderBook() {
        viewModelScope.launch {
            marketDataRepository.getOrderBook(symbol).collect { book ->
                _uiState.update { it.copy(orderBook = book) }
            }
        }
    }
}
