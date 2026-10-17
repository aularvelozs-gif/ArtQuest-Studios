package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CobroAgilDao
import com.example.data.local.dao.ConfiguracionTasaDao
import com.example.data.local.dao.DetalleVentaDao
import com.example.data.local.dao.ProductoDao
import com.example.data.local.dao.VentaDao
import com.example.data.local.entity.ConfiguracionTasa
import com.example.data.local.entity.DetalleVenta
import com.example.data.local.entity.Producto
import com.example.data.local.entity.Venta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConfiguracionTasa::class,
        Producto::class,
        Venta::class,
        DetalleVenta::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configuracionTasaDao(): ConfiguracionTasaDao
    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao
    abstract fun detalleVentaDao(): DetalleVentaDao
    abstract fun cobroAgilDao(): CobroAgilDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cobroagil_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.cobroAgilDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: CobroAgilDao) {
            // 1. Tasa inicial
            dao.insertRate(
                ConfiguracionTasa(
                    valorTasa = 36.50,
                    fuente = "BCV",
                    fechaActualizacion = System.currentTimeMillis()
                )
            )

            // 2. Catálogo inicial de productos representativos para comercios
            val initialProducts = listOf(
                Producto(
                    codigoBarras = "759103100001",
                    nombre = "Harina P.A.N. 1kg",
                    categoria = "Víveres",
                    monedaBase = "USD",
                    precioBase = 1.15,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100002",
                    nombre = "Arroz Mary Tradicional 1kg",
                    categoria = "Víveres",
                    monedaBase = "USD",
                    precioBase = 1.25,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100003",
                    nombre = "Pasta Primor Corta 1kg",
                    categoria = "Víveres",
                    monedaBase = "USD",
                    precioBase = 1.30,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100004",
                    nombre = "Café Fama de América 250g",
                    categoria = "Café & Bebidas",
                    monedaBase = "USD",
                    precioBase = 2.40,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100005",
                    nombre = "Aceite Vegetal Mazeite 1L",
                    categoria = "Víveres",
                    monedaBase = "USD",
                    precioBase = 3.20,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100006",
                    nombre = "Azúcar Montalbán 1kg",
                    categoria = "Víveres",
                    monedaBase = "USD",
                    precioBase = 1.10,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100007",
                    nombre = "Leche Completa La Campiña 1L",
                    categoria = "Lácteos",
                    monedaBase = "USD",
                    precioBase = 2.10,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100008",
                    nombre = "Queso Blanco Llanero (kg)",
                    categoria = "Lácteos",
                    monedaBase = "VES",
                    precioBase = 182.50, // aprox $5 en VES
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100009",
                    nombre = "Cartón de Huevos (30 un)",
                    categoria = "Víveres",
                    monedaBase = "USD",
                    precioBase = 4.80,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100010",
                    nombre = "Refresco Pepsi 1.5L",
                    categoria = "Bebidas",
                    monedaBase = "USD",
                    precioBase = 1.50,
                    esFavorito = true
                ),
                Producto(
                    codigoBarras = "759103100011",
                    nombre = "Mantequilla Mavesa 500g",
                    categoria = "Lácteos",
                    monedaBase = "USD",
                    precioBase = 2.80,
                    esFavorito = false
                ),
                Producto(
                    codigoBarras = "759103100012",
                    nombre = "Atún Margarita 140g",
                    categoria = "Enlatados",
                    monedaBase = "USD",
                    precioBase = 1.65,
                    esFavorito = false
                ),
                Producto(
                    codigoBarras = "759103100013",
                    nombre = "Mayonesa Mavesa 445g",
                    categoria = "Salsas",
                    monedaBase = "USD",
                    precioBase = 2.30,
                    esFavorito = false
                ),
                Producto(
                    codigoBarras = "759103100014",
                    nombre = "Salsa de Tomate Pampero 397g",
                    categoria = "Salsas",
                    monedaBase = "USD",
                    precioBase = 1.45,
                    esFavorito = false
                ),
                Producto(
                    codigoBarras = "759103100015",
                    nombre = "Galletas Oreo Tubo",
                    categoria = "Golosinas",
                    monedaBase = "USD",
                    precioBase = 0.90,
                    esFavorito = false
                ),
                // Producto sin movimiento de prueba ("Producto hueso")
                Producto(
                    codigoBarras = "759103100016",
                    nombre = "Encurtidos Gourmet Frasco 500g",
                    categoria = "Conservas",
                    monedaBase = "USD",
                    precioBase = 4.50,
                    esFavorito = false
                )
            )

            dao.insertAllProducts(initialProducts)

            // 3. Crear un par de ventas de ejemplo para los reportes inmediatos
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            val v1Id = dao.insertVenta(
                Venta(
                    fechaHora = now - (dayMillis * 3), // hace 3 días
                    tasaAplicada = 36.50,
                    totalUsd = 12.50,
                    totalVes = 456.25,
                    montoPagadoUsd = 20.00,
                    montoPagadoVes = 0.00,
                    vueltoEntregadoUsd = 0.00,
                    vueltoEntregadoVes = 273.75,
                    metodoPagoPrincipal = "Efectivo USD"
                )
            )
            dao.insertDetalles(
                listOf(
                    DetalleVenta(
                        ventaId = v1Id,
                        productoId = 1L,
                        nombreProducto = "Harina P.A.N. 1kg",
                        cantidad = 5,
                        precioUnitarioUsd = 1.15,
                        precioUnitarioVes = 41.98,
                        subtotalUsd = 5.75,
                        subtotalVes = 209.90
                    ),
                    DetalleVenta(
                        ventaId = v1Id,
                        productoId = 4L,
                        nombreProducto = "Café Fama de América 250g",
                        cantidad = 2,
                        precioUnitarioUsd = 2.40,
                        precioUnitarioVes = 87.60,
                        subtotalUsd = 4.80,
                        subtotalVes = 175.20
                    ),
                    DetalleVenta(
                        ventaId = v1Id,
                        productoId = 10L,
                        nombreProducto = "Refresco Pepsi 1.5L",
                        cantidad = 1,
                        precioUnitarioUsd = 1.50,
                        precioUnitarioVes = 54.75,
                        subtotalUsd = 1.50,
                        subtotalVes = 54.75
                    )
                )
            )

            val v2Id = dao.insertVenta(
                Venta(
                    fechaHora = now - (dayMillis * 1), // ayer
                    tasaAplicada = 36.50,
                    totalUsd = 9.25,
                    totalVes = 337.63,
                    montoPagadoUsd = 5.00,
                    montoPagadoVes = 155.13,
                    vueltoEntregadoUsd = 0.00,
                    vueltoEntregadoVes = 0.00,
                    metodoPagoPrincipal = "Mixto"
                )
            )
            dao.insertDetalles(
                listOf(
                    DetalleVenta(
                        ventaId = v2Id,
                        productoId = 1L,
                        nombreProducto = "Harina P.A.N. 1kg",
                        cantidad = 3,
                        precioUnitarioUsd = 1.15,
                        precioUnitarioVes = 41.98,
                        subtotalUsd = 3.45,
                        subtotalVes = 125.94
                    ),
                    DetalleVenta(
                        ventaId = v2Id,
                        productoId = 5L,
                        nombreProducto = "Aceite Vegetal Mazeite 1L",
                        cantidad = 1,
                        precioUnitarioUsd = 3.20,
                        precioUnitarioVes = 116.80,
                        subtotalUsd = 3.20,
                        subtotalVes = 116.80
                    ),
                    DetalleVenta(
                        ventaId = v2Id,
                        productoId = 2L,
                        nombreProducto = "Arroz Mary Tradicional 1kg",
                        cantidad = 2,
                        precioUnitarioUsd = 1.25,
                        precioUnitarioVes = 45.63,
                        subtotalUsd = 2.50,
                        subtotalVes = 91.25
                    )
                )
            )

            val v3Id = dao.insertVenta(
                Venta(
                    fechaHora = now - 3600000L * 2, // hoy hace 2 horas
                    tasaAplicada = 36.50,
                    totalUsd = 4.80,
                    totalVes = 175.20,
                    montoPagadoUsd = 0.00,
                    montoPagadoVes = 175.20,
                    vueltoEntregadoUsd = 0.00,
                    vueltoEntregadoVes = 0.00,
                    metodoPagoPrincipal = "Pago Móvil"
                )
            )
            dao.insertDetalles(
                listOf(
                    DetalleVenta(
                        ventaId = v3Id,
                        productoId = 9L,
                        nombreProducto = "Cartón de Huevos (30 un)",
                        cantidad = 1,
                        precioUnitarioUsd = 4.80,
                        precioUnitarioVes = 175.20,
                        subtotalUsd = 4.80,
                        subtotalVes = 175.20
                    )
                )
            )
        }
    }
}
