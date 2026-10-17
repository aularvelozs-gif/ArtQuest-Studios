package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.CobroAgilDao
import com.example.data.local.entity.ConfiguracionTasa
import com.example.data.local.entity.DetalleVenta
import com.example.data.local.entity.MetodoPagoResumen
import com.example.data.local.entity.Producto
import com.example.data.local.entity.TopProductoVendido
import com.example.data.local.entity.Venta
import com.example.data.local.entity.VentaConDetalles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CobroAgilRepository(private val dao: CobroAgilDao) {

    val activeRate: Flow<ConfiguracionTasa?> = dao.getActiveRate()
    val rateHistory: Flow<List<ConfiguracionTasa>> = dao.getRateHistory()
    val allProducts: Flow<List<Producto>> = dao.getAllProducts()
    val activeProducts: Flow<List<Producto>> = dao.getActiveProducts()
    val favoriteProducts: Flow<List<Producto>> = dao.getFavoriteProducts()
    val salesWithDetails: Flow<List<VentaConDetalles>> = dao.getAllVentasConDetalles()
    val allSales: Flow<List<Venta>> = dao.getAllVentas()
    val topSoldProducts: Flow<List<TopProductoVendido>> = dao.getTopSoldProducts(10)
    val salesByPaymentMethod: Flow<List<MetodoPagoResumen>> = dao.getSalesByPaymentMethod()

    fun searchProducts(query: String): Flow<List<Producto>> = dao.searchProducts(query)

    fun getIdleProducts(daysAgo: Int = 30): Flow<List<Producto>> {
        val thirtyDaysMillis = daysAgo * 24L * 60 * 60 * 1000
        val sinceTime = System.currentTimeMillis() - thirtyDaysMillis
        return dao.getIdleProducts(sinceTime)
    }

    suspend fun getProductByBarcode(barcode: String): Producto? = withContext(Dispatchers.IO) {
        dao.getProductByBarcode(barcode)
    }

    suspend fun updateRate(valor: Double, fuente: String): Long = withContext(Dispatchers.IO) {
        dao.insertRate(
            ConfiguracionTasa(
                valorTasa = valor,
                fuente = fuente,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
    }

    suspend fun insertProduct(producto: Producto): Long = withContext(Dispatchers.IO) {
        dao.insertProduct(producto)
    }

    suspend fun updateProduct(producto: Producto) = withContext(Dispatchers.IO) {
        dao.updateProduct(producto)
    }

    suspend fun deleteProduct(producto: Producto) = withContext(Dispatchers.IO) {
        dao.deleteProduct(producto)
    }

    suspend fun toggleFavorite(producto: Producto) = withContext(Dispatchers.IO) {
        dao.updateProduct(producto.copy(esFavorito = !producto.esFavorito))
    }

    suspend fun processSale(venta: Venta, items: List<DetalleVenta>): Long = withContext(Dispatchers.IO) {
        val ventaId = dao.insertVenta(venta)
        val detallesConVentaId = items.map { it.copy(ventaId = ventaId) }
        dao.insertDetalles(detallesConVentaId)
        ventaId
    }

    suspend fun ensureInitialData() = withContext(Dispatchers.IO) {
        if (dao.countProducts() == 0) {
            AppDatabase.populateInitialData(dao)
        }
    }
}
