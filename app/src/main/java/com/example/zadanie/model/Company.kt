package com.example.zadanie.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Company(
    val elements: MutableList<Element>
)

data class Companies(
    val documents: MutableList<Element>
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
    val website: String,
    val opening_hours: String
)

data class CompanyWithMembers(
    val bar_id: String,
    val bar_name: String,
    val lat: String,
    val lon: String,
    val bar_type: String,
    val users: String
)