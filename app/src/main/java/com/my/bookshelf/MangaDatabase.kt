/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.my.bookshelf

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [MangaEntity::class, SpecialSeriesEntity::class, WishlistItemEntity::class, CategoryEntity::class], version = 6)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun specialSeriesDao(): SpecialSeriesDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MangaDatabase? = null

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE manga_table ADD COLUMN audioNoteUri TEXT")
                db.execSQL("ALTER TABLE manga_table ADD COLUMN audioNoteUpdatedAt INTEGER")
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE manga_table ADD COLUMN audioNoteEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS wishlist_table (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        dateAdded INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS category_table (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO category_table (id, name, sortOrder)
                    VALUES ('${CategoryEntity.DEFAULT_ID}', '${CategoryEntity.DEFAULT_NAME}', 0)
                """.trimIndent())
                db.execSQL("""
                    ALTER TABLE manga_table
                    ADD COLUMN categoryId TEXT NOT NULL DEFAULT '${CategoryEntity.DEFAULT_ID}'
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): MangaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MangaDatabase::class.java,
                    "manga_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}



