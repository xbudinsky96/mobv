package com.example.zadanie.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.zadanie.R
import com.example.zadanie.adapter.FriendsAdapter
import com.example.zadanie.fragment.*
import com.example.zadanie.model.*
import com.example.zadanie.ui.login.LoginFragment
import com.example.zadanie.ui.login.LoginFragmentDirections
import com.example.zadanie.ui.login.RegistrationFragment
import com.example.zadanie.ui.login.RegistrationFragmentDirections
import com.example.zadanie.utilities.getSalt
import com.example.zadanie.utilities.hashPassword
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

val apiService = ApiService()

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

    private fun insertCompaniesToDataBase(companyViewModel: CompanyViewModel, companies: MutableList<CompanyWithMembers>) {
        companies.forEach { company -> companyViewModel.addCompany(company) }
    }

    private fun insertCompaniesToDataBase(companyViewModel: NearbyCompanyViewModel, elements: MutableList<Element>) {
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
                    companyViewModel.deleteCompanies()
                    insertCompaniesToDataBase(companyViewModel, body.elements)
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

    fun getCompanyByID(fragmentCheckInDetail: CheckInDetailFragment?, fragmentHome: HomeFragment?, id: Long) {
        val query = "[out:json];node({companyId});out body;>;out skel;"
            .replace("{companyId}", id.toString())

        val context = fragmentCheckInDetail?.requireContext() ?: fragmentHome?.requireContext()
        val companies = overPassAPI.getCompanyByID(query)
        companies.enqueue(object: Callback<Company> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val body = response.body()
                if(body != null) {
                    val foundCompany = body.elements[0]
                    val latitude = foundCompany.lat
                    val longitude = foundCompany.lon

                    if (fragmentCheckInDetail != null) {
                        val binding = fragmentCheckInDetail.binding
                        fragmentCheckInDetail.setDetails(foundCompany, binding)
                        binding.confirm.setOnClickListener {
                            if (context != null) {
                                checkInCompany(foundCompany, null, fragmentCheckInDetail)
                            }
                        }
                        binding.confirm.isEnabled = true
                        fragmentCheckInDetail.setCoordinates(latitude, longitude)
                    }

                    if (fragmentHome != null) {
                        val binding = fragmentHome.binding
                        binding.companyName.text = foundCompany.tags.name
                        binding.nameTitle.text = loggedInUser.name
                        binding.showOnMap.isEnabled = true
                        binding.showOnMap.setOnClickListener {
                            val queryUrl: Uri = Uri.parse("https://www.google.com/maps/@${latitude},${longitude},16z")
                            val showOnMap = Intent(Intent.ACTION_VIEW, queryUrl)
                            context?.startActivity(showOnMap)
                        }
                    }
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

    fun getCompaniesWithMembers(fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val companyViewModel = ViewModelProvider(fragment)[CompanyViewModel::class.java]
        val context = fragment.requireContext()
        val companies = mPageAPI.getCompaniesWithMembers(loggedInUser.uid, auth)
        companies.enqueue(object: Callback<MutableList<CompanyWithMembers>> {
            override fun onResponse(
                call: Call<MutableList<CompanyWithMembers>>,
                response: Response<MutableList<CompanyWithMembers>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        companyViewModel.deleteCompanies()
                        insertCompaniesToDataBase(companyViewModel, body)
                    } else {
                        Toast.makeText(context, "No companies found!", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
                else {
                    Toast.makeText(context, "Response not successful!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MutableList<CompanyWithMembers>>, t: Throwable) {
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun checkInCompany(company: Element, fragmentHome: HomeFragment?, fragmentCheckInDetail: CheckInDetailFragment?) {
        val auth = "Bearer " + loggedInUser.access
        val fragment = fragmentCheckInDetail ?: fragmentHome
        val usersViewModel = ViewModelProvider(fragment!!)[UsersViewModel::class.java]
        val context = fragment.requireContext()
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
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Checked in to " + company.tags.name + "!", Toast.LENGTH_SHORT).show()
                    loggedInUser.companyId = company.id.toString()
                    usersViewModel.updateUser(true, loggedInUser)
                    fragment.findNavController().navigate(
                        CheckInDetailFragmentDirections.actionCheckInDetailFragmentToHomeFragment(company.id)
                    )

                    if (loggedInUser.lat != null && loggedInUser.lon != null) {
                        fragmentCheckInDetail?.createFence(loggedInUser.lat!!, loggedInUser.lon!!)
                    }
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
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

    fun checkOutCompany(fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val userViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val leaveCompany = mPageAPI.checkOutCompany(
            PostLogoutCompany("", "", "", 0.0, 0.0),
            loggedInUser.uid,
            auth
        )
        leaveCompany.enqueue(object: Callback<CheckInResponse> {
            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {
                if (response.isSuccessful) {
                    loggedInUser.companyId = null
                    userViewModel.updateUser(true, loggedInUser)
                    Toast.makeText(fragment.requireContext(), "Checked out!", Toast.LENGTH_SHORT).show()
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Failed to check out!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Failure!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun loginUser(userName: String, password: String, fragment: LoginFragment) {
        val usersViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val userFromDB = usersViewModel.getUserByName(userName)
        val pass = getPassword(userFromDB, password)
        val login = mPageAPI.login(PostCredentials(userName, pass))
        login.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful) {
                    val user = response.body()
                    if (user != null && user.uid != "-1") {
                        Toast.makeText(fragment.requireContext(), "Logged in", Toast.LENGTH_SHORT).show()
                        loggedInUser = user
                        loggedInUser.isLogged = true
                        if (userFromDB == null) {
                            loggedInUser.name = userName
                            usersViewModel.addUser(loggedInUser)
                            findNavController(fragment).navigate(LoginFragmentDirections.actionLoginFragmentToCheckInDetailFragment(0))
                        } else {
                            loggedInUser.companyId = userFromDB.companyId
                            usersViewModel.updateUser(true, loggedInUser)
                            try {
                                findNavController(fragment).navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment(
                                    loggedInUser.companyId?.toLong()!!))
                            }
                            catch (e: Exception) {
                                findNavController(fragment).navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment(0))
                            }
                        }
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

    private fun getPassword(userFromDB: User, password: String): String {
        if (userFromDB != null) {
            if (userFromDB.salt != null) {
                return hashPassword(password, userFromDB.salt!!)
            }
            return password
        }
        return password
    }

    fun logoutUser(fragment: Fragment) {
        val usersViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        usersViewModel.updateUser(false, loggedInUser)
    }

    fun getLoggedUser(fragment: Fragment) {
        val usersViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val user = usersViewModel.getLoggedUser()
        if (user != null) {
            loggedInUser = user
            fragment.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToCompanyFragment())
        }
    }

    fun registerUser(userName: String, password: String, fragment: RegistrationFragment) {
        val usersViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val salt = getSalt()
        val hashedPassword = hashPassword(password, salt)
        val register = mPageAPI.register(PostCredentials(userName, hashedPassword))
        register.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newUser = response.body()
                    if(newUser != null) {
                        if (newUser.uid != "-1") {
                            loggedInUser = newUser
                            loggedInUser.name = userName
                            loggedInUser.isLogged = true
                            loggedInUser.salt = salt
                            usersViewModel.addUser(loggedInUser)
                            fragment.findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToCompanyFragment())
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

    fun refreshToken(fragment: Fragment) {
        val context = fragment.requireContext()
        val usersViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val refreshToken = mPageAPI.refreshToken(PostRefreshToken(loggedInUser.refresh), loggedInUser.uid)
        refreshToken.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newCredentials = response.body()
                    if (newCredentials != null) {
                        loggedInUser = newCredentials
                        usersViewModel.updateUser(true, loggedInUser)
                        Toast.makeText(context, "Token refreshed. Try again!", Toast.LENGTH_SHORT).show()
                    }
                }
                else if (response.code() == 401) {
                    Toast.makeText(context, "Couldn't refresh token! Please log in again!", Toast.LENGTH_SHORT).show()
                    apiService.logoutUser(fragment)
                    fragment.findNavController().navigate(R.id.action_toLoginFragment)
                    return
                }
                else {
                    Toast.makeText(context, "Couldn't refresh token! Please check your connection or log in again!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(context, "Error while refreshing token!", Toast.LENGTH_SHORT).show()
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
                else if(response.code() == 401) {
                    refreshToken(fragment)
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
                else if(response.code() == 401) {
                    refreshToken(fragment)
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
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
            }

            override fun onFailure(call: Call<MutableList<Friend>>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error! Couldn't load friends!", Toast.LENGTH_SHORT).show()
            }
        })
    }

}