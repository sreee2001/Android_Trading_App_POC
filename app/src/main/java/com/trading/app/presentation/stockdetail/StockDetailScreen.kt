package com.trading.app.presentation.stockdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.trading.app.presentation.components.*
import com.trading.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    onBack: () -> Unit,
    onTrade: (String) -> Unit,
    viewModel: StockDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val intervals = listOf("1m", "5m", "15m", "1h", "1D", "1W", "1M")

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(viewModel.symbol, style = TradingTypography.ticker, color = TextPrimary)
                        state.quote?.let {
                            Text(it.companyName, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            state.quote?.let {
                TradeButton(
                    symbol = viewModel.symbol,
                    price = it.lastPrice,
                    onTrade = { onTrade(viewModel.symbol) }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price header
            item {
                state.quote?.let { quote ->
                    PriceHeader(quote)
                }
            }

            // Interval selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    intervals.forEach { interval ->
                        IntervalChip(
                            label = interval,
                            isSelected = state.selectedInterval == interval,
                            onClick = { viewModel.selectInterval(interval) }
                        )
                    }
                }
            }

            // Candlestick chart
            item {
                CandlestickChart(
                    candles = state.candles,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                )
            }

            // Quote details
            item {
                state.quote?.let { QuoteDetails(it) }
            }

            // Order book
            item {
                Text("Order Book (Level 2)", style = TradingTypography.sectionHeader, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                state.orderBook?.let { OrderBookView(it) }
            }

            // Depth chart
            item {
                Text("Market Depth", style = TradingTypography.sectionHeader, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                state.orderBook?.let { DepthChart(it) }
            }
        }
    }
}

@Composable
private fun PriceHeader(quote: com.trading.app.domain.model.StockQuote) {
    Column {
        FlashingPriceText(
            price = quote.lastPrice,
            formattedPrice = Formatters.formatCurrency(quote.lastPrice),
            textStyle = TradingTypography.priceDisplay,
            textColor = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        PriceChangeIndicator(change = quote.change, changePercent = quote.changePercent)
    }
}

@Composable
private fun IntervalChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryBlue else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) DarkBackground else TextSecondary
        )
    }
}

@Composable
private fun QuoteDetails(quote: com.trading.app.domain.model.StockQuote) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Text("Quote Details", style = TradingTypography.sectionHeader, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        val items = listOf(
            "Open" to Formatters.formatPrice(quote.open),
            "High" to Formatters.formatPrice(quote.high),
            "Low" to Formatters.formatPrice(quote.low),
            "Volume" to Formatters.formatVolume(quote.volume),
            "Market Cap" to Formatters.formatMarketCap(quote.marketCap),
            "Bid" to "${Formatters.formatPrice(quote.bid)} x ${quote.bidSize}",
            "Ask" to "${Formatters.formatPrice(quote.ask)} x ${quote.askSize}",
            "Spread" to Formatters.formatPrice(quote.spread)
        )

        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        Text(label, fontSize = 11.sp, color = TextTertiary)
                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeButton(symbol: String, price: Double, onTrade: () -> Unit) {
    Surface(
        color = DarkSurface,
        tonalElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onTrade,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = DarkBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buy", fontWeight = FontWeight.Bold, color = DarkBackground)
            }
            OutlinedButton(
                onClick = onTrade,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LossRed)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sell", fontWeight = FontWeight.Bold, color = LossRed)
            }
        }
    }
}
