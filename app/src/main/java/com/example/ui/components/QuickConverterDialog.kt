package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.UsdGreen
import com.example.ui.theme.UsdGreenLight
import com.example.ui.theme.VesBlue
import com.example.ui.theme.VesBlueLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickConverterDialog(
    currentRate: Double,
    onDismiss: () -> Unit
) {
    var usdInput by remember { mutableStateOf("10") }
    var vesInput by remember {
        val initialVes = CurrencyCalculator.usdToVes(10.0, currentRate)
        mutableStateOf(initialVes.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Convertidor Rápido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tasa de referencia
                Text(
                    text = "Tasa activa: 1 USD = ${CurrencyCalculator.formatRate(currentRate)} VES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Input USD
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(UsdGreenLight)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Monto en Dólares (USD):",
                        style = MaterialTheme.typography.labelMedium,
                        color = UsdGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = usdInput,
                        onValueChange = { newVal ->
                            usdInput = newVal
                            val usdVal = newVal.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val calculatedVes = CurrencyCalculator.usdToVes(usdVal, currentRate)
                            vesInput = if (calculatedVes == 0.0 && newVal.isEmpty()) "" else calculatedVes.toString()
                        },
                        placeholder = { Text("0.00") },
                        prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_usd_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    // Billetes rápidos USD
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1, 5, 10, 20, 50, 100).forEach { bill ->
                            SuggestionChip(
                                onClick = {
                                    usdInput = bill.toString()
                                    val calcVes = CurrencyCalculator.usdToVes(bill.toDouble(), currentRate)
                                    vesInput = calcVes.toString()
                                },
                                label = { Text("$$bill") }
                            )
                        }
                    }
                }

                // Icono intercambiar
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Conversión bidireccional",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Input VES
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VesBlueLight)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Monto en Bolívares (VES):",
                        style = MaterialTheme.typography.labelMedium,
                        color = VesBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = vesInput,
                        onValueChange = { newVal ->
                            vesInput = newVal
                            val vesVal = newVal.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val calculatedUsd = CurrencyCalculator.vesToUsd(vesVal, currentRate)
                            usdInput = if (calculatedUsd == 0.0 && newVal.isEmpty()) "" else calculatedUsd.toString()
                        },
                        placeholder = { Text("0.00") },
                        prefix = { Text("Bs. ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_ves_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    // Montos rápidos VES
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(50, 100, 200, 500, 1000).forEach { bs ->
                            SuggestionChip(
                                onClick = {
                                    vesInput = bs.toString()
                                    val calcUsd = CurrencyCalculator.vesToUsd(bs.toDouble(), currentRate)
                                    usdInput = calcUsd.toString()
                                },
                                label = { Text("Bs. $bs") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_converter_button")
            ) {
                Text("Listo")
            }
        }
    )
}
