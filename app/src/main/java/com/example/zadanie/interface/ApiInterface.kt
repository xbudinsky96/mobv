package com.example.zadanie.`interface`

import com.example.zadanie.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

const val apiKey = "KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M"
interface ApiInterface {

    @POST("find")
    @Headers(
        "Access-Control-Request-Headers: *",
        "api-key: $apiKey",
        "Content-Type: application/json"
    )
    fun getPubs(@Body postPub : PostPub): Call<Companies>

    @POST("create.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json"
    )
    fun register(@Body register: PostCredentials): Call<String>

    @POST("login.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json"
    )
    fun login(@Body login: PostCredentials): Call<String>

    @GET("interpreter")
    fun getNearbyCompanies(@Query("data") data: String): Call<Company>

    @GET("interpreter")
    fun getCompanyByID(@Query("data") data: String): Call<Company>

    @POST("refresh.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json"
    )
    fun refreshToken(@Body credentials: PostRefreshToken): Response<String>
}