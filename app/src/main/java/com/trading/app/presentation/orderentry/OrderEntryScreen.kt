package com.trading.app.presentation.orderentry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trading.app.domain.model.*
import com.trading.app.presentation.components.Formatters
import com.trading.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryScreen(
    onBack: () -> Unit,
    viewModel: OrderEntryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trade ${state.symbol}", style = TradingTypography.ticker, color = TextPrimary)
                        Text(state.companyName, fontSize = 12.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Current Price", color = TextSecondary, fontSize = 14.sp)
                Text(
                    Formatters.formatCurrency(state.currentPrice),
                    style = TradingTypography.priceLarge,
                    color = TextPrimary
                )
            }

            // Buy/Sell toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(4.dp)
            ) {
                OrderSide.entries.forEach { side ->
                    val isSelected = state.side == side
                    val color = when {
                        isSelected && side == OrderSide.BUY -> ProfitGreen
                        isSelected && side == OrderSide.SELL -> LossRed
                        else -> DarkCard
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color)
                            .clickable { viewModel.setSide(side) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            side.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSelected) DarkBackground else TextSecondary
                        )
                    }
                }
            }

            // Order Type
            Text("Order Type", style = TradingTypography.label)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderType.entries.forEach { type ->
                    val isSelected = state.type == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryBlue else DarkSurfaceVariant)
                            .clickable { viewModel.setType(type) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            type.name.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarkBackground else TextSecondary
                        )
                    }
                }
            }

            // Quantity
            TradingTextField(
                label = "Quantity",
                value = state.quantity,
                onValueChange = viewModel::setQuantity,
                keyboardType = KeyboardType.Number
            )

            // Quick quantity buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.25, 0.50, 0.75, 1.0).forEach { pct ->
                    OutlinedButton(
                        onClick = { viewModel.setQuantityPercent(pct) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        )
                    ) {
                        Text("${(pct * 100).toInt()}%", fontSize = 12.sp)
                    }
                }
            }

            // Limit price (conditional)
            if (state.type in listOf(OrderType.LIMIT, OrderType.STOP_LIMIT)) {
                TradingTextField(
                    label = "Limit Price",
                    value = state.limitPrice,
                    onValueChange = viewModel::setLimitPrice,
                    keyboardType = KeyboardType.Decimal
                )
            }

            // Stop price (conditional)
            if (state.type in listOf(OrderType.STOP, OrderType.STOP_LIMIT, OrderType.TRAILING_STOP)) {
                TradingTextField(
                    label = "Stop Price",
                    value = state.stopPrice,
                    onValueChange = viewModel::setStopPrice,
                    keyboardType = KeyboardType.Decimal
                )
            }

            // Time in Force
            Text("Time in Force", style = TradingTypography.label)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeInForce.entries.forEach { tif ->
                    val isSelected = state.timeInForce == tif
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryBlue else DarkSurfaceVariant)
                            .clickable { viewModel.setTimeInForce(tif) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            tif.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarkBackground else TextSecondary
                        )
                    }
                }
            }

            // Order summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(16.dp)
            ) {
                Text("Order Summary", style = TradingTypography.sectionHeader, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                SummaryRow("Side", state.side.name, if (state.side == OrderSide.BUY) ProfitGreen else LossRed)
                SummaryRow("Type", state.type.name.replace("_", " "), TextPrimary)
                SummaryRow("Quantity", state.quantity.ifEmpty { "—" }, TextPrimary)
                SummaryRow("Est. Cost", Formatters.formatCurrency(state.estimatedCost), TextPrimary)
                SummaryRow("Buying Power", Formatters.formatCurrency(state.buyingPower), TextSecondary)
                if (state.currentPosition > 0) {
                    SummaryRow("Current Position", "${state.currentPosition} shares", TextSecondary)
                }
            }

            // Error
            state.error?.let { error ->
                Text(
                    error,
                    color = LossRed,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LossRed.copy(alpha = 0.1f))
                        .padding(12.dp)
                )
            }

            // Success
            state.orderResult?.let { result ->
                Text(
                    result,
                    color = ProfitGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProfitGreen.copy(alpha = 0.1f))
                        .padding(12.dp)
                )
            }

            // Submit button
            Button(
                onClick = { viewModel.showConfirmation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.side == OrderSide.BUY) ProfitGreen else LossRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Review ${state.side.name} Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Confirmation dialog
        if (state.showConfirmation) {
            AlertDialog(
                onDismissRequest = viewModel::dismissConfirmation,
                title = { Text("Confirm Order", color = TextPrimary) },
                text = {
                    Column {
                        Text(
                            "${state.side} ${state.quantity} ${state.symbol} @ ${if (state.type == OrderType.MARKET) "MARKET" else state.limitPrice}",
                            color = TextPrimary, fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Est. Cost: ${Formatters.formatCurrency(state.estimatedCost)}", color = TextSecondary)
                        Text("Time in Force: ${state.timeInForce.name}", color = TextSecondary)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::submitOrder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.side == OrderSide.BUY) ProfitGreen else LossRed
                        )
                    ) { Text("Confirm", color = DarkBackground) }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissConfirmation) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
private fun TradingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
