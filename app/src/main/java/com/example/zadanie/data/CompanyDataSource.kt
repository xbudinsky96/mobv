package com.example.zadanie.data

import android.content.Context
import com.example.zadanie.model.Company
import com.google.gson.Gson
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
}