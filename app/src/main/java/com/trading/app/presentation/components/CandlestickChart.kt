package com.trading.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.domain.model.Candlestick
import com.trading.app.presentation.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CandlestickChart(
    candles: List<Candlestick>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var crosshairIndex by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        // Crosshair tooltip
        if (crosshairIndex in candles.indices) {
            val c = candles[crosshairIndex]
            CrosshairTooltip(c)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkCard)
                .pointerInput(candles.size) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offsetX = (offsetX + pan.x).coerceIn(
                            -(candles.size * 12f * scale - size.width),
                            0f
                        )
                    }
                }
                .pointerInput(candles.size) {
                    detectTapGestures { offset ->
                        val candleWidth = 12f * scale
                        val index = ((offset.x - offsetX) / candleWidth).roundToInt()
                        crosshairIndex = index.coerceIn(candles.indices)
                    }
                }
        ) {
            drawCandlesticks(candles, scale, offsetX, textMeasurer)
        }

        // Volume bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(DarkCard)
        ) {
            drawVolumeBars(candles, scale, offsetX)
        }
    }
}

private fun DrawScope.drawCandlesticks(
    candles: List<Candlestick>,
    scale: Float,
    offsetX: Float,
    textMeasurer: TextMeasurer
) {
    val padding = 40f
    val chartHeight = size.height - padding * 2
    val candleWidth = 12f * scale
    val bodyWidth = 8f * scale

    val visibleStart = ((-offsetX) / candleWidth).toInt().coerceAtLeast(0)
    val visibleEnd = ((size.width - offsetX) / candleWidth).toInt().coerceAtMost(candles.size - 1)
    if (visibleStart > visibleEnd) return

    val visibleCandles = candles.subList(visibleStart, (visibleEnd + 1).coerceAtMost(candles.size))
    val allHigh = visibleCandles.maxOf { it.high }
    val allLow = visibleCandles.minOf { it.low }
    val priceRange = if (allHigh - allLow == 0.0) 1.0 else allHigh - allLow

    fun priceToY(price: Double): Float {
        return (padding + chartHeight * (1 - (price - allLow) / priceRange)).toFloat()
    }

    // Draw grid lines
    val gridLines = 5
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
    for (i in 0..gridLines) {
        val y = padding + (chartHeight / gridLines) * i
        drawLine(DarkBorder, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f, pathEffect = dashEffect)

        val price = allHigh - (priceRange / gridLines) * i
        val text = Formatters.formatPrice(price)
        val result = textMeasurer.measure(text, TextStyle(fontSize = 9.sp, color = TextTertiary))
        drawText(result, topLeft = Offset(size.width - result.size.width - 4, y - result.size.height / 2))
    }

    // Draw candles
    for (i in visibleStart..visibleEnd) {
        val candle = candles[i]
        val x = i * candleWidth + offsetX + candleWidth / 2
        val color = if (candle.isBullish) ChartGreen else ChartRed

        // Wick
        drawLine(
            color = color,
            start = Offset(x, priceToY(candle.high)),
            end = Offset(x, priceToY(candle.low)),
            strokeWidth = 1.5f
        )

        // Body
        val openY = priceToY(candle.open)
        val closeY = priceToY(candle.close)
        val bodyTop = minOf(openY, closeY)
        val bodyHeight = maxOf(abs(openY - closeY), 1f)

        drawRect(
            color = color,
            topLeft = Offset(x - bodyWidth / 2, bodyTop),
            size = Size(bodyWidth, bodyHeight)
        )
    }
}

private fun DrawScope.drawVolumeBars(
    candles: List<Candlestick>,
    scale: Float,
    offsetX: Float
) {
    val candleWidth = 12f * scale
    val bodyWidth = 8f * scale

    val visibleStart = ((-offsetX) / candleWidth).toInt().coerceAtLeast(0)
    val visibleEnd = ((size.width - offsetX) / candleWidth).toInt().coerceAtMost(candles.size - 1)
    if (visibleStart > visibleEnd) return

    val visibleCandles = candles.subList(visibleStart, (visibleEnd + 1).coerceAtMost(candles.size))
    val maxVol = visibleCandles.maxOf { it.volume }.toFloat()
    if (maxVol == 0f) return

    for (i in visibleStart..visibleEnd) {
        val candle = candles[i]
        val x = i * candleWidth + offsetX + candleWidth / 2
        val color = if (candle.isBullish) ChartGreen.copy(alpha = 0.4f) else ChartRed.copy(alpha = 0.4f)
        val barHeight = (candle.volume / maxVol) * size.height * 0.85f

        drawRect(
            color = color,
            topLeft = Offset(x - bodyWidth / 2, size.height - barHeight),
            size = Size(bodyWidth, barHeight)
        )
    }
}

@Composable
private fun CrosshairTooltip(candle: Candlestick) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val color = if (candle.isBullish) ProfitGreen else LossRed
        TooltipItem("O", Formatters.formatPrice(candle.open), color)
        TooltipItem("H", Formatters.formatPrice(candle.high), color)
        TooltipItem("L", Formatters.formatPrice(candle.low), color)
        TooltipItem("C", Formatters.formatPrice(candle.close), color)
        TooltipItem("Vol", Formatters.formatVolume(candle.volume), TextSecondary)
    }
}

@Composable
private fun TooltipItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label ", fontSize = 10.sp, color = TextTertiary)
        Text(text = value, fontSize = 11.sp, color = color)
    }
}
