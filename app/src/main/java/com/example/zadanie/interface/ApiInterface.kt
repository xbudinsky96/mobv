package com.example.zadanie.`interface`

import com.example.zadanie.model.Company
import com.example.zadanie.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @POST("find")
    fun getCompany() : Call<Company>

    @GET("home_feed")
    fun getUsers() : Call<User>
}