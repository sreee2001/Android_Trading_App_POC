package com.trading.app.presentation.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.app.domain.model.Order
import com.trading.app.domain.model.Portfolio
import com.trading.app.domain.repository.OrderRepository
import com.trading.app.domain.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioUiState(
    val portfolio: Portfolio? = null,
    val orders: List<Order> = emptyList(),
    val selectedTab: Int = 0 // 0 = positions, 1 = orders
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            portfolioRepository.getPortfolio().collect { portfolio ->
                _uiState.update { it.copy(portfolio = portfolio) }
            }
        }
        viewModelScope.launch {
            orderRepository.getOrderHistory().collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }

    fun selectTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }
}
