package com.example.zadanie.model

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey

lateinit var loggedInUser: User

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = false)
    val uid: String,
    val name: String,
    val access: String,
    val refresh: String,
    var lat: Double,
    var lon: Double
)

data class Friend(
    val user_id: String,
    val user_name: String,
    val bar_id: String,
    val bar_name: String,
    val time: String,
    val bar_lat: String,
    val bar_lon: String
)