package com.example.zadanie.model

data class PostPub(
    val collection: String,
    val database: String,
    val dataSource: String
)

data class PostCredentials(
    val api_key: String,
    val name: String,
    val password: String
)

data class PostRefreshToken(
    val api_key: String,
    val uid: String,
    val refresh: String
)

data class PostPubsWithMembers(
    val api_key: String,
    val uid: String
)

data class PostLoginLogoutCompany(
    val api_key: String,
    val uid: String,
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double
)