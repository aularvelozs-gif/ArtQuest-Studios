package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ConfiguracionTasa
import com.example.data.local.entity.DetalleVenta
import com.example.data.local.entity.MetodoPagoResumen
import com.example.data.local.entity.Producto
import com.example.data.local.entity.TopProductoVendido
import com.example.data.local.entity.Venta
import com.example.data.local.entity.VentaConDetalles
import com.example.data.repository.CobroAgilRepository
import com.example.domain.CurrencyCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val producto: Producto,
    val cantidad: Int,
    val unitPriceUsd: Double,
    val unitPriceVes: Double
) {
    val subtotalUsd: Double get() = CurrencyCalculator.roundTo(unitPriceUsd * cantidad, 2)
    val subtotalVes: Double get() = CurrencyCalculator.roundTo(unitPriceVes * cantidad, 2)
}

data class DaySalesAnalytics(
    val dayName: String,
    val totalUsd: Double,
    val totalVes: Double,
    val salesCount: Int
)

class CobroAgilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CobroAgilRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = CobroAgilRepository(database.cobroAgilDao())
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    // --- TASA ACTIVA Y ESTADO ---
    val activeRate: StateFlow<ConfiguracionTasa?> = repository.activeRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rateHistory: StateFlow<List<ConfiguracionTasa>> = repository.rateHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- PRODUCTOS ---
    val allProducts: StateFlow<List<Producto>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<Producto>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProducts: StateFlow<List<Producto>> = repository.favoriteProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- VENTAS & HISTORIAL ---
    val salesWithDetails: StateFlow<List<VentaConDetalles>> = repository.salesWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<Venta>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ANALÍTICA ---
    val topSoldProducts: StateFlow<List<TopProductoVendido>> = repository.topSoldProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val idleProducts: StateFlow<List<Producto>> = repository.getIdleProducts(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesByPaymentMethod: StateFlow<List<MetodoPagoResumen>> = repository.salesByPaymentMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ESTADO DEL MOSTRADOR / CARRITO ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _inputPaidUsd = MutableStateFlow("")
    val inputPaidUsd: StateFlow<String> = _inputPaidUsd.asStateFlow()

    private val _inputPaidVes = MutableStateFlow("")
    val inputPaidVes: StateFlow<String> = _inputPaidVes.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("Efectivo USD")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // --- DIÁLOGOS Y NAVEGACIÓN AUXILIAR ---
    private val _showEditRateDialog = MutableStateFlow(false)
    val showEditRateDialog: StateFlow<Boolean> = _showEditRateDialog.asStateFlow()

    private val _showQuickConverter = MutableStateFlow(false)
    val showQuickConverter: StateFlow<Boolean> = _showQuickConverter.asStateFlow()

    private val _showBarcodeScanner = MutableStateFlow(false)
    val showBarcodeScanner: StateFlow<Boolean> = _showBarcodeScanner.asStateFlow()

    private val _productToEdit = MutableStateFlow<Producto?>(null)
    val productToEdit: StateFlow<Producto?> = _productToEdit.asStateFlow()

    private val _showAddEditProductDialog = MutableStateFlow(false)
    val showAddEditProductDialog: StateFlow<Boolean> = _showAddEditProductDialog.asStateFlow()

    private val _lastSaleReceipt = MutableStateFlow<VentaConDetalles?>(null)
    val lastSaleReceipt: StateFlow<VentaConDetalles?> = _lastSaleReceipt.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    private val _saleFeedbackMessage = MutableStateFlow<String?>(null)
    val saleFeedbackMessage: StateFlow<String?> = _saleFeedbackMessage.asStateFlow()

    // --- CÁLCULOS REACTIVOS ---
    val currentRateValue: Double
        get() = activeRate.value?.valorTasa ?: 36.50

    val isRateOutdated: Boolean
        get() {
            val lastUpdate = activeRate.value?.fechaActualizacion ?: return true
            val diff = System.currentTimeMillis() - lastUpdate
            return diff > 24 * 60 * 60 * 1000L // mayor a 24 horas
        }

    val cartTotalUsd: Double
        get() = CurrencyCalculator.roundTo(_cartItems.value.sumOf { it.subtotalUsd }, 2)

    val cartTotalVes: Double
        get() = CurrencyCalculator.roundTo(_cartItems.value.sumOf { it.subtotalVes }, 2)

    val paymentCalculation: StateFlow<CurrencyCalculator.PaymentCalculation> = combine(
        _cartItems,
        _inputPaidUsd,
        _inputPaidVes,
        activeRate
    ) { items, paidUsdText, paidVesText, rateObj ->
        val rate = rateObj?.valorTasa ?: 36.50
        val totalUsd = items.sumOf { it.subtotalUsd }
        val paidUsd = CurrencyCalculator.evalExpression(paidUsdText)
        val paidVes = CurrencyCalculator.evalExpression(paidVesText)
        CurrencyCalculator.computePayment(totalUsd, paidUsd, paidVes, rate)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CurrencyCalculator.computePayment(0.0, 0.0, 0.0, 36.50)
    )

    // --- ACCIONES DEL CARRITO ---
    fun addProductToCart(producto: Producto) {
        val rate = currentRateValue
        val (priceUsd, priceVes) = CurrencyCalculator.convertProductPrice(
            precioBase = producto.precioBase,
            monedaBase = producto.monedaBase,
            tasa = rate
        )

        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.producto.id == producto.id }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(
                cantidad = existing.cantidad + 1,
                unitPriceUsd = priceUsd,
                unitPriceVes = priceVes
            )
        } else {
            currentList.add(
                CartItem(
                    producto = producto,
                    cantidad = 1,
                    unitPriceUsd = priceUsd,
                    unitPriceVes = priceVes
                )
            )
        }
        _cartItems.value = currentList
    }

    fun removeOrDecrementCartItem(productoId: Long) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.producto.id == productoId }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            if (existing.cantidad > 1) {
                currentList[existingIndex] = existing.copy(cantidad = existing.cantidad - 1)
            } else {
                currentList.removeAt(existingIndex)
            }
            _cartItems.value = currentList
        }
    }

    fun removeCartItemCompletely(productoId: Long) {
        _cartItems.value = _cartItems.value.filter { it.producto.id != productoId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _inputPaidUsd.value = ""
        _inputPaidVes.value = ""
    }

    // --- MODIFICADORES DE PAGO ---
    fun setPaidUsd(text: String) {
        _inputPaidUsd.value = text
    }

    fun appendPaidUsdBill(amount: Double) {
        val current = CurrencyCalculator.evalExpression(_inputPaidUsd.value)
        val newAmount = current + amount
        _inputPaidUsd.value = if (newAmount % 1.0 == 0.0) newAmount.toInt().toString() else "%.2f".format(newAmount)
    }

    fun setExactUsdPayment() {
        val total = cartTotalUsd
        _inputPaidUsd.value = if (total % 1.0 == 0.0) total.toInt().toString() else "%.2f".format(total)
        _inputPaidVes.value = ""
    }

    fun setPaidVes(text: String) {
        _inputPaidVes.value = text
    }

    fun appendPaidVesAmount(amount: Double) {
        val current = CurrencyCalculator.evalExpression(_inputPaidVes.value)
        val newAmount = current + amount
        _inputPaidVes.value = if (newAmount % 1.0 == 0.0) newAmount.toInt().toString() else "%.2f".format(newAmount)
    }

    fun setExactVesPayment() {
        val total = cartTotalVes
        _inputPaidVes.value = if (total % 1.0 == 0.0) total.toInt().toString() else "%.2f".format(total)
        _inputPaidUsd.value = ""
    }

    fun setPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    // --- REGISTRAR Y COBRAR ---
    fun registerAndCharge(onSuccess: (Long) -> Unit = {}) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _saleFeedbackMessage.value = "El carrito está vacío"
            return
        }

        val rate = currentRateValue
        val calc = paymentCalculation.value

        if (!calc.isFullyPaid) {
            _saleFeedbackMessage.value = "Monto insuficiente. Falta ${CurrencyCalculator.formatUsd(calc.remainingUsd)} / ${CurrencyCalculator.formatVes(calc.remainingVes)}"
            return
        }

        viewModelScope.launch {
            val venta = Venta(
                fechaHora = System.currentTimeMillis(),
                tasaAplicada = rate,
                totalUsd = calc.totalUsd,
                totalVes = calc.totalVes,
                montoPagadoUsd = calc.paidUsd,
                montoPagadoVes = calc.paidVes,
                vueltoEntregadoUsd = calc.changeUsd,
                vueltoEntregadoVes = calc.changeVes,
                metodoPagoPrincipal = _selectedPaymentMethod.value
            )

            val detalles = items.map { item ->
                DetalleVenta(
                    ventaId = 0,
                    productoId = item.producto.id,
                    nombreProducto = item.producto.nombre,
                    cantidad = item.cantidad,
                    precioUnitarioUsd = item.unitPriceUsd,
                    precioUnitarioVes = item.unitPriceVes,
                    subtotalUsd = item.subtotalUsd,
                    subtotalVes = item.subtotalVes
                )
            }

            val ventaId = repository.processSale(venta, detalles)
            val completedSale = VentaConDetalles(
                venta = venta.copy(id = ventaId),
                detalles = detalles.map { it.copy(ventaId = ventaId) }
            )

            _lastSaleReceipt.value = completedSale
            _showReceiptDialog.value = true
            clearCart()
            _saleFeedbackMessage.value = "¡Cobro registrado exitosamente!"
            onSuccess(ventaId)
        }
    }

    // --- GESTIÓN DE TASA ---
    fun openEditRate() {
        _showEditRateDialog.value = true
    }

    fun closeEditRate() {
        _showEditRateDialog.value = false
    }

    fun saveRate(newRate: Double, source: String) {
        if (newRate > 0) {
            viewModelScope.launch {
                repository.updateRate(newRate, source)
                _showEditRateDialog.value = false
                // Actualizar precios unitarios del carrito con la nueva tasa
                val updatedCart = _cartItems.value.map { item ->
                    val (newUsd, newVes) = CurrencyCalculator.convertProductPrice(
                        precioBase = item.producto.precioBase,
                        monedaBase = item.producto.monedaBase,
                        tasa = newRate
                    )
                    item.copy(unitPriceUsd = newUsd, unitPriceVes = newVes)
                }
                _cartItems.value = updatedCart
            }
        }
    }

    // --- CONVERTIDOR RÁPIDO ---
    fun openQuickConverter() {
        _showQuickConverter.value = true
    }

    fun closeQuickConverter() {
        _showQuickConverter.value = false
    }

    // --- ESCÁNER DE CÓDIGO DE BARRAS ---
    fun openBarcodeScanner() {
        _showBarcodeScanner.value = true
    }

    fun closeBarcodeScanner() {
        _showBarcodeScanner.value = false
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode.trim())
            if (product != null) {
                addProductToCart(product)
                _saleFeedbackMessage.value = "Agregado: ${product.nombre}"
                _showBarcodeScanner.value = false
            } else {
                _saleFeedbackMessage.value = "Producto con código '$barcode' no encontrado"
            }
        }
    }

    // --- BÚSQUEDA Y FILTROS ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    // --- GESTIÓN DE PRODUCTOS ---
    fun openAddProduct() {
        _productToEdit.value = null
        _showAddEditProductDialog.value = true
    }

    fun openEditProduct(producto: Producto) {
        _productToEdit.value = producto
        _showAddEditProductDialog.value = true
    }

    fun closeAddEditProduct() {
        _productToEdit.value = null
        _showAddEditProductDialog.value = false
    }

    fun saveProduct(producto: Producto) {
        viewModelScope.launch {
            if (producto.id == 0L) {
                repository.insertProduct(producto)
            } else {
                repository.updateProduct(producto)
            }
            closeAddEditProduct()
        }
    }

    fun deleteProduct(producto: Producto) {
        viewModelScope.launch {
            repository.deleteProduct(producto)
            closeAddEditProduct()
        }
    }

    fun toggleFavorite(producto: Producto) {
        viewModelScope.launch {
            repository.toggleFavorite(producto)
        }
    }

    // --- RECIBO / TICKET ---
    fun showReceipt(ventaConDetalles: VentaConDetalles) {
        _lastSaleReceipt.value = ventaConDetalles
        _showReceiptDialog.value = true
    }

    fun closeReceipt() {
        _showReceiptDialog.value = false
    }

    fun dismissFeedback() {
        _saleFeedbackMessage.value = null
    }

    // --- CÁLCULO DE DÍAS DE MAYOR VENTA ---
    fun getSalesByDayOfWeek(): List<DaySalesAnalytics> {
        val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val calendar = java.util.Calendar.getInstance()
        val dayMap = mutableMapOf<Int, Triple<Double, Double, Int>>() // DayOfWeek (1=Dom, 2=Lun...) -> (USD, VES, count)

        for (i in 1..7) {
            dayMap[i] = Triple(0.0, 0.0, 0)
        }

        allSales.value.forEach { sale ->
            calendar.timeInMillis = sale.fechaHora
            val dow = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            val current = dayMap[dow] ?: Triple(0.0, 0.0, 0)
            dayMap[dow] = Triple(
                current.first + sale.totalUsd,
                current.second + sale.totalVes,
                current.third + 1
            )
        }

        // Orden de Lunes a Domingo: 2, 3, 4, 5, 6, 7, 1
        val order = listOf(
            java.util.Calendar.MONDAY to "Lun",
            java.util.Calendar.TUESDAY to "Mar",
            java.util.Calendar.WEDNESDAY to "Mié",
            java.util.Calendar.THURSDAY to "Jue",
            java.util.Calendar.FRIDAY to "Vie",
            java.util.Calendar.SATURDAY to "Sáb",
            java.util.Calendar.SUNDAY to "Dom"
        )

        return order.map { (calDay, label) ->
            val data = dayMap[calDay] ?: Triple(0.0, 0.0, 0)
            DaySalesAnalytics(
                dayName = label,
                totalUsd = CurrencyCalculator.roundTo(data.first, 2),
                totalVes = CurrencyCalculator.roundTo(data.second, 2),
                salesCount = data.third
            )
        }
    }
}
