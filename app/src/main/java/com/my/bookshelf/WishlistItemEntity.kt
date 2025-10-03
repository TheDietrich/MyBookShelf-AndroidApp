/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */
package com.my.bookshelf

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "wishlist_table")
data class WishlistItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dateAdded: Long = System.currentTimeMillis()
)
