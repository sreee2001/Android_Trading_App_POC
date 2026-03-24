package com.trading.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.domain.model.StockQuote
import com.trading.app.presentation.theme.*

@Composable
fun StockTickerCard(
    quote: StockQuote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (quote.isPositive) ProfitGreen.copy(alpha = 0.08f) else LossRed.copy(alpha = 0.08f),
        animationSpec = tween(300),
        label = "bgColor"
    )
    val changeColor = if (quote.isPositive) ProfitGreen else LossRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quote.symbol,
                style = TradingTypography.ticker,
                color = TextPrimary
            )
            Text(
                text = quote.companyName,
                style = TradingTypography.label,
                color = TextSecondary,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Sparkline placeholder
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(30.dp)
        ) {
            SparklineChart(
                data = listOf(
                    quote.open, quote.low, quote.high, quote.lastPrice
                ),
                color = changeColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = Formatters.formatPrice(quote.lastPrice),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${Formatters.formatChange(quote.change)} (${Formatters.formatPercent(quote.changePercent)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = changeColor
            )
        }
    }
}

@Composable
fun PriceChangeIndicator(
    change: Double,
    changePercent: Double,
    modifier: Modifier = Modifier
) {
    val color = if (change >= 0) ProfitGreen else LossRed
    val bg = if (change >= 0) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f)
    val arrow = if (change >= 0) "▲" else "▼"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = arrow, color = color, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${Formatters.formatChange(change)} (${Formatters.formatPercent(changePercent)})",
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
