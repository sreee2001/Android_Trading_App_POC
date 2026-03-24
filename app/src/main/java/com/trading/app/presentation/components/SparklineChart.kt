package com.trading.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SparklineChart(
    data: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val min = data.min()
        val max = data.max()
        val range = if (max - min == 0.0) 1.0 else max - min

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1)) * width
            val y = height - ((value - min) / range * height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}
