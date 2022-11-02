package com.example.zadanie.data

import android.content.Context
import com.example.zadanie.`interface`.ApiInterface
import com.example.zadanie.model.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

class CompanyDataSource {
    fun getCompanies(context: Context): Company? {
        val jsonString = loadJson(context)
        fetchCompaniesFromAPI()
        fetchData()
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

    private fun fetchCompaniesFromAPI() {
        val url = "https://api.letsbuildthatapp.com/youtube/"
        val retrofitBuilderUsers = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(ApiInterface::class.java)

        val retrofitUsers = retrofitBuilderUsers.getUsers()

        retrofitUsers.enqueue(object: Callback<Entity> {
            override fun onResponse(call: Call<Entity>, response: Response<Entity>) {
                val body = response.body()
                println(body)
            }

            override fun onFailure(call: Call<Entity>, t: Throwable) {
                println("fail")
            }

        })
    }

    fun fetchData() {
        val retrofitBuilderCompany = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://data.mongodb-api.com/app/data-fswjp/endpoint/data/v1/action/")
            .build()
            .create(ApiInterface::class.java)

        val companies = retrofitBuilderCompany.getAmenities(Post("bars", "mobvapp", "Cluster0"))

        //println(companies.body())

        companies.enqueue(object: Callback<Companies> {
            override fun onResponse(call: Call<Companies>, response: Response<Companies>) {
                val body = response.body()
                println(body)
            }

            override fun onFailure(call: Call<Companies>, t: Throwable) {
                println("API fail")
            }
        })
    }
}