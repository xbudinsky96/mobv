package com.example.zadanie.data

import android.content.Context
import android.widget.Toast
import com.example.zadanie.`interface`.ApiInterface
import com.example.zadanie.model.*
import com.google.gson.Gson
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

class CompanyDataSource {
    fun getCompanies(context: Context): Company? {
        val jsonString = loadJson(context)
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

    fun fetchData(companyViewModel: CompanyViewModel, context: Context) {
        val retrofitBuilderCompany = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://data.mongodb-api.com/app/data-fswjp/endpoint/data/v1/action/")
            .build()
            .create(ApiInterface::class.java)

        val companies = retrofitBuilderCompany.getAmenities(Post("bars", "mobvapp", "Cluster0"))

        companies.enqueue(object: Callback<Companies> {
            override fun onResponse(call: Call<Companies>, response: Response<Companies>) {
                val body = response.body()
                if (body != null) {
                    insertDataToDataBase(companyViewModel, body.documents)
                }
                else {
                    Toast.makeText(context, "Couldn't fetch data!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Companies>, t: Throwable) {
                Toast.makeText(context, "Couldn't fetch data!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun insertDataToDataBase(companyViewModel: CompanyViewModel, elements: MutableList<Element>) {
        elements.forEach { element -> companyViewModel.addCompany(element) }
    }
}