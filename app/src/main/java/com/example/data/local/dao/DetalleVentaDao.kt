package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DetalleVenta
import com.example.data.local.entity.Producto
import com.example.data.local.entity.TopProductoVendido
import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleVentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalles(detalles: List<DetalleVenta>)

    @Query("SELECT * FROM Detalle_Venta WHERE venta_id = :ventaId")
    fun getDetallesByVenta(ventaId: Long): Flow<List<DetalleVenta>>

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
}
