package com.example.zadanie.data

import android.content.Context
import com.example.zadanie.`interface`.ApiInterface
import com.example.zadanie.model.Company
import com.example.zadanie.model.User
import com.example.zadanie.model.Video
import com.google.gson.Gson
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InputStream

class CompanyDataSource {
    fun getCompanies(context: Context): Company? {
        val jsonString = loadJson(context)
        //val users = fetchJson()
        return Gson().fromJson(jsonString, Company::class.java)
    }

    private fun loadJson(context: Context): String {
        var input: InputStream? = null
        val jsonString: String

        try {
            // Create InputStream
            input = context.assets.open("pubs.json")
            val size = input.available()
            // Create a buffer with the size
            val buffer = ByteArray(size)
            // Read data from InputStream into the Buffer
            input.read(buffer)
            // Create a json String
            jsonString = String(buffer)
            return jsonString
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            // Must close the stream
            input?.close()
        }

        return ""
    }

    private fun fetchJson() {
        //val url = "https://data.mongodb-api.com/app/data-fswjp/endpoint/data/v1/action/find&appid=KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M"
        val url = "https://api.letsbuildthatapp.com/youtube/home_feed"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().toString()
                println(body)
                //val gson = Gson().fromJson(body, Company::class.java)
                val gson = Gson().fromJson(body, User::class.java)
                println(gson)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("fail")
            }
        })
    }

    private fun fetchCompaniesFromAPI() {
        val url = "https://api.letsbuildthatapp.com/youtube/"
        val retrofitBuilderUsers = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            //.baseUrl("https://data.mongodb-api.com/app/KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M/data-fswjp/endpoint/data/v1/action/")
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)

        val retrofitBuilderCompany = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://data.mongodb-api.com/app/KHUu1Fo8042UwzczKz9nNeuVOsg2T4ClIfhndD2Su0G0LHHCBf0LnUF05L231J0M/data-fswjp/endpoint/data/v1/action/")
            .build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilderCompany.getCompany()

        retrofitData.enqueue(object: retrofit2.Callback<Company> {
            override fun onResponse(call: retrofit2.Call<Company>, response: retrofit2.Response<Company>) {
                val body = response.body()
                println(body)
            }

            override fun onFailure(call: retrofit2.Call<Company>, t: Throwable) {
                println("Failed to fetch data")
            }

        })

        val retrofitUsers = retrofitBuilderUsers.getUsers()

        retrofitUsers.enqueue(object: retrofit2.Callback<User> {
            override fun onResponse(call: retrofit2.Call<User>, response: retrofit2.Response<User>) {
                val body = response.body()
                println(body?.videos)
            }

            override fun onFailure(call: retrofit2.Call<User>, t: Throwable) {
                println("fail")
            }

        })
    }
}