package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "Configuracion_Tasa")
data class ConfiguracionTasa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "valor_tasa")
    val valorTasa: Double,
    @ColumnInfo(name = "fuente")
    val fuente: String = "Manual", // "BCV", "Paralelo", "Manual"
    @ColumnInfo(name = "fecha_actualizacion")
    val fechaActualizacion: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "Productos",
    indices = [Index(value = ["codigo_barras"], unique = true)]
)
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "codigo_barras")
    val codigoBarras: String? = null,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "categoria")
    val categoria: String = "General",
    @ColumnInfo(name = "moneda_base")
    val monedaBase: String = "USD", // "USD" o "VES"
    @ColumnInfo(name = "precio_base")
    val precioBase: Double,
    @ColumnInfo(name = "es_favorito")
    val esFavorito: Boolean = false,
    @ColumnInfo(name = "activo")
    val activo: Boolean = true
)

@Entity(tableName = "Ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "fecha_hora")
    val fechaHora: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "tasa_aplicada")
    val tasaAplicada: Double,
    @ColumnInfo(name = "total_usd")
    val totalUsd: Double,
    @ColumnInfo(name = "total_ves")
    val totalVes: Double,
    @ColumnInfo(name = "monto_pagado_usd")
    val montoPagadoUsd: Double = 0.0,
    @ColumnInfo(name = "monto_pagado_ves")
    val montoPagadoVes: Double = 0.0,
    @ColumnInfo(name = "vuelto_entregado_usd")
    val vueltoEntregadoUsd: Double = 0.0,
    @ColumnInfo(name = "vuelto_entregado_ves")
    val vueltoEntregadoVes: Double = 0.0,
    @ColumnInfo(name = "metodo_pago_principal")
    val metodoPagoPrincipal: String = "Efectivo USD" // "Efectivo USD", "Pago Móvil", "Punto de Venta", "Mixto"
)

@Entity(
    tableName = "Detalle_Venta",
    foreignKeys = [
        ForeignKey(
            entity = Venta::class,
            parentColumns = ["id"],
            childColumns = ["venta_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["venta_id"]),
        Index(value = ["producto_id"])
    ]
)
data class DetalleVenta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "venta_id")
    val ventaId: Long,
    @ColumnInfo(name = "producto_id")
    val productoId: Long,
    @ColumnInfo(name = "nombre_producto")
    val nombreProducto: String = "",
    @ColumnInfo(name = "cantidad")
    val cantidad: Int,
    @ColumnInfo(name = "precio_unitario_usd")
    val precioUnitarioUsd: Double,
    @ColumnInfo(name = "precio_unitario_ves")
    val precioUnitarioVes: Double,
    @ColumnInfo(name = "subtotal_usd")
    val subtotalUsd: Double,
    @ColumnInfo(name = "subtotal_ves")
    val subtotalVes: Double
)

data class VentaConDetalles(
    @Embedded
    val venta: Venta,
    @Relation(
        parentColumn = "id",
        entityColumn = "venta_id"
    )
    val detalles: List<DetalleVenta>
)

data class TopProductoVendido(
    @ColumnInfo(name = "producto_id")
    val productoId: Long,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "total_unidades")
    val totalUnidades: Int,
    @ColumnInfo(name = "total_usd")
    val totalUsd: Double,
    @ColumnInfo(name = "total_ves")
    val totalVes: Double
)

data class MetodoPagoResumen(
    @ColumnInfo(name = "metodo_pago_principal")
    val metodo: String,
    @ColumnInfo(name = "conteo_ventas")
    val conteoVentas: Int,
    @ColumnInfo(name = "total_usd")
    val totalUsd: Double,
    @ColumnInfo(name = "total_ves")
    val totalVes: Double
)
