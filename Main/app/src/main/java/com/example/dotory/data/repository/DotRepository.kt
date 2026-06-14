package com.example.dotory.data.repository

import com.example.dotory.data.local.DotDao
import com.example.dotory.data.local.DotEntity
import com.example.dotory.data.model.Dot
import com.example.dotory.data.model.DotCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DotRepository(private val dotDao: DotDao) {

    fun getAllDots(): Flow<List<Dot>> {
        return dotDao.getAllDots().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getDotsByCategory(category: DotCategory): Flow<List<Dot>> {
        return dotDao.getDotsByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertDot(dot: Dot): Long = withContext(Dispatchers.IO) {
        dotDao.insertDot(dot.toEntity())
    }

    suspend fun getDotById(id: Long): Dot? = withContext(Dispatchers.IO) {
        dotDao.getDotById(id)?.toDomain()
    }

    suspend fun updateDot(dot: Dot) = withContext(Dispatchers.IO) {
        dotDao.updateDot(dot.toEntity())
    }

    suspend fun deleteDot(dot: Dot) = withContext(Dispatchers.IO) {
        dotDao.deleteDot(dot.toEntity())
    }

    // Entity -> Domain Model 변환 확장 함수
    private fun DotEntity.toDomain(): Dot {
        val cat = try {
            DotCategory.valueOf(category)
        } catch (e: IllegalArgumentException) {
            DotCategory.ETC
        }
        return Dot(
            id = id,
            latitude = latitude,
            longitude = longitude,
            title = title,
            comment = comment,
            photoUri = photoUri,
            category = cat,
            createdAt = createdAt
        )
    }

    // Domain Model -> Entity 변환 확장 함수
    private fun Dot.toEntity(): DotEntity {
        return DotEntity(
            id = id,
            latitude = latitude,
            longitude = longitude,
            title = title,
            comment = comment,
            photoUri = photoUri,
            category = category.name,
            createdAt = createdAt
        )
    }
}
