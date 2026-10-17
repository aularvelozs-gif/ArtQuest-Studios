package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ConfiguracionTasa
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.VesBlueLight

@Composable
fun RateHeaderBanner(
    activeRate: ConfiguracionTasa?,
    isRateOutdated: Boolean,
    onEditRateClick: () -> Unit,
    onQuickConverterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rateValue = activeRate?.valorTasa ?: 36.50
    val source = activeRate?.fuente ?: "Manual"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Alerta visual en amarillo si la tasa no se ha modificado en las últimas 24 horas
            AnimatedVisibility(visible = isRateOutdated) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberWarning.copy(alpha = 0.2f))
                        .clickable { onEditRateClick() }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("rate_outdated_alert"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta tasa desactualizada",
                        tint = Color(0xFFB78103),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tasa no actualizada en 24h. Toca para actualizar.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A5500)
                    )
                }
            }

            // Banner principal de tasa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de tasa activa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VesBlueLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("rate_display_badge")
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Tasa de cambio",
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "1 USD = ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${CurrencyCalculator.formatRate(rateValue)} VES",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                        Text(
                            text = "Fuente: $source",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Botones rápidos de acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Editar Tasa
                    FilledTonalButton(
                        onClick = onEditRateClick,
                        modifier = Modifier.testTag("edit_rate_button"),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Tasa",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tasa",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Botón Convertidor
                    ElevatedButton(
                        onClick = onQuickConverterClick,
                        modifier = Modifier.testTag("quick_converter_button"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Convertidor",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Convertidor",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
