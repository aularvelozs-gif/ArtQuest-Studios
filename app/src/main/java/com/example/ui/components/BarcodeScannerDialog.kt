package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RedError

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (barcode: String) -> Unit
) {
    var barcodeInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

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
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Escáner de Barra",
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visor óptico con línea láser animada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cámara",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(54.dp)
                    )

                    // Línea láser roja
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .offset(y = (laserOffset - 45).dp)
                            .background(RedError)
                    )

                    Text(
                        text = "Apunta al código o selecciona abajo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    )
                }

                // Entrada manual o de lector USB/Bluetooth
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text("Código de barras") },
                    placeholder = { Text("Ej: 759103100001") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (barcodeInput.isNotBlank()) {
                                onBarcodeScanned(barcodeInput)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_text_field")
                )

                // Códigos de muestra para prueba rápida
                Text(
                    text = "Prueba rápida con productos de ejemplo:",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = { onBarcodeScanned("759103100001") },
                        label = { Text("Harina P.A.N.") }
                    )
                    SuggestionChip(
                        onClick = { onBarcodeScanned("759103100002") },
                        label = { Text("Arroz Mary") }
                    )
                    SuggestionChip(
                        onClick = { onBarcodeScanned("759103100004") },
                        label = { Text("Café Fama") }
                    )
                    SuggestionChip(
                        onClick = { onBarcodeScanned("759103100010") },
                        label = { Text("Pepsi 1.5L") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (barcodeInput.isNotBlank()) {
                        onBarcodeScanned(barcodeInput)
                    }
                },
                modifier = Modifier.testTag("scan_confirm_button")
            ) {
                Text("Escanear")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("scan_cancel_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
