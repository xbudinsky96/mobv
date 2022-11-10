package com.example.zadanie.model

import androidx.room.Entity
import androidx.room.PrimaryKey

lateinit var loggedInUser: User

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = false)
    val uid: String,
    val name: String,
    val access: String,
    val refresh: String
)