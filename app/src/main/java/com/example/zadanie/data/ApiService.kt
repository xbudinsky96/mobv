package com.example.zadanie.data

import UserHandlerModel
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.zadanie.`interface`.ApiInterface
import com.example.zadanie.adapter.FriendsAdapter
import com.example.zadanie.databinding.FragmentCheckInDetailBinding
import com.example.zadanie.fragment.FriendListFragment
import com.example.zadanie.model.*
import com.example.zadanie.ui.login.LoginFragment
import com.example.zadanie.ui.login.RegistrationFragment
import com.example.zadanie.ui.login.RegistrationFragmentDirections
import okhttp3.internal.and
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class ApiService {

    private val mPageAPI = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://zadanie.mpage.sk/")
        .build()
        .create(ApiInterface::class.java)

    private val overPassAPI = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://overpass-api.de/api/")
        .build()
        .create(ApiInterface::class.java)

    private fun insertCompanyToDataBase(companyViewModel: CompanyViewModel, companies: MutableList<CompanyWithMembers>) {
        companies.forEach { company -> companyViewModel.addCompany(company) }
    }

    private fun insertCompanyToDataBase(companyViewModel: NearbyCompanyViewModel, elements: MutableList<Element>) {
        elements.forEach { element -> companyViewModel.addCompany(element) }
    }

    fun fetchNearbyCompanies(lat: Double, lon: Double, context: Context, companyViewModel: NearbyCompanyViewModel) {
        val query = "[out:json];node(around:500,{lat}, {lon});(node(around:500)[\"amenity\"~\"^pub\$|^bar\$|^restaurant\$|^cafe\$|^fast_food\$|^stripclub\$|^nightclub\$\"];);out body;>;out skel;\n"
            .replace("{lat}", lat.toString())
            .replace("{lon}", lon.toString())

        val companies = overPassAPI.getNearbyCompanies(query)
        companies.enqueue(object: Callback<Company> {
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val body = response.body()
                if(body != null) {
                    insertCompanyToDataBase(companyViewModel, body.elements)
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

    fun getCompanyByID(context: Context, id: Long, binding: FragmentCheckInDetailBinding) {
        val query = "[out:json];node({companyId});out body;>;out skel;"
            .replace("{companyId}", id.toString())

        val companies = overPassAPI.getCompanyByID(query)
        Toast.makeText(context, "Fetching data!", Toast.LENGTH_SHORT).show()
        companies.enqueue(object: Callback<Company> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val body = response.body()
                if(body != null) {
                    val foundCompany = body.elements[0]
                    setDetails(foundCompany, binding)
                    binding.confirm.setOnClickListener {
                        checkInCompany(foundCompany, context)
                    }
                    binding.confirm.isEnabled = true
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

    fun setDetails(foundCompany: Element, binding: FragmentCheckInDetailBinding) {
        val openingHours = if(foundCompany.tags.opening_hours != null) "\n\n   Opening hours: \n" + foundCompany.tags.opening_hours + "\n" else ""
        val tel = if(foundCompany.tags.phone != null) "    " +  foundCompany.tags.phone + "\n" else ""
        val web = if(foundCompany.tags.website != null) "    " + foundCompany.tags.website + "\n" else ""
        val type = if(foundCompany.tags.amenity != null) "    " + foundCompany.tags.amenity + "\n\n" else ""
        val contact = if(tel != null || web != null) "   Contact us: \n" else ""

        binding.content.text =
            "   ${foundCompany.tags.name}\n" + type + contact + tel + web + openingHours
    }

    fun getCompaniesWithMembers(context: Context, companyViewModel: CompanyViewModel) {
        val auth = "Bearer " + loggedInUser.access
        val companies = mPageAPI.getCompaniesWithMembers(loggedInUser.uid, auth)
        companies.enqueue(object: Callback<MutableList<CompanyWithMembers>> {
            override fun onResponse(
                call: Call<MutableList<CompanyWithMembers>>,
                response: Response<MutableList<CompanyWithMembers>>
            ) {
                val body = response.body()
                if (body != null) {
                    insertCompanyToDataBase(companyViewModel, body)
                }
                else {
                    Toast.makeText(context, "No companies found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MutableList<CompanyWithMembers>>, t: Throwable) {
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun checkInCompany(company: Element, context: Context) {
        val auth = "Bearer " + loggedInUser.access
        val checkInCompany = mPageAPI.checkInCompany(
            PostLoginCompany(
                company.id.toString(),
                company.tags.name,
                company.tags.amenity,
                company.lat,
                company.lon
            ),
            loggedInUser.uid,
            auth
        )
        checkInCompany.enqueue(object: Callback<CheckInResponse> {
            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {
                println(response.message())
                if (response.isSuccessful) {
                    Toast.makeText(context, "Checked in to " + company.tags.name + "!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "Failure!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to check in!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun checkOutCompany() {
        val auth = "Bearer " + loggedInUser.access
        val leaveCompany = mPageAPI.checkOutCompany(
            PostLogoutCompany("", "", 0.0, 0.0),
            loggedInUser.uid,
            auth
        )
        leaveCompany.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    println("CHECKED OUT")
                }
                else {
                    println("FAILED TO CHECK OUT")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                println("FAILED TO CHECK OUT")
            }

        })
    }

    fun loginUser(userName: String, password: String, fragment: LoginFragment, action: NavDirections) {
        val login = mPageAPI.login(PostCredentials(userName, password))
        login.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    val user = response.body()
                    if (user != null && user.uid != "-1") {
                        Toast.makeText(fragment.requireContext(), "Logged in", Toast.LENGTH_SHORT).show()
                        findNavController(fragment).navigate(action)
                        loggedInUser = user
                        loggedInUser.lat = fragment.location.latitude
                        loggedInUser.lon = fragment.location.longitude
                    }
                    else {
                        Toast.makeText(fragment.requireContext(), "Wrong username or password!", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Something went wrong! Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error occurred while logging in user!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun registerUser(userName: String, password: String, fragment: RegistrationFragment, userHandlerModel: UserHandlerModel, location: Location) {
        val register = mPageAPI.register(PostCredentials(userName, password))
        register.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newUser = response.body()
                    if(newUser != null) {
                        if (newUser.uid != "-1") {
                            userHandlerModel.addUser(newUser)
                            loggedInUser = newUser
                            loggedInUser.lat = location.latitude
                            loggedInUser.lon = location.longitude
                            val action = RegistrationFragmentDirections.actionRegistrationFragmentToCompanyFragment()
                            fragment.findNavController().navigate(action)
                            Toast.makeText(fragment.requireContext(), "Successful registration!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(fragment.requireContext(), "Username is already taken!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error occurred while registering user!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun refreshToken() {
        val refreshToken = mPageAPI.refreshToken(PostRefreshToken(loggedInUser.refresh), loggedInUser.uid)
        refreshToken.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newCredentials = response.body()
                    if (newCredentials != null) {
                        loggedInUser = newCredentials
                    }
                }
                else {

                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addFriend(name: String, fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val addFriend = mPageAPI.addFriend(PostAddDeleteUser(name), loggedInUser.uid, auth)
        addFriend.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(fragment.requireContext(), "Friend added successfully!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Could not add friend!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error! Friend has not been added!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun deleteFriend(name: String, fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val deleteFriend = mPageAPI.deleteFriend(PostAddDeleteUser(name), loggedInUser.uid, auth)
        deleteFriend.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(fragment.requireContext(), "Friend deleted successfully!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Could not remove friend!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error! Friend has not been deleted!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showFriends(fragment: FriendListFragment, adapter: FriendsAdapter) {
        val auth = "Bearer " + loggedInUser.access
        val showFriends = mPageAPI.showFriends(loggedInUser.uid, auth)
        showFriends.enqueue(object: Callback<MutableList<Friend>> {
            override fun onResponse(
                call: Call<MutableList<Friend>>,
                response: Response<MutableList<Friend>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.isEmpty()) {
                            Toast.makeText(fragment.requireContext(), "You don't have friends yet!", Toast.LENGTH_SHORT).show()
                        }
                        adapter.setUsers(body)
                        fragment.binding.list.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<Friend>>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error! Couldn't load friends!", Toast.LENGTH_SHORT).show()
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