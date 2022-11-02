package com.example.zadanie.`interface`

import com.example.zadanie.model.Company
import com.example.zadanie.model.Element
import com.example.zadanie.model.Post
import com.example.zadanie.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers("Access-Control-Request-Headers: *", "api-key: KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M", "Content-Type: application/json")
    @POST("find")
    fun getCompany() : Call<MutableList<Element>>

    @Headers("Access-Control-Request-Headers: *", "api-key: KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M", "Content-Type: application/json")
    @POST("find")
    suspend fun create(@Body post : Post): Call<Company>

    @GET("home_feed")
    fun getUsers() : Call<User>
}