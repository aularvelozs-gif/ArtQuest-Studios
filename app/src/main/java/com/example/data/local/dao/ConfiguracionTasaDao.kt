package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ConfiguracionTasa
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionTasaDao {

    @Query("SELECT * FROM Configuracion_Tasa ORDER BY fecha_actualizacion DESC LIMIT 1")
    fun getActiveRate(): Flow<ConfiguracionTasa?>

    @Query("SELECT * FROM Configuracion_Tasa ORDER BY fecha_actualizacion DESC")
    fun getRateHistory(): Flow<List<ConfiguracionTasa>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(tasa: ConfiguracionTasa): Long
}
