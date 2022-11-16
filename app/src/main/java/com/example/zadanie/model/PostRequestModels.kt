package com.example.zadanie.model

data class PostCredentials(
    val name: String,
    val password: String
)

data class PostRefreshToken(
    val refresh: String
)

data class PostLoginCompany(
    val id: String,
    val name: String,
    val type: String,
    val lat: Double,
    val lon: Double
)

data class PostLogoutCompany(
    val id: String,
    val name: String,
    val type: String,
    val lat: Double,
    val lon: Double
)

data class PostAddDeleteUser(
    val contact: String
)