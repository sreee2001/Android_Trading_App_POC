package com.trading.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.trading.app.domain.model.MarketIndex
import com.trading.app.domain.model.SectorPerformance
import com.trading.app.presentation.components.FlashingPriceText
import com.trading.app.presentation.components.Formatters
import com.trading.app.presentation.components.StockTickerCard
import com.trading.app.presentation.theme.*

@Composable
fun DashboardScreen(
    onStockClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Market Status
        item {
            MarketStatusBanner()
        }

        // Market Indices
        item {
            Text("Market Overview", style = TradingTypography.sectionHeader, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.indices) { index ->
                    MarketIndexCard(index)
                }
            }
        }

        // Sector Heatmap
        item {
            Text("Sector Performance", style = TradingTypography.sectionHeader, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            SectorHeatmap(state.sectors)
        }

        // Top Movers
        item {
            Text("Top Movers", style = TradingTypography.sectionHeader, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(state.topMovers, key = { it.symbol }) { quote ->
            StockTickerCard(
                quote = quote,
                onClick = { onStockClick(quote.symbol) }
            )
        }
    }
}

@Composable
private fun MarketStatusBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ProfitGreenDark.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ProfitGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Market Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ProfitGreen)
        Spacer(modifier = Modifier.weight(1f))
        Text("NYSE · NASDAQ", fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun MarketIndexCard(index: MarketIndex) {
    val changeColor = if (index.isPositive) ProfitGreen else LossRed
    val bg = if (index.isPositive) ProfitGreen.copy(alpha = 0.06f) else LossRed.copy(alpha = 0.06f)

    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(14.dp)
    ) {
        Text(index.name, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        FlashingPriceText(
            price = index.value,
            formattedPrice = Formatters.formatPrice(index.value),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            textColor = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${Formatters.formatChange(index.change)} (${Formatters.formatPercent(index.changePercent)})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = changeColor
        )
    }
}

@Composable
private fun SectorHeatmap(sectors: List<SectorPerformance>) {
    val rows = sectors.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { sector ->
                    SectorHeatmapCell(
                        sector = sector,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if row has fewer than 3
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectorHeatmapCell(sector: SectorPerformance, modifier: Modifier = Modifier) {
    val bg = when {
        sector.changePercent > 2.0 -> HeatmapLightGreen
        sector.changePercent > 0.5 -> HeatmapGreen.copy(alpha = 0.6f)
        sector.changePercent > 0.0 -> HeatmapDarkGreen.copy(alpha = 0.5f)
        sector.changePercent > -0.5 -> HeatmapNeutral
        sector.changePercent > -2.0 -> HeatmapLightRed.copy(alpha = 0.5f)
        else -> HeatmapDarkRed.copy(alpha = 0.7f)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            sector.name,
            fontSize = 9.sp,
            color = TextPrimary,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
        Text(
            Formatters.formatPercent(sector.changePercent),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
