/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.my.bookshelf

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [MangaEntity::class, SpecialSeriesEntity::class], version = 4)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun specialSeriesDao(): SpecialSeriesDao

    companion object {
        @Volatile
        private var INSTANCE: MangaDatabase? = null

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE manga_table ADD COLUMN audioNoteUri TEXT")
                db.execSQL("ALTER TABLE manga_table ADD COLUMN audioNoteUpdatedAt INTEGER")
            }
        }

        // NEU: v3 -> v4 (boolean als INTEGER NOT NULL DEFAULT 0)
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE manga_table ADD COLUMN audioNoteEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): MangaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MangaDatabase::class.java,
                    "manga_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4) // NEU
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


