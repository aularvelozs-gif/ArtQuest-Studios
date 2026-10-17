package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Producto
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightEmerald
import com.example.ui.theme.RedError
import com.example.ui.theme.UsdGreen
import com.example.ui.theme.UsdGreenLight
import com.example.ui.theme.VesBlue
import com.example.ui.theme.VesBlueLight
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.CobroAgilViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CounterScreen(
    viewModel: CobroAgilViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoriteProducts.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val inputPaidUsd by viewModel.inputPaidUsd.collectAsStateWithLifecycle()
    val inputPaidVes by viewModel.inputPaidVes.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val paymentCalc by viewModel.paymentCalculation.collectAsStateWithLifecycle()
    val rate = viewModel.currentRateValue

    // Filtrado de búsqueda
    val searchResults = if (searchQuery.isNotBlank()) {
        activeProducts.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
                    (it.codigoBarras != null && it.codigoBarras.contains(searchQuery)) ||
                    it.categoria.contains(searchQuery, ignoreCase = true)
        }
    } else emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("counter_screen_container"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. BARRA DE BÚSQUEDA & ESCÁNER ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("counter_search_field"),
                    placeholder = { Text("Buscar producto o código...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true
                )

                Button(
                    onClick = { viewModel.openBarcodeScanner() },
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("barcode_scanner_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Escanear Código de Barras",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 1.1 RESULTADOS DE BÚSQUEDA (Si hay texto) ---
        if (searchQuery.isNotBlank()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Resultados (${searchResults.size}):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (searchResults.isEmpty()) {
                            Text(
                                text = "No se encontraron productos coincidentes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            searchResults.take(6).forEach { prod ->
                                val (priceUsd, priceVes) = CurrencyCalculator.convertProductPrice(
                                    prod.precioBase,
                                    prod.monedaBase,
                                    rate
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addProductToCart(prod)
                                            viewModel.setSearchQuery("")
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.nombre,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${prod.categoria} • Cod: ${prod.codigoBarras ?: "S/C"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyCalculator.formatUsd(priceUsd),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = UsdGreen
                                        )
                                        Text(
                                            text = CurrencyCalculator.formatVes(priceVes),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VesBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            viewModel.addProductToCart(prod)
                                            viewModel.setSearchQuery("")
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Agregar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        // --- 2. ACCESO RÁPIDO (PRODUCTOS FAVORITOS) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acceso Rápido (Favoritos)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Toque para sumar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (favorites.isEmpty()) {
                    Text(
                        text = "Marca productos con estrella ⭐ en el catálogo para verlos aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(favorites) { product ->
                            FavoriteProductCard(
                                product = product,
                                rate = rate,
                                onClick = { viewModel.addProductToCart(product) }
                            )
                        }
                    }
                }
            }
        }

        // --- 3. CARRITO DINÁMICO ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cart_container"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Carrito (${cartItems.sumOf { it.cantidad }} artículos)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (cartItems.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearCart() },
                                modifier = Modifier.testTag("clear_cart_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = RedError,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Vaciar",
                                    color = RedError,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "El carrito está vacío.\nAgrega productos desde Favoritos o el buscador.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cartItems.forEach { item ->
                                CartItemRow(
                                    item = item,
                                    onIncrement = { viewModel.addProductToCart(item.producto) },
                                    onDecrement = { viewModel.removeOrDecrementCartItem(item.producto.id) },
                                    onRemove = { viewModel.removeCartItemCompletely(item.producto.id) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // --- 4. MÓDULO DE PAGO Y CÁLCULO DE VUELTO ---
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_module_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Módulo de Pago Bimoneda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Gran Total en ambas monedas
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL A COBRAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyCalculator.formatUsd(paymentCalc.totalUsd),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = UsdGreen,
                                    modifier = Modifier.testTag("total_usd_text")
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Al cambio (${CurrencyCalculator.formatRate(rate)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyCalculator.formatVes(paymentCalc.totalVes),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = VesBlue,
                                    modifier = Modifier.testTag("total_ves_text")
                                )
                            }
                        }
                    }

                    // Campos "Paga con"
                    Text(
                        text = "¿Cuánto entrega el cliente? (Soporta expresiones como 20+5):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Campo Dólares
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(UsdGreenLight)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Efectivo USD entregado:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UsdGreen
                            )
                            TextButton(
                                onClick = { viewModel.setExactUsdPayment() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Pago Exacto $", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = inputPaidUsd,
                            onValueChange = { viewModel.setPaidUsd(it) },
                            placeholder = { Text("0.00 o ej: 20+5") },
                            prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_paid_usd")
                        )

                        // Billetes rápidos USD
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1.0, 5.0, 10.0, 20.0, 50.0, 100.0).forEach { bill ->
                                SuggestionChip(
                                    onClick = { viewModel.appendPaidUsdBill(bill) },
                                    label = { Text("+$${bill.toInt()}") }
                                )
                            }
                        }
                    }

                    // Campo Bolívares
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VesBlueLight)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pago Móvil / Efectivo VES entregado:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = VesBlue
                            )
                            TextButton(
                                onClick = { viewModel.setExactVesPayment() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Pago Exacto Bs.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = inputPaidVes,
                            onValueChange = { viewModel.setPaidVes(it) },
                            placeholder = { Text("0.00 o ej: 150+50") },
                            prefix = { Text("Bs. ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_paid_ves")
                        )

                        // Montos rápidos VES
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(50.0, 100.0, 200.0, 500.0).forEach { bs ->
                                SuggestionChip(
                                    onClick = { viewModel.appendPaidVesAmount(bs) },
                                    label = { Text("+Bs.${bs.toInt()}") }
                                )
                            }
                        }
                    }

                    // Método de Pago Selector
                    Text(
                        text = "Método de Pago Principal:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Efectivo USD", "Pago Móvil", "Punto de Venta", "Mixto").forEach { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { viewModel.setPaymentMethod(method) },
                                label = { Text(method) },
                                leadingIcon = if (selectedPaymentMethod == method) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // --- CÁLCULO DE VUELTO O FALTANTE ---
                    if (cartItems.isNotEmpty()) {
                        if (paymentCalc.isFullyPaid) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_result_card"),
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "VUELTO / CAMBIO A ENTREGAR:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "En Dólares: ${CurrencyCalculator.formatUsd(paymentCalc.changeUsd)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                        Text(
                                            text = "O en Bs: ${CurrencyCalculator.formatVes(paymentCalc.changeVes)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = VesBlue
                                        )
                                    }

                                    // Sugerencia de Vuelto Mixto si aplica
                                    if (paymentCalc.suggestedMixedBillsUsd > 0 && paymentCalc.suggestedMixedChangeVes > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "💡 Vuelto Mixto recomendado: $${paymentCalc.suggestedMixedBillsUsd} en efectivo + ${CurrencyCalculator.formatVes(paymentCalc.suggestedMixedChangeVes)} al cambio",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("missing_payment_card"),
                                shape = RoundedCornerShape(10.dp),
                                color = RedError.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, RedError)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚠️ RESTANTE / FALTANTE:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RedError
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyCalculator.formatUsd(paymentCalc.remainingUsd),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RedError
                                        )
                                        Text(
                                            text = "(${CurrencyCalculator.formatVes(paymentCalc.remainingVes)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RedError
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // BOTÓN DE ACCIÓN PRINCIPAL: [ 💵 REGISTRAR Y COBRAR ]
                    Button(
                        onClick = { viewModel.registerAndCharge() },
                        enabled = cartItems.isNotEmpty() && paymentCalc.isFullyPaid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("register_and_charge_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💵 REGISTRAR Y COBRAR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteProductCard(
    product: Producto,
    rate: Double,
    onClick: () -> Unit
) {
    val (priceUsd, priceVes) = CurrencyCalculator.convertProductPrice(
        product.precioBase,
        product.monedaBase,
        rate
    )

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .testTag("fav_product_${product.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = product.nombre,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.categoria,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = CurrencyCalculator.formatUsd(priceUsd),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = UsdGreen
            )

            Text(
                text = CurrencyCalculator.formatVes(priceVes),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = VesBlue
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                text = item.producto.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${CurrencyCalculator.formatUsd(item.unitPriceUsd)} • ${CurrencyCalculator.formatVes(item.unitPriceVes)} c/u",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Contador de cantidad (- cant +)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Disminuir",
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "${item.cantidad}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Subtotal en $ y Bs.
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = CurrencyCalculator.formatUsd(item.subtotalUsd),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = UsdGreen
            )
            Text(
                text = CurrencyCalculator.formatVes(item.subtotalVes),
                style = MaterialTheme.typography.labelSmall,
                color = VesBlue
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar",
                tint = RedError.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
