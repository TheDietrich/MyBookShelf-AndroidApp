package com.example.mybooksheelf

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MangaEntity::class], version = 1, exportSchema = false)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao

    companion object {
        @Volatile
        private var INSTANCE: MangaDatabase? = null

        fun getDatabase(app: Application): MangaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    app,
                    MangaDatabase::class.java,
                    "manga_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
