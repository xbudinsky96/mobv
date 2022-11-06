package com.example.zadanie.data

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.zadanie.`interface`.ApiInterface
import com.example.zadanie.`interface`.apiKey
import com.example.zadanie.model.*
import com.example.zadanie.ui.login.RegistrationFragment
import com.example.zadanie.ui.login.RegistrationFragmentDirections
import okhttp3.internal.and
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class ApiService {

    fun fetchCompanies(companyViewModel: CompanyViewModel, context: Context) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://data.mongodb-api.com/app/data-fswjp/endpoint/data/v1/action/")
            .build()
            .create(ApiInterface::class.java)

        val companies = retrofitBuilder.getPubs(PostPub("bars", "mobvapp", "Cluster0"))

        companies.enqueue(object: Callback<Companies> {
            override fun onResponse(call: Call<Companies>, response: Response<Companies>) {
                val body = response.body()
                if (body != null) {
                    insertCompanyToDataBase(companyViewModel, body.documents)
                }
                else {
                    Toast.makeText(context, "No data has been retrieved!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Companies>, t: Throwable) {
                Toast.makeText(context, "Couldn't fetch data!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun insertCompanyToDataBase(companyViewModel: CompanyViewModel, elements: MutableList<Element>) {
        elements.forEach { element -> companyViewModel.addCompany(element) }
    }

    fun fetchNearbyCompanies(lat: Double, lon: Double, context: Context) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://overpass-api.de/api/")
            .build()
            .create(ApiInterface::class.java)

        val query = "[out:json];node(around:500,{lat}, {lon});(node(around:500)[\"amenity\"~\"^pub\$|^bar\$|^restaurant\$|^cafe\$|^fast_food\$|^stripclub\$|^nightclub\$\"];);out body;>;out skel;\n"
            .replace("{lat}", lat.toString())
            .replace("{lon}", lon.toString())

        val companies = retrofitBuilder.getNearbyCompanies(query)
        companies.enqueue(object: Callback<Company> {
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val body = response.body()
                if(body != null) {
                    println(body.elements)
                }
                else {
                    Toast.makeText(context, "No data has been retrieved!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Company>, t: Throwable) {
                Toast.makeText(context, "Couldn't fetch data!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun getCompanyByID(context: Context, id: Int) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://overpass-api.de/api/")
            .build()
            .create(ApiInterface::class.java)

        val query = "[out:json];node({companyId});out body;>;out skel;"
            .replace("{companyId}", id.toString())

        val companies = retrofitBuilder.getCompanyByID(query)
        companies.enqueue(object: Callback<Company> {
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val body = response.body()
                if(body != null) {
                    println(body.elements)
                }
                else {
                    Toast.makeText(context, "No data has been retrieved!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Company>, t: Throwable) {
                Toast.makeText(context, "Couldn't fetch data!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun getCompaniesWithMembers(uid: Int) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://zadanie.mpage.sk/")
            .build()
            .create(ApiInterface::class.java)
        val companies = retrofitBuilder.getCompaniesWithMembers(PostPubsWithMembers(apiKey, uid.toString()))
        companies.enqueue(object: Callback<Company> {
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val body = response.body()
                if(body != null) {
                    TODO()
                }
                else {
                    TODO()
                }
            }

            override fun onFailure(call: Call<Company>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    fun checkInCompany(uid: Int, company: Element) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://zadanie.mpage.sk/")
            .build()
            .create(ApiInterface::class.java)
        val checkInCompany = retrofitBuilder.checkInOutCompany(
            PostLoginLogoutCompany(
                apiKey,
                uid.toString(),
                company.id.toString(),
                company.tags.name,
                company.lat,
                company.lon
            )
        )
        checkInCompany.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    fun checkOutCompany(uid: Int) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://zadanie.mpage.sk/")
            .build()
            .create(ApiInterface::class.java)
        val leaveCompany = retrofitBuilder.checkInOutCompany(
            PostLoginLogoutCompany(apiKey, uid.toString(), "", "", 0.0, 0.0)
        )
        leaveCompany.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    fun loginUser(userName: String, password: String, fragment: Fragment, action: NavDirections) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://zadanie.mpage.sk/")
            .build()
            .create(ApiInterface::class.java)

        val login = retrofitBuilder.login(PostCredentials(apiKey, userName, password))
        login.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.code() == 200) {
                    Toast.makeText(fragment.requireContext(), "Logged in", Toast.LENGTH_SHORT).show()
                    findNavController(fragment).navigate(action)
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Something went wrong! Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error occurred while logging in user!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun refreshToken(uid: Int) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://zadanie.mpage.sk/")
            .build()
            .create(ApiInterface::class.java)

        val refreshToken = retrofitBuilder.refreshToken(PostRefreshToken(apiKey, uid.toString(), ""))
        refreshToken.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    fun registerUser(userName: String, password: String, fragment: RegistrationFragment) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://zadanie.mpage.sk/")
            .build()
            .create(ApiInterface::class.java)

        val register = retrofitBuilder.register(PostCredentials(apiKey, userName, password))
        register.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.code() == 200) {
                    Toast.makeText(fragment.requireContext(), "Successful registration!", Toast.LENGTH_SHORT).show()
                    val action = RegistrationFragmentDirections.actionRegistrationFragmentToCompanyFragment()
                    loginUser(userName, password, fragment, action)
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error occurred while registering user!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hashPassword(password: String): String {
        val salt: ByteArray = getSalt()
        try {
            val md = MessageDigest.getInstance("SHA-512")
            md.update(salt)
            val bytes = md.digest(password.encodeToByteArray())
            val sb: StringBuilder = StringBuilder()
            for (element in bytes) {
                sb.append(((element.and(0xff)) + 0x100).toString(16)).substring(1)
            }
            return sb.toString()
        }
        catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
}