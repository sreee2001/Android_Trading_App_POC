package com.trading.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.app.domain.repository.MarketDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val marketDataRepository: MarketDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadMarketData()
    }

    private fun loadMarketData() {
        viewModelScope.launch {
            marketDataRepository.getMarketIndices().collect { indices ->
                _uiState.update { it.copy(indices = indices, isLoading = false) }
            }
        }
        viewModelScope.launch {
            marketDataRepository.getTopMovers().collect { movers ->
                _uiState.update { it.copy(topMovers = movers) }
            }
        }
        viewModelScope.launch {
            marketDataRepository.getSectorPerformance().collect { sectors ->
                _uiState.update { it.copy(sectors = sectors) }
            }
        }
    }
}
