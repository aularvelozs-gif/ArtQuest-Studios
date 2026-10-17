package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.example.ui.theme.VesBlueLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditRateDialog(
    currentRate: Double,
    currentSource: String,
    onDismiss: () -> Unit,
    onSaveRate: (rate: Double, source: String) -> Unit
) {
    var rateInput by remember { mutableStateOf(currentRate.toString()) }
    var selectedSource by remember { mutableStateOf(currentSource) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedRate = rateInput.replace(',', '.').toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CurrencyExchange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Tasa de Cambio del Día",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ajusta la tasa para calcular instantáneamente los precios de mostrador y vueltos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Input de tasa
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = {
                        rateInput = it
                        errorMessage = null
                    },
                    label = { Text("Valor en Bs. por 1 USD") },
                    placeholder = { Text("Ej: 36.50") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rate_input_field")
                )

                // Botones de presets rápidos
                Text(
                    text = "Valores rápidos de referencia:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            rateInput = "36.50"
                            selectedSource = "BCV"
                        },
                        label = { Text("BCV 36.50") }
                    )
                    SuggestionChip(
                        onClick = {
                            rateInput = "42.00"
                            selectedSource = "Paralelo"
                        },
                        label = { Text("42.00") }
                    )
                    SuggestionChip(
                        onClick = {
                            rateInput = "45.00"
                            selectedSource = "Paralelo"
                        },
                        label = { Text("45.00") }
                    )
                }

                // Selector de fuente
                Text(
                    text = "Fuente de la tasa:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BCV", "Paralelo", "Manual").forEach { src ->
                        FilterChip(
                            selected = selectedSource == src,
                            onClick = { selectedSource = src },
                            label = { Text(src) }
                        )
                    }
                }

                // Vista previa de conversión
                if (parsedRate > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VesBlueLight)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Vista previa de conversión:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$1.00 = ${CurrencyCalculator.formatVes(parsedRate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$10.00 = ${CurrencyCalculator.formatVes(parsedRate * 10)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedRate <= 0.0) {
                        errorMessage = "Ingresa una tasa válida mayor a 0"
                    } else {
                        onSaveRate(parsedRate, selectedSource)
                    }
                },
                modifier = Modifier.testTag("save_rate_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar Tasa")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_rate_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
