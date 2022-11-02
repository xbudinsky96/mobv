package com.example.zadanie.`interface`

import com.example.zadanie.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @POST("find")
    @Headers(
        "Access-Control-Request-Headers: *",
        "api-key: KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M",
        "Content-Type: application/json"
    )
    fun getAmenities(@Body post : Post): Call<Companies>

    @GET("home_feed")
    fun getUsers() : Call<Entity>
}