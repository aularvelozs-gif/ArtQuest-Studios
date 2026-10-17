package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VentaConDetalles
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.UsdGreen
import com.example.ui.theme.VesBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaleReceiptDialog(
    ventaConDetalles: VentaConDetalles,
    onDismiss: () -> Unit
) {
    val venta = ventaConDetalles.venta
    val detalles = ventaConDetalles.detalles
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val fechaStr = dateFormat.format(Date(venta.fechaHora))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "COBROÁGIL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Comprobante de Venta #${venta.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, DividerDefaults.color, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                // Info cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fecha: $fechaStr",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Método: ${venta.metodoPagoPrincipal}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tasa congelada: 1 USD = ${CurrencyCalculator.formatRate(venta.tasaAplicada)} VES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Encabezado de columnas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cant / Producto",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = "USD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "VES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Lista de items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    items(detalles) { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${d.cantidad}x ${d.nombreProducto}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                text = CurrencyCalculator.formatUsd(d.subtotalUsd),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CurrencyCalculator.formatVes(d.subtotalVes),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Totales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TOTAL VENTA:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Desglose de Pago y Vuelto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pagado con:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${CurrencyCalculator.formatUsd(venta.montoPagadoUsd)} + ${CurrencyCalculator.formatVes(venta.montoPagadoVes)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (venta.vueltoEntregadoUsd > 0 || venta.vueltoEntregadoVes > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vuelto entregado:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = buildString {
                                if (venta.vueltoEntregadoUsd > 0) append("${CurrencyCalculator.formatUsd(venta.vueltoEntregadoUsd)} ")
                                if (venta.vueltoEntregadoVes > 0) append("(${CurrencyCalculator.formatVes(venta.vueltoEntregadoVes)})")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_receipt_button")
            ) {
                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Listo / Nueva Venta")
            }
        }
    )
}
