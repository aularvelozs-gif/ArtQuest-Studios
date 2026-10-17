package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.MetodoPagoResumen
import com.example.data.local.entity.Venta
import com.example.data.local.entity.VentaConDetalles
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenta(venta: Venta): Long

    @Transaction
    @Query("SELECT * FROM Ventas ORDER BY fecha_hora DESC")
    fun getAllVentasConDetalles(): Flow<List<VentaConDetalles>>

    @Transaction
    @Query("SELECT * FROM Ventas WHERE id = :id")
    suspend fun getVentaConDetallesById(id: Long): VentaConDetalles?

    @Query("SELECT * FROM Ventas ORDER BY fecha_hora DESC")
    fun getAllVentas(): Flow<List<Venta>>

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
