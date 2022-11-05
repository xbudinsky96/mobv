package com.example.zadanie.model

data class PostPub(
    val collection: String,
    val database: String,
    val dataSource: String
)

data class PostRegister(
    val api_key: String = "asd",
    val database: String,
    val dataSource: String
)