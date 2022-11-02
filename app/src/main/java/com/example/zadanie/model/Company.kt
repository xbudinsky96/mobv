package com.example.zadanie.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Company(
    val elements: MutableList<Element>
)

@Entity(tableName = "company_table")
data class Element(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Tags,
    val type: String
)

@Entity(tableName = "tag_table")
data class Tags(
    val name: String,
    val amenity: String,
    val phone: String,
    val description: String,
    val email: String,
    val food: String,
    val url: String,
    val website: String,
    val opening_hours: String
)