package com.example.zadanie.model

data class User(
    val videos: MutableList<Video>
)

data class Video(
    val id: Long,
    val name: String
)