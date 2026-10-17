package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.VentaConDetalles
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.UsdGreen
import com.example.ui.theme.VesBlue
import com.example.ui.theme.VesBlueLight
import com.example.ui.viewmodel.CobroAgilViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: CobroAgilViewModel,
    modifier: Modifier = Modifier
) {
    val salesWithDetails by viewModel.salesWithDetails.collectAsStateWithLifecycle()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("history_screen")
    ) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Historial de Ventas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${salesWithDetails.size} cobros",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Badge de inmutabilidad
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Inmutable",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Historial inmutable: cada venta preserva su tasa original sin verse alterada por cambios futuros.",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (salesWithDetails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay ventas registradas.\nRealiza tu primer cobro en el Mostrador.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(salesWithDetails, key = { it.venta.id }) { item ->
                    SaleHistoryCard(
                        ventaConDetalles = item,
                        formattedDate = dateFormat.format(Date(item.venta.fechaHora)),
                        onViewReceipt = { viewModel.showReceipt(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun SaleHistoryCard(
    ventaConDetalles: VentaConDetalles,
    formattedDate: String,
    onViewReceipt: () -> Unit
) {
    val venta = ventaConDetalles.venta
    val itemCount = ventaConDetalles.detalles.sumOf { it.cantidad }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Venta #${venta.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(venta.metodoPagoPrincipal, fontSize = 11.sp) }
                    )
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tasa congelada
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = VesBlueLight
            ) {
                Text(
                    text = "🔒 Tasa congelada: 1 USD = ${CurrencyCalculator.formatRate(venta.tasaAplicada)} VES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VesBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            // Desglose de totales y productos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$itemCount productos vendidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (venta.vueltoEntregadoUsd > 0 || venta.vueltoEntregadoVes > 0) {
                        Text(
                            text = "Vuelto: ${CurrencyCalculator.formatUsd(venta.vueltoEntregadoUsd)} / ${CurrencyCalculator.formatVes(venta.vueltoEntregadoVes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyCalculator.formatUsd(venta.totalUsd),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UsdGreen
                    )
                    Text(
                        text = CurrencyCalculator.formatVes(venta.totalVes),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = VesBlue
                    )
                }
            }

            // Botón para ver comprobante
            OutlinedButton(
                onClick = onViewReceipt,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ver Comprobante Digital")
            }
        }
    }
}
