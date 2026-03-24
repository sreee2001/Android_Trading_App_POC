package com.trading.app.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.app.domain.model.WatchlistItem
import com.trading.app.domain.repository.MarketDataRepository
import com.trading.app.domain.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val items: List<WatchlistItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.trading.app.domain.model.StockQuote> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val marketDataRepository: MarketDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            watchlistRepository.getWatchlist().collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length >= 1) {
            viewModelScope.launch {
                marketDataRepository.searchSymbols(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = true) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
    }

    fun addToWatchlist(symbol: String, companyName: String) {
        viewModelScope.launch {
            watchlistRepository.addToWatchlist(symbol, companyName)
            _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
        }
    }

    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch {
            watchlistRepository.removeFromWatchlist(symbol)
        }
    }
}
