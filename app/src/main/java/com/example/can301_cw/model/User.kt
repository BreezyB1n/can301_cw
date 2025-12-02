package com.example.can301_cw.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
import java.util.Date

/**
 * User model for user account information.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var username: String = "",
    var email: String = "",
    var password: String = "", 
    var avatarPath: String? = null,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date()
)

