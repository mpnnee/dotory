package com.example.dotory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dots")
data class DotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val comment: String,
    val photoUri: String,
    val category: String,        // DotCategory.name 형태로 저장
    val createdAt: Long
)
