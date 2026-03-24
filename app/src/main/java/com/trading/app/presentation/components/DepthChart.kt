package com.trading.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.trading.app.domain.model.OrderBook
import com.trading.app.presentation.theme.*

@Composable
fun DepthChart(
    orderBook: OrderBook,
    modifier: Modifier = Modifier
) {
    if (orderBook.bids.isEmpty() || orderBook.asks.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCard)
    ) {
        val width = size.width
        val height = size.height
        val midX = width / 2

        // Cumulative bids (right to left from center)
        val cumulativeBids = orderBook.bids.runningFold(0) { acc, entry -> acc + entry.quantity }.drop(1)
        val cumulativeAsks = orderBook.asks.runningFold(0) { acc, entry -> acc + entry.quantity }.drop(1)
        val maxCumulative = maxOf(
            cumulativeBids.maxOrNull() ?: 1,
            cumulativeAsks.maxOrNull() ?: 1
        ).toFloat()

        // Bid side (left)
        val bidPath = Path().apply {
            moveTo(midX, height)
            cumulativeBids.forEachIndexed { i, cum ->
                val x = midX - (i + 1).toFloat() / cumulativeBids.size * midX
                val y = height - (cum / maxCumulative) * height * 0.9f
                lineTo(x, y)
            }
            lineTo(0f, height)
            close()
        }

        // Ask side (right)
        val askPath = Path().apply {
            moveTo(midX, height)
            cumulativeAsks.forEachIndexed { i, cum ->
                val x = midX + (i + 1).toFloat() / cumulativeAsks.size * midX
                val y = height - (cum / maxCumulative) * height * 0.9f
                lineTo(x, y)
            }
            lineTo(width, height)
            close()
        }

        drawPath(bidPath, ChartGreen.copy(alpha = 0.3f), style = Fill)
        drawPath(askPath, ChartRed.copy(alpha = 0.3f), style = Fill)

        // Center line
        drawLine(
            TextTertiary, Offset(midX, 0f), Offset(midX, height), strokeWidth = 1f
        )
    }
}
