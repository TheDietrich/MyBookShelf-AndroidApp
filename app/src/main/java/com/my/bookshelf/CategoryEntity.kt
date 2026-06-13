package com.my.bookshelf

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "category_table")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sortOrder: Int = 0
) {
    companion object {
        const val DEFAULT_ID = "00000000-0000-0000-0000-000000000001"
        const val DEFAULT_NAME = "Allgemein"
    }
}
