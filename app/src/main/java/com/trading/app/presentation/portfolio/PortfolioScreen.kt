package com.trading.app.presentation.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trading.app.domain.model.Order
import com.trading.app.domain.model.OrderStatus
import com.trading.app.domain.model.Position
import com.trading.app.presentation.components.FlashingPriceText
import com.trading.app.presentation.components.Formatters
import com.trading.app.presentation.components.PortfolioSummaryCard
import com.trading.app.presentation.theme.*

@Composable
fun PortfolioScreen(
    onStockClick: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Portfolio summary
        item {
            state.portfolio?.let { PortfolioSummaryCard(it) }
        }

        // Tab selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .padding(4.dp)
            ) {
                listOf("Positions", "Order History").forEachIndexed { index, label ->
                    val isSelected = state.selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryBlue else DarkSurface)
                            .clickable { viewModel.selectTab(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarkBackground else TextSecondary
                        )
                    }
                }
            }
        }

        if (state.selectedTab == 0) {
            // Positions
            val positions = state.portfolio?.positions ?: emptyList()
            if (positions.isEmpty()) {
                item {
                    EmptyState("No positions yet", "Place a trade to get started")
                }
            } else {
                items(positions, key = { it.symbol }) { position ->
                    PositionCard(position, onClick = { onStockClick(position.symbol) })
                }
            }
        } else {
            // Order history
            if (state.orders.isEmpty()) {
                item {
                    EmptyState("No orders yet", "Your order history will appear here")
                }
            } else {
                items(state.orders, key = { it.id }) { order ->
                    OrderCard(order)
                }
            }
        }
    }
}

@Composable
private fun PositionCard(position: Position, onClick: () -> Unit) {
    val pnlColor = if (position.isProfit) ProfitGreen else LossRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(position.symbol, style = TradingTypography.ticker, color = TextPrimary)
            Text("${position.quantity} shares", fontSize = 12.sp, color = TextSecondary)
            Text("Avg: ${Formatters.formatPrice(position.averageCost)}", fontSize = 11.sp, color = TextTertiary)
        }
        Column(horizontalAlignment = Alignment.End) {
            FlashingPriceText(
                price = position.currentPrice,
                formattedPrice = Formatters.formatCurrency(position.marketValue),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textColor = TextPrimary
            )
            Text(
                "${Formatters.formatChange(position.unrealizedPnL)} (${Formatters.formatPercent(position.unrealizedPnLPercent)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = pnlColor
            )
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    val statusColor = when (order.status) {
        OrderStatus.FILLED -> StatusFilled
        OrderStatus.CANCELLED -> StatusCancelled
        OrderStatus.REJECTED -> StatusRejected
        OrderStatus.NEW, OrderStatus.ACKNOWLEDGED, OrderStatus.PARTIALLY_FILLED -> StatusPending
        OrderStatus.PENDING_CANCEL -> StatusCancelled
    }
    val sideColor = if (order.side == com.trading.app.domain.model.OrderSide.BUY) ProfitGreen else LossRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(sideColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(order.side.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = sideColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.symbol, style = TradingTypography.ticker, color = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(order.status.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${order.filledQuantity}/${order.quantity} @ ${order.avgFillPrice?.let { Formatters.formatPrice(it) } ?: "—"}",
                fontSize = 12.sp, color = TextSecondary)
            Text(order.type.name.replace("_", " "), fontSize = 12.sp, color = TextTertiary)
        }
        Text(Formatters.formatTimestamp(order.updatedAt, "MMM dd, HH:mm:ss"), fontSize = 10.sp, color = TextTertiary)
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = TextTertiary)
    }
}
