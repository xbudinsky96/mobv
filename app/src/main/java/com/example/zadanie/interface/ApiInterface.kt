package com.example.zadanie.`interface`

import com.example.zadanie.model.*
import retrofit2.Call
import retrofit2.http.*

const val apiKey = "c95332ee022df8c953ce470261efc695ecf3e784"
interface ApiInterface {

    @POST("user/create.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun register(@Body register: PostCredentials): Call<User>

    @POST("user/login.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun login(@Body login: PostCredentials): Call<User>

    @POST("user/refresh.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun refreshToken(@Body credentials: PostRefreshToken, @Header("x-user") uid: String): Call<User>

    @GET("interpreter")
    fun getNearbyCompanies(@Query("data") data: String): Call<Company>

    @GET("interpreter")
    fun getCompanyByID(@Query("data") data: String): Call<Company>

    @GET("bar/list.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun getCompaniesWithMembers(@Header("x-user") uid: String, @Header("authorization") auth: String): Call<MutableList<CompanyWithMembers>>

    @POST("bar/message.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun checkInCompany(@Body credentials: PostLoginCompany, @Header("x-user") uid: String, @Header("authorization") auth: String): Call<CheckInResponse>

    @POST("bar/message.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun checkOutCompany(@Body credentials: PostLogoutCompany, @Header("x-user") uid: String, @Header("authorization") auth: String): Call<String>

    @POST("contact/message.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun addFriend(@Body contact: PostAddDeleteUser, @Header("x-user") uid: String, @Header("authorization") auth: String): Call<Void>

    @POST("contact/delete.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun deleteFriend(@Body contact: PostAddDeleteUser, @Header("x-user") uid: String, @Header("authorization") auth: String): Call<Void>

    @GET("contact/list.php")
    @Headers(
        "Accept: application/json",
        "Cache-Control: no-cache",
        "Content-Type: application/json",
        "x-apikey: $apiKey"
    )
    fun showFriends(@Header("x-user") uid: String, @Header("authorization") auth: String): Call<MutableList<Friend>>
}