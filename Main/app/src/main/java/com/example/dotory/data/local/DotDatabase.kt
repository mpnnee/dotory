package com.example.dotory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DotEntity::class], version = 1, exportSchema = false)
abstract class DotDatabase : RoomDatabase() {
    abstract fun dotDao(): DotDao

    companion object {
        @Volatile private var INSTANCE: DotDatabase? = null
        fun getDatabase(context: Context): DotDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DotDatabase::class.java,
                    "dot_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
