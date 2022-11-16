package com.example.zadanie.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.zadanie.`interface`.ApiInterface
import com.example.zadanie.adapter.FriendsAdapter
import com.example.zadanie.databinding.FragmentCheckInDetailBinding
import com.example.zadanie.fragment.CheckInDetailFragment
import com.example.zadanie.fragment.CheckInDetailFragmentDirections
import com.example.zadanie.fragment.FriendListFragment
import com.example.zadanie.fragment.HomeFragment
import com.example.zadanie.model.*
import com.example.zadanie.ui.login.LoginFragment
import com.example.zadanie.ui.login.LoginFragmentDirections
import com.example.zadanie.ui.login.RegistrationFragment
import com.example.zadanie.ui.login.RegistrationFragmentDirections
import okhttp3.internal.and
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

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
                        setDetails(foundCompany, binding)
                        binding.confirm.setOnClickListener {
                            if (context != null) {
                                checkInCompany(foundCompany, null, fragmentCheckInDetail)
                            }
                        }
                        binding.confirm.isEnabled = true
                        setCoordinates(fragmentCheckInDetail, binding, latitude, longitude)
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

    private fun setDetails(foundCompany: Element, binding: FragmentCheckInDetailBinding) {
        val openingHours = if(foundCompany.tags.opening_hours != null && foundCompany.tags.opening_hours != "") "Opening hours:" + "\n\n" + foundCompany.tags.opening_hours.replace(", ", "\n") else ""
        val tel = if(foundCompany.tags.phone != null && foundCompany.tags.phone != "") "TEL: " + foundCompany.tags.phone else ""
        val web = if(foundCompany.tags.website != null && foundCompany.tags.website != "") "WEB: " + foundCompany.tags.website else ""
        val contact = if(tel != null && tel != "" || web != null && web != "") "Contact us: \n" else ""

        binding.compName.text = foundCompany.tags.name
        binding.compType.text = foundCompany.tags.amenity.replace("_", " ")
        binding.openingHours.text = openingHours
        binding.tel.text = tel
        binding.web.text = web
        binding.tel.text = tel
        binding.contactUs.text = contact
    }

    private fun setCoordinates(fragment: Fragment, binding: FragmentCheckInDetailBinding, latitude: Double, longitude: Double) {
        val showOnMap = binding.showonmap
        val searchPrefix = "https://www.google.com/maps/@"
        showOnMap.isEnabled = true
        showOnMap.setOnClickListener {
            val queryUrl: Uri = Uri.parse("${searchPrefix}${latitude},${longitude},16z")
            val show = Intent(Intent.ACTION_VIEW, queryUrl)
            fragment.startActivity(show)
        }
    }

    fun getCompaniesWithMembers(fragment: Fragment) {
        val companyViewModel = ViewModelProvider(fragment)[CompanyViewModel::class.java]
        val context = fragment.requireContext()
        val auth = "Bearer " + loggedInUser.access
        val companies = mPageAPI.getCompaniesWithMembers(loggedInUser.uid, auth)
        companies.enqueue(object: Callback<MutableList<CompanyWithMembers>> {
            override fun onResponse(
                call: Call<MutableList<CompanyWithMembers>>,
                response: Response<MutableList<CompanyWithMembers>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        insertCompanyToDataBase(companyViewModel, body)
                    } else {
                        Toast.makeText(context, "No companies found!", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                    getCompaniesWithMembers(fragment)
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
            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Checked in to " + company.tags.name + "!", Toast.LENGTH_SHORT).show()
                    loggedInUser.companyId = company.id.toString()
                    usersViewModel.updateUser(true, loggedInUser)
                    val action = CheckInDetailFragmentDirections.actionCheckInDetailFragmentToHomeFragment(company.id)
                    fragment.findNavController().navigate(action)
                    getCompaniesWithMembers(fragment)
                }
                else if(response.code() == 401) {
                    if (fragment != null) {
                        refreshToken(fragment)
                    }
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

    //TODO
    fun checkOutCompany(fragment: Fragment) {
        val auth = "Bearer " + loggedInUser.access
        val leaveCompany = mPageAPI.checkOutCompany(
            PostLogoutCompany("", "Big Table", 37.4227271, -122.08751),
            loggedInUser.uid,
            auth
        )
        leaveCompany.enqueue(object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    loggedInUser.companyId = "-1"
                    Toast.makeText(fragment.requireContext(), "Checked out!", Toast.LENGTH_SHORT).show()
                }
                else if(response.code() == 401) {
                    refreshToken(fragment)
                    checkOutCompany(fragment)
                }
                else {
                    Toast.makeText(fragment.requireContext(), "Failed to check out!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
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
                        loggedInUser.lat = fragment.location?.latitude
                        loggedInUser.lon = fragment.location?.longitude
                        if (userFromDB == null) {
                            loggedInUser.name = userName
                            usersViewModel.addUser(loggedInUser)
                        } else {
                            loggedInUser.companyId = userFromDB.companyId
                            usersViewModel.updateUser(true, loggedInUser)
                            try {
                                val action = LoginFragmentDirections.actionLoginFragmentToCheckInDetailFragment(
                                    loggedInUser.companyId?.toLong()!!)
                                findNavController(fragment).navigate(action)
                            }
                            catch (e: Exception) {
                                val action = LoginFragmentDirections.actionLoginFragmentToCheckInDetailFragment(0)
                                findNavController(fragment).navigate(action)
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
                            loggedInUser.lat = fragment.location?.latitude
                            loggedInUser.lon = fragment.location?.longitude
                            loggedInUser.isLogged = true
                            loggedInUser.salt = salt
                            usersViewModel.addUser(loggedInUser)
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
                    }
                }
                else {
                    Toast.makeText(context, "Couldn't refresh token!", Toast.LENGTH_SHORT).show()
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
                    addFriend(name, fragment)
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
                    deleteFriend(name, fragment)
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
                    showFriends(fragment, adapter)
                }
            }

            override fun onFailure(call: Call<MutableList<Friend>>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), "Error! Couldn't load friends!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
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