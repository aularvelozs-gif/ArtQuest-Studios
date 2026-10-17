package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

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
}
