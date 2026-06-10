package com.example.dotory.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DotDao {
    @Query("SELECT * FROM dots ORDER BY createdAt DESC")
    fun getAllDots(): Flow<List<DotEntity>>

    @Query("SELECT * FROM dots WHERE category = :category ORDER BY createdAt DESC")
    fun getDotsByCategory(category: String): Flow<List<DotEntity>>

    @Query("SELECT * FROM dots WHERE id = :id")
    fun getDotById(id: Long): DotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDot(dot: DotEntity): Long

    @Update
    fun updateDot(dot: DotEntity)

    @Delete
    fun deleteDot(dot: DotEntity)
}
