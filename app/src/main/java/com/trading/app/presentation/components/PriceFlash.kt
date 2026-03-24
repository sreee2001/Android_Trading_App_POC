package com.trading.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trading.app.presentation.theme.LossRed
import com.trading.app.presentation.theme.ProfitGreen
import com.trading.app.presentation.theme.TextPrimary
import kotlinx.coroutines.delay

/**
 * Direction of the last price tick for flash animation.
 */
enum class TickDirection { UP, DOWN, NONE }

/**
 * Remembers the previous price and computes the tick direction whenever the price changes.
 */
@Composable
fun rememberTickDirection(price: Double): TickDirection {
    var previousPrice by remember { mutableDoubleStateOf(price) }
    var direction by remember { mutableStateOf(TickDirection.NONE) }

    LaunchedEffect(price) {
        direction = when {
            price > previousPrice -> TickDirection.UP
            price < previousPrice -> TickDirection.DOWN
            else -> TickDirection.NONE
        }
        previousPrice = price
    }

    return direction
}

/**
 * A composable that briefly flashes green or red when the price ticks up or down,
 * then fades back to transparent. Mimics real trading terminal price flash behavior.
 */
@Composable
fun FlashingPriceText(
    price: Double,
    formattedPrice: String,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),
    textColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    val tick = rememberTickDirection(price)
    var isFlashing by remember { mutableStateOf(false) }

    // Trigger flash on every tick change
    LaunchedEffect(tick, price) {
        if (tick != TickDirection.NONE) {
            isFlashing = true
            delay(400) // flash duration
            isFlashing = false
        }
    }

    val flashColor = when {
        isFlashing && tick == TickDirection.UP -> ProfitGreen.copy(alpha = 0.25f)
        isFlashing && tick == TickDirection.DOWN -> LossRed.copy(alpha = 0.25f)
        else -> Color.Transparent
    }

    val animatedBg by animateColorAsState(
        targetValue = flashColor,
        animationSpec = tween(durationMillis = if (isFlashing) 100 else 500),
        label = "priceFlash"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = when {
            isFlashing && tick == TickDirection.UP -> ProfitGreen
            isFlashing && tick == TickDirection.DOWN -> LossRed
            else -> textColor
        },
        animationSpec = tween(durationMillis = if (isFlashing) 100 else 600),
        label = "priceTextFlash"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(animatedBg)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = formattedPrice,
            style = textStyle,
            color = animatedTextColor
        )
    }
}
