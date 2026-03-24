package com.trading.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.domain.model.OrderBook
import com.trading.app.domain.model.OrderBookEntry
import com.trading.app.presentation.theme.*

@Composable
fun OrderBookView(
    orderBook: OrderBook,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bid", fontSize = 11.sp, color = ProfitGreen, fontWeight = FontWeight.SemiBold)
            Text("Price", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Text("Ask", fontSize = 11.sp, color = LossRed, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))

        val maxQty = maxOf(
            orderBook.bids.maxOfOrNull { it.quantity } ?: 1,
            orderBook.asks.maxOfOrNull { it.quantity } ?: 1
        ).toFloat()

        val rowCount = minOf(orderBook.bids.size, orderBook.asks.size, 10)
        for (i in 0 until rowCount) {
            val bid = orderBook.bids.getOrNull(i)
            val ask = orderBook.asks.getOrNull(i)
            OrderBookRow(bid, ask, maxQty)
        }

        // Spread
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Spread: ${Formatters.formatPrice(orderBook.spread)} (${String.format("%.3f", orderBook.spreadPercent)}%)",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun OrderBookRow(bid: OrderBookEntry?, ask: OrderBookEntry?, maxQty: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bid quantity with bar
        Box(modifier = Modifier.weight(1f)) {
            if (bid != null) {
                val fraction = bid.quantity / maxQty
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .background(ProfitGreen.copy(alpha = 0.12f))
                )
                Text(
                    text = Formatters.formatVolume(bid.quantity.toLong()),
                    fontSize = 10.sp,
                    color = ProfitGreen.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                )
            }
        }

        // Price (center — use bid price or ask price)
        val price = bid?.price ?: ask?.price ?: 0.0
        Text(
            text = Formatters.formatPrice(price),
            fontSize = 10.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )

        // Ask quantity with bar
        Box(modifier = Modifier.weight(1f)) {
            if (ask != null) {
                val fraction = ask.quantity / maxQty
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(LossRed.copy(alpha = 0.12f))
                )
                Text(
                    text = Formatters.formatVolume(ask.quantity.toLong()),
                    fontSize = 10.sp,
                    color = LossRed.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }
        }
    }
}
