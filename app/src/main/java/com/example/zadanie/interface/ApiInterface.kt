package com.example.zadanie.`interface`

import com.example.zadanie.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val apiKey = "KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M"
interface ApiInterface {

    @POST("find")
    @Headers(
        "Access-Control-Request-Headers: *",
        "api-key: $apiKey",
        "Content-Type: application/json"
    )
    fun getAmenities(@Body postPub : PostPub): Call<Companies>

    @POST
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json"
    )
    fun register()
}