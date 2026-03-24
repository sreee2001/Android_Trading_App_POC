package com.trading.app.presentation.watchlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trading.app.presentation.components.FlashingPriceText
import com.trading.app.presentation.components.Formatters
import com.trading.app.presentation.components.SparklineChart
import com.trading.app.presentation.components.StockTickerCard
import com.trading.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onStockClick: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Search symbols...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = DarkBorder,
                cursorColor = PrimaryBlue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            singleLine = true
        )

        // Search results dropdown
        AnimatedVisibility(visible = state.isSearching && state.searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
            ) {
                state.searchResults.forEach { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.addToWatchlist(stock.symbol, stock.companyName)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stock.symbol, style = TradingTypography.ticker, color = TextPrimary)
                            Text(stock.companyName, fontSize = 12.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = PrimaryBlue)
                    }
                    if (stock != state.searchResults.last()) {
                        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                    }
                }
            }
        }

        // Watchlist items
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Watchlist (${state.items.size})",
                    style = TradingTypography.sectionHeader,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(state.items, key = { it.symbol }) { item ->
                WatchlistItemRow(
                    item = item,
                    onClick = { onStockClick(item.symbol) },
                    onRemove = { viewModel.removeFromWatchlist(item.symbol) }
                )
            }
        }
    }
}

@Composable
private fun WatchlistItemRow(
    item: com.trading.app.domain.model.WatchlistItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val changeColor = if (item.isPositive) ProfitGreen else LossRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.symbol, style = TradingTypography.ticker, color = TextPrimary)
            Text(item.companyName, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
        }

        if (item.sparklineData.isNotEmpty()) {
            SparklineChart(
                data = item.sparklineData,
                color = changeColor,
                modifier = Modifier
                    .width(60.dp)
                    .height(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(horizontalAlignment = Alignment.End) {
            FlashingPriceText(
                price = item.lastPrice,
                formattedPrice = Formatters.formatPrice(item.lastPrice),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textColor = TextPrimary
            )
            Text(
                "${Formatters.formatChange(item.change)} (${Formatters.formatPercent(item.changePercent)})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = changeColor
            )
        }
    }
}
