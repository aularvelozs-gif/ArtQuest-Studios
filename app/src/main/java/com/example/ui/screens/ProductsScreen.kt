package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Producto
import com.example.domain.CurrencyCalculator
import com.example.ui.theme.RedError
import com.example.ui.theme.UsdGreen
import com.example.ui.theme.UsdGreenLight
import com.example.ui.theme.VesBlue
import com.example.ui.theme.VesBlueLight
import com.example.ui.viewmodel.CobroAgilViewModel

@Composable
fun ProductsScreen(
    viewModel: CobroAgilViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showAddEditDialog by viewModel.showAddEditProductDialog.collectAsStateWithLifecycle()
    val productToEdit by viewModel.productToEdit.collectAsStateWithLifecycle()
    val rate = viewModel.currentRateValue

    val categories = remember(allProducts) {
        listOf("Todos") + allProducts.map { it.categoria }.distinct().filter { it.isNotBlank() }
    }

    val filteredProducts = remember(allProducts, selectedCategory) {
        if (selectedCategory == "Todos") allProducts
        else allProducts.filter { it.categoria.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddProduct() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Producto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Cabecera del catálogo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Catálogo de Productos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${allProducts.size} registrados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros de categoría en scroll horizontal
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de productos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("products_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCardItem(
                        product = product,
                        rate = rate,
                        onToggleFavorite = { viewModel.toggleFavorite(product) },
                        onEdit = { viewModel.openEditProduct(product) }
                    )
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditProductDialog(
            product = productToEdit,
            currentRate = rate,
            onDismiss = { viewModel.closeAddEditProduct() },
            onSave = { viewModel.saveProduct(it) },
            onDelete = { viewModel.deleteProduct(it) }
        )
    }
}

@Composable
fun ProductCardItem(
    product: Producto,
    rate: Double,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit
) {
    val (priceUsd, priceVes) = CurrencyCalculator.convertProductPrice(
        product.precioBase,
        product.monedaBase,
        rate
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Toggle Favorito
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (product.esFavorito) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (product.esFavorito) Color(0xFFFFA000) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${product.categoria} • ${product.codigoBarras?.let { "Cod: $it" } ?: "Sin código"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Moneda base: ${product.monedaBase} (${if (product.monedaBase == "USD") "$${product.precioBase}" else "Bs. ${product.precioBase}"})",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Precios en $ y Bs.
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyCalculator.formatUsd(priceUsd),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsdGreen
                )
                Text(
                    text = CurrencyCalculator.formatVes(priceVes),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VesBlue
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Editar
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditProductDialog(
    product: Producto?,
    currentRate: Double,
    onDismiss: () -> Unit,
    onSave: (Producto) -> Unit,
    onDelete: (Producto) -> Unit
) {
    val isEditing = product != null

    var nombre by remember { mutableStateOf(product?.nombre ?: "") }
    var codigoBarras by remember { mutableStateOf(product?.codigoBarras ?: "") }
    var categoria by remember { mutableStateOf(product?.categoria ?: "Víveres") }
    var monedaBase by remember { mutableStateOf(product?.monedaBase ?: "USD") }
    var precioBaseText by remember { mutableStateOf(product?.precioBase?.toString() ?: "") }
    var esFavorito by remember { mutableStateOf(product?.esFavorito ?: false) }
    var activo by remember { mutableStateOf(product?.activo ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Producto" else "Nuevo Producto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorMessage = null
                    },
                    label = { Text("Nombre del Producto *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = codigoBarras,
                    onValueChange = { codigoBarras = it },
                    label = { Text("Código de Barras") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Moneda Base (USD o VES)
                Text(
                    text = "Moneda Base de Fijación de Precio:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = monedaBase == "USD",
                        onClick = { monedaBase = "USD" },
                        label = { Text("Dólares ($ USD)") }
                    )
                    FilterChip(
                        selected = monedaBase == "VES",
                        onClick = { monedaBase = "VES" },
                        label = { Text("Bolívares (Bs. VES)") }
                    )
                }

                // Precio Base
                OutlinedTextField(
                    value = precioBaseText,
                    onValueChange = {
                        precioBaseText = it
                        errorMessage = null
                    },
                    label = { Text("Precio Base en $monedaBase *") },
                    prefix = { Text(if (monedaBase == "USD") "$ " else "Bs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Vista previa de precio en ambas monedas
                val parsedPrice = precioBaseText.replace(',', '.').toDoubleOrNull() ?: 0.0
                if (parsedPrice > 0) {
                    val (pUsd, pVes) = CurrencyCalculator.convertProductPrice(parsedPrice, monedaBase, currentRate)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Precios calculados:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${CurrencyCalculator.formatUsd(pUsd)} = ${CurrencyCalculator.formatVes(pVes)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Switch Favorito (Acceso rápido mostrador)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Acceso Rápido (Favorito)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Mostrar en el mostrador principal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = esFavorito, onCheckedChange = { esFavorito = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        errorMessage = "Ingresa el nombre del producto"
                        return@Button
                    }
                    val price = precioBaseText.replace(',', '.').toDoubleOrNull()
                    if (price == null || price <= 0.0) {
                        errorMessage = "Ingresa un precio mayor a 0"
                        return@Button
                    }

                    val updatedProduct = (product ?: Producto(
                        nombre = nombre,
                        precioBase = price
                    )).copy(
                        nombre = nombre.trim(),
                        codigoBarras = codigoBarras.trim().ifEmpty { null },
                        categoria = categoria.trim().ifEmpty { "General" },
                        monedaBase = monedaBase,
                        precioBase = price,
                        esFavorito = esFavorito,
                        activo = activo
                    )
                    onSave(updatedProduct)
                }
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditing) {
                    TextButton(
                        onClick = { product?.let { onDelete(it) } },
                        colors = ButtonDefaults.textButtonColors(contentColor = RedError)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
