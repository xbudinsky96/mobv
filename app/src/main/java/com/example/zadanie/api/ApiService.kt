package com.example.zadanie.api

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.zadanie.R
import com.example.zadanie.adapter.FriendsAdapter
import com.example.zadanie.fragment.*
import com.example.zadanie.model.*
import com.example.zadanie.utilities.getSalt
import com.example.zadanie.utilities.hashPassword
import com.google.android.material.snackbar.Snackbar
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

    fun fetchNearbyCompanies(lat: Double, lon: Double, fragment: Fragment, companyViewModel: NearbyCompanyViewModel) {
        val query = "[out:json];node(around:500,{lat}, {lon});(node(around:500)[\"amenity\"~\"^pub\$|^bar\$|^restaurant\$|^cafe\$|^fast_food\$|^stripclub\$|^nightclub\$\"];);out body;>;out skel;\n"
            .replace("{lat}", lat.toString())
            .replace("{lon}", lon.toString())

        val companies = overPassAPI.getNearbyCompanies(query)
        companies.enqueue(object: Callback<Company> {
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val pubs = response.body()
                if(pubs != null) {
                    companyViewModel.deleteCompanies()
                    insertCompaniesToDataBase(companyViewModel, pubs.elements)
                }
                else {
                    Snackbar.make(fragment.requireView(), "No data has been retrieved!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Company>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Couldn't fetch data!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getCompanyByID(fragmentCheckInDetail: CheckInDetailFragment, id: Long) {
        val query = "[out:json];node({companyId});out body;>;out skel;"
            .replace("{companyId}", id.toString())

        val companies = overPassAPI.getCompanyByID(query)
        companies.enqueue(object: Callback<Company> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val pubs = response.body()
                if (pubs != null) {
                    val foundCompany = pubs.elements[0]
                    val latitude = foundCompany.lat
                    val longitude = foundCompany.lon

                    try {
                        val binding = fragmentCheckInDetail.binding
                        fragmentCheckInDetail.setDetails(foundCompany, binding)
                        binding.confirm.setOnClickListener {
                            checkInCompany(foundCompany, fragmentCheckInDetail)
                        }
                        binding.confirm.isEnabled = true
                        fragmentCheckInDetail.setCoordinates(latitude, longitude)
                    } catch (_: Exception) {}
                }
                else {
                    Snackbar.make(
                        fragmentCheckInDetail.requireView(),
                        "No data has been retrieved!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Company>, t: Throwable) {
                Toast.makeText(
                    fragmentCheckInDetail.requireContext(),
                    "Couldn't fetch data!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun getCompanyByID(homeFragment: HomeFragment, id: Long) {
        val query = "[out:json];node({companyId});out body;>;out skel;"
            .replace("{companyId}", id.toString())

        val companies = overPassAPI.getCompanyByID(query)
        companies.enqueue(object: Callback<Company> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Company>, response: Response<Company>) {
                val pubs = response.body()
                if (pubs != null) {
                    val foundCompany = pubs.elements[0]
                    try {
                        val binding = homeFragment.binding
                        binding.companyName.text = foundCompany.tags.name
                        binding.nameTitle.text = loggedInUser.name
                        binding.showOnMap.isEnabled = true
                        binding.showOnMap.setOnClickListener {
                            val queryUrl: Uri =
                                Uri.parse("https://www.google.com/maps/@${foundCompany.lat},${foundCompany.lon},16z")
                            val showOnMap = Intent(Intent.ACTION_VIEW, queryUrl)
                            homeFragment.requireContext().startActivity(showOnMap)
                        }
                    } catch (_: Exception) {}
                }
                else {
                    Snackbar.make(
                        homeFragment.requireView(),
                        "No data has been retrieved!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Company>, t: Throwable) {
                Toast.makeText(
                    homeFragment.requireContext(),
                    "Couldn't fetch data!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun getCompaniesWithMembers(fragment: Fragment, pullToRefresh: SwipeRefreshLayout?) {
        val auth = "Bearer " + loggedInUser.access
        val companyViewModel = ViewModelProvider(fragment)[CompanyViewModel::class.java]
        val companies = mPageAPI.getCompaniesWithMembers(loggedInUser.uid, auth)
        companies.enqueue(object: Callback<MutableList<CompanyWithMembers>> {
            override fun onResponse(
                call: Call<MutableList<CompanyWithMembers>>,
                response: Response<MutableList<CompanyWithMembers>>
            ) {
                if (pullToRefresh != null) {
                    pullToRefresh.isRefreshing = false
                }
                if (response.isSuccessful) {
                    val pubs = response.body()
                    if (pubs != null) {
                        companyViewModel.deleteCompanies()
                        insertCompaniesToDataBase(companyViewModel, pubs)
                    } else {
                        Snackbar.make(fragment.requireView(), "No companies found!", Snackbar.LENGTH_SHORT).show()
                    }
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
                else {
                    Snackbar.make(fragment.requireView(), "Response not successful!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MutableList<CompanyWithMembers>>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Something went wrong!", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    fun checkInCompany(company: Element, fragmentCheckInDetail: CheckInDetailFragment) {
        val auth = "Bearer " + loggedInUser.access
        val usersViewModel = ViewModelProvider(fragmentCheckInDetail)[UsersViewModel::class.java]
        val checkInCompany = mPageAPI.checkInCompany(
            PostLoginLogoutCompany(
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
                    Snackbar.make(fragmentCheckInDetail.requireView(), "Checked in!", Snackbar.LENGTH_SHORT).show()
                    loggedInUser.companyId = company.id.toString()
                    usersViewModel.updateUser(true, loggedInUser)

                    if (loggedInUser.lat != null && loggedInUser.lon != null) {
                        fragmentCheckInDetail.createFence(loggedInUser.lat!!, loggedInUser.lon!!)
                    }
                }
                else if(response.code() == 401) {
                    refreshToken(fragmentCheckInDetail)
                }
                else {
                    Snackbar.make(fragmentCheckInDetail.requireView(), "Failure!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {
                Snackbar.make(fragmentCheckInDetail.requireView(), "Failed to check in!", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    fun checkOutCompany(fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val userViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val leaveCompany = mPageAPI.checkOutCompany(
            PostLoginLogoutCompany("", "", "", 0.0, 0.0),
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
                    fragment.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCheckInDetailFragment(0))
                    Snackbar.make(fragment.requireView(), "Checked out!", Snackbar.LENGTH_SHORT).show()
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
                else {
                    Snackbar.make(fragment.requireView(), "Failed to check out!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Failure!", Snackbar.LENGTH_SHORT).show()
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
                        Snackbar.make(fragment.requireView(), "Logged in", Snackbar.LENGTH_SHORT).show()
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
                        Snackbar.make(fragment.requireView(), "Wrong username or password!", Snackbar.LENGTH_SHORT).show()
                    }
                }
                else {
                    Snackbar.make(fragment.requireView(), "Something went wrong! Try again.", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Error occurred while logging in user!", Snackbar.LENGTH_SHORT).show()
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
        Snackbar.make(fragment.requireView(), "Logged out!", Snackbar.LENGTH_SHORT).show()
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
                            Snackbar.make(fragment.requireView(), "Successful registration!", Snackbar.LENGTH_SHORT).show()
                        }
                        else {
                            Snackbar.make(fragment.requireView(), "Username is already taken!", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    Snackbar.make(fragment.requireView(), "Something went wrong!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Error occurred while registering user!", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    fun refreshToken(fragment: Fragment) {
        val usersViewModel = ViewModelProvider(fragment)[UsersViewModel::class.java]
        val refreshToken = mPageAPI.refreshToken(PostRefreshToken(loggedInUser.refresh), loggedInUser.uid)
        refreshToken.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newCredentials = response.body()
                    if (newCredentials != null) {
                        loggedInUser = newCredentials
                        usersViewModel.updateUser(true, loggedInUser)
                        Snackbar.make(fragment.requireView(), "Token refreshed. Try again!", Snackbar.LENGTH_SHORT).show()
                    }
                }
                else if (response.code() == 401) {
                    Snackbar.make(fragment.requireView(), "Couldn't refresh token! Please log in again!", Snackbar.LENGTH_SHORT).show()
                    apiService.logoutUser(fragment)
                    fragment.findNavController().navigate(R.id.action_toLoginFragment)
                    return
                }
                else {
                    Snackbar.make(fragment.requireView(), "Couldn't refresh token! Please check your connection or log in again!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Error while refreshing token!", Snackbar.LENGTH_SHORT).show()
            }

        })
    }

    fun addFriend(name: String, fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val addFriend = mPageAPI.addFriend(PostAddDeleteUser(name), loggedInUser.uid, auth)
        addFriend.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Snackbar.make(fragment.requireView(), "Friend added successfully!", Snackbar.LENGTH_SHORT).show()
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
                else {
                    Snackbar.make(fragment.requireView(), "Could not add friend!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Error! Friend has not been added!", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    fun deleteFriend(name: String, fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val deleteFriend = mPageAPI.deleteFriend(PostAddDeleteUser(name), loggedInUser.uid, auth)
        deleteFriend.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Snackbar.make(fragment.requireView(), "Friend deleted successfully!", Snackbar.LENGTH_SHORT).show()
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
                else {
                    Snackbar.make(fragment.requireView(), "Could not remove friend!", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Error! Friend has not been deleted!", Snackbar.LENGTH_SHORT).show()
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
                    val friends = response.body()
                    if (friends != null) {
                        if (friends.isEmpty()) {
                            Snackbar.make(fragment.requireView(), "You don't have friends yet!", Snackbar.LENGTH_SHORT).show()
                        }
                        adapter.setUsers(friends)
                        fragment.binding.list.adapter = adapter
                    }
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                }
            }

            override fun onFailure(call: Call<MutableList<Friend>>, t: Throwable) {
                Snackbar.make(fragment.requireView(), "Error! Couldn't load friends!", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

}