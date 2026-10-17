package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ConfiguracionTasa
import com.example.data.local.entity.DetalleVenta
import com.example.data.local.entity.MetodoPagoResumen
import com.example.data.local.entity.Producto
import com.example.data.local.entity.TopProductoVendido
import com.example.data.local.entity.Venta
import com.example.data.local.entity.VentaConDetalles
import kotlinx.coroutines.flow.Flow

@Dao
interface CobroAgilDao {

    // --- TASAS DE CAMBIO ---
    @Query("SELECT * FROM Configuracion_Tasa ORDER BY fecha_actualizacion DESC LIMIT 1")
    fun getActiveRate(): Flow<ConfiguracionTasa?>

    @Query("SELECT * FROM Configuracion_Tasa ORDER BY fecha_actualizacion DESC")
    fun getRateHistory(): Flow<List<ConfiguracionTasa>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(tasa: ConfiguracionTasa): Long

    // --- PRODUCTOS ---
    @Query("SELECT * FROM Productos ORDER BY nombre ASC")
    fun getAllProducts(): Flow<List<Producto>>

    @Query("SELECT * FROM Productos WHERE activo = 1 ORDER BY nombre ASC")
    fun getActiveProducts(): Flow<List<Producto>>

    @Query("SELECT * FROM Productos WHERE es_favorito = 1 AND activo = 1 ORDER BY nombre ASC LIMIT 10")
    fun getFavoriteProducts(): Flow<List<Producto>>

    @Query("SELECT * FROM Productos WHERE id = :id")
    suspend fun getProductById(id: Long): Producto?

    @Query("SELECT * FROM Productos WHERE codigo_barras = :barcode AND activo = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Producto?

    @Query("SELECT * FROM Productos WHERE activo = 1 AND (nombre LIKE '%' || :query || '%' OR codigo_barras LIKE '%' || :query || '%' OR categoria LIKE '%' || :query || '%') ORDER BY nombre ASC")
    fun searchProducts(query: String): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(producto: Producto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(productos: List<Producto>)

    @Update
    suspend fun updateProduct(producto: Producto)

    @Delete
    suspend fun deleteProduct(producto: Producto)

    @Query("SELECT COUNT(*) FROM Productos")
    suspend fun countProducts(): Int

    // --- VENTAS & DETALLES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenta(venta: Venta): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalles(detalles: List<DetalleVenta>)

    @Transaction
    @Query("SELECT * FROM Ventas ORDER BY fecha_hora DESC")
    fun getAllVentasConDetalles(): Flow<List<VentaConDetalles>>

    @Transaction
    @Query("SELECT * FROM Ventas WHERE id = :id")
    suspend fun getVentaConDetallesById(id: Long): VentaConDetalles?

    @Query("SELECT * FROM Ventas ORDER BY fecha_hora DESC")
    fun getAllVentas(): Flow<List<Venta>>

    // --- ANALÍTICA ---
    @Query("""
        SELECT d.producto_id, 
               CASE WHEN p.nombre IS NOT NULL THEN p.nombre ELSE d.nombre_producto END AS nombre,
               SUM(d.cantidad) AS total_unidades,
               SUM(d.subtotal_usd) AS total_usd,
               SUM(d.subtotal_ves) AS total_ves
        FROM Detalle_Venta d
        LEFT JOIN Productos p ON d.producto_id = p.id
        GROUP BY d.producto_id, nombre
        ORDER BY total_unidades DESC, total_usd DESC
        LIMIT :limit
    """)
    fun getTopSoldProducts(limit: Int = 10): Flow<List<TopProductoVendido>>

    @Query("""
        SELECT * FROM Productos 
        WHERE activo = 1 
          AND id NOT IN (
              SELECT DISTINCT d.producto_id 
              FROM Detalle_Venta d
              INNER JOIN Ventas v ON d.venta_id = v.id
              WHERE v.fecha_hora >= :sinceTime
          )
        ORDER BY nombre ASC
    """)
    fun getIdleProducts(sinceTime: Long): Flow<List<Producto>>

    @Query("""
        SELECT metodo_pago_principal, 
               COUNT(*) AS conteo_ventas,
               SUM(total_usd) AS total_usd, 
               SUM(total_ves) AS total_ves
        FROM Ventas
        GROUP BY metodo_pago_principal
        ORDER BY total_usd DESC
    """)
    fun getSalesByPaymentMethod(): Flow<List<MetodoPagoResumen>>
}
