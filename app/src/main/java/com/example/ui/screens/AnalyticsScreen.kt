package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MetodoPagoResumen
import com.example.data.local.entity.Producto
import com.example.data.local.entity.TopProductoVendido
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.UsdGreen
import com.example.ui.theme.UsdGreenLight
import com.example.ui.theme.VesBlue
import com.example.ui.theme.VesBlueLight
import com.example.ui.viewmodel.CobroAgilViewModel
import com.example.ui.viewmodel.DaySalesAnalytics

@Composable
fun AnalyticsScreen(
    viewModel: CobroAgilViewModel,
    modifier: Modifier = Modifier
) {
    val topProducts by viewModel.topSoldProducts.collectAsStateWithLifecycle()
    val idleProducts by viewModel.idleProducts.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.salesByPaymentMethod.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val dayOfWeekSales = remember(allSales) { viewModel.getSalesByDayOfWeek() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSqlQueries by remember { mutableStateOf(false) }

    val tabs = listOf("Días de Venta", "Top Productos", "Huesos (30d)", "Por Método")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Cabecera del Panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analítica Financiera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón para desplegar scripts SQL
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { showSqlQueries = !showSqlQueries }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showSqlQueries) "Ocultar SQL" else "Ver SQL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Script SQL desplegable
        if (showSqlQueries) {
            item {
                SqlQueryExplanationCard()
            }
        }

        // Resumen rápido de ventas globales
        item {
            val totalUsd = allSales.sumOf { it.totalUsd }
            val totalVes = allSales.sumOf { it.totalVes }
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VENTAS TOTALES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${allSales.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FACTURADO USD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = UsdGreen
                        )
                        Text(
                            text = CurrencyCalculator.formatUsd(totalUsd),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = UsdGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FACTURADO VES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VesBlue
                        )
                        Text(
                            text = CurrencyCalculator.formatVes(totalVes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VesBlue
                        )
                    }
                }
            }
        }

        // Pestañas de reportes
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Contenido según la pestaña seleccionada
        when (selectedTabIndex) {
            0 -> {
                // 1. DÍAS DE MAYOR VENTA (Gráfico de barras)
                item {
                    DaysSalesChartSection(dayOfWeekSales)
                }
            }
            1 -> {
                // 2. TOP PRODUCTOS MÁS VENDIDOS
                item {
                    Text(
                        text = "Top Productos Más Vendidos (Ranking por unidades e ingresos):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (topProducts.isEmpty()) {
                    item {
                        Text(
                            text = "No hay datos de productos vendidos aún.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(topProducts) { index, item ->
                        TopProductCard(index = index + 1, product = item)
                    }
                }
            }
            2 -> {
                // 3. PRODUCTOS SIN MOVIMIENTO ("PRODUCTOS HUESO")
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Productos Sin Movimiento (\"Productos Hueso\"):",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Artículos activos con cero ventas registradas en los últimos 30 días. Recomendado: crear promociones o liquidar inventario.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                if (idleProducts.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "🎉 ¡Excelente! No tienes productos hueso estancados en los últimos 30 días.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(idleProducts) { prod ->
                        IdleProductCard(product = prod, rate = viewModel.currentRateValue)
                    }
                }
            }
            3 -> {
                // 4. DESGLOSE DE INGRESOS POR MONEDA / MÉTODO
                item {
                    Text(
                        text = "Desglose de Ingresos por Método de Pago:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (paymentMethods.isEmpty()) {
                    item {
                        Text(
                            text = "No hay pagos registrados aún.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(paymentMethods) { method ->
                        PaymentMethodSummaryCard(method = method)
                    }
                }
            }
        }
    }
}

@Composable
fun DaysSalesChartSection(days: List<DaySalesAnalytics>) {
    val maxUsd = days.maxOfOrNull { it.totalUsd }?.takeIf { it > 0 } ?: 1.0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Ventas por Día de la Semana (USD y VES)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Gráfico de barras visual
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    val fraction = (day.totalUsd / maxUsd).toFloat().coerceIn(0.05f, 1f)
                    val isPeak = day.totalUsd == maxUsd && day.totalUsd > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (day.totalUsd > 0) {
                            Text(
                                text = "$${day.totalUsd.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPeak) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Barra
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isPeak) EmeraldGreen
                                    else if (day.totalUsd > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Etiqueta del día
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPeak) FontWeight.Black else FontWeight.Medium,
                            color = if (isPeak) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider()

            // Detalle por día en lista
            days.filter { it.salesCount > 0 }.forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${day.dayName} (${day.salesCount} ventas)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        Text(
                            text = CurrencyCalculator.formatUsd(day.totalUsd),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsdGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${CurrencyCalculator.formatVes(day.totalVes)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = VesBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopProductCard(index: Int, product: TopProductoVendido) {
    val medal = when (index) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$index"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                Text(
                    text = medal,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
                Column {
                    Text(
                        text = product.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${product.totalUnidades} unidades despachadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyCalculator.formatUsd(product.totalUsd),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = UsdGreen
                )
                Text(
                    text = CurrencyCalculator.formatVes(product.totalVes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VesBlue
                )
            }
        }
    }
}

@Composable
fun IdleProductCard(product: Producto, rate: Double) {
    val (priceUsd, priceVes) = CurrencyCalculator.convertProductPrice(
        product.precioBase,
        product.monedaBase,
        rate
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = AmberWarning.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Categoría: ${product.categoria} • ${product.codigoBarras?.let { "Cod: $it" } ?: "Sin código"}",
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
        }
    }
}

@Composable
fun PaymentMethodSummaryCard(method: MetodoPagoResumen) {
    val icon = when (method.metodo) {
        "Efectivo USD" -> Icons.Default.MonetizationOn
        "Pago Móvil" -> Icons.Default.PhoneAndroid
        "Punto de Venta" -> Icons.Default.CreditCard
        else -> Icons.Default.Analytics
    }

    val iconColor = when (method.metodo) {
        "Efectivo USD" -> UsdGreen
        "Pago Móvil" -> VesBlue
        "Punto de Venta" -> EmeraldGreen
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.3f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = method.metodo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${method.conteoVentas} transacciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyCalculator.formatUsd(method.totalUsd),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsdGreen
                )
                Text(
                    text = CurrencyCalculator.formatVes(method.totalVes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VesBlue
                )
            }
        }
    }
}

@Composable
fun SqlQueryExplanationCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📜 Consultas SQL Relacionales Empleadas:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "-- 1. Top Productos Más Vendidos\n" +
                        "SELECT d.producto_id, p.nombre,\n" +
                        "       SUM(d.cantidad) AS total_unidades,\n" +
                        "       SUM(d.subtotal_usd) AS total_usd\n" +
                        "FROM Detalle_Venta d\n" +
                        "LEFT JOIN Productos p ON d.producto_id = p.id\n" +
                        "GROUP BY d.producto_id, p.nombre\n" +
                        "ORDER BY total_unidades DESC LIMIT 10;\n\n" +
                        "-- 2. Productos Sin Movimiento (Hueso - 30 días)\n" +
                        "SELECT * FROM Productos\n" +
                        "WHERE activo = 1 AND id NOT IN (\n" +
                        "   SELECT DISTINCT d.producto_id FROM Detalle_Venta d\n" +
                        "   JOIN Ventas v ON d.venta_id = v.id\n" +
                        "   WHERE v.fecha_hora >= :hace30Dias\n" +
                        ");",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
