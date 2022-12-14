package com.example.zadanie.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.zadanie.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Query("UPDATE usersTable SET isLogged = :log, lat = :lat, lon = :lon, refresh = :refresh, access = :access, companyId = :companyId WHERE uid = :id")
    suspend fun updateUser(log: Boolean, id: String, lat: Double?, lon: Double?, refresh: String, access: String, companyId: String?)

    @Query("SELECT * FROM usersTable ORDER BY uid ASC")
    fun readUsers(): LiveData<MutableList<User>>

    @Query("SELECT * FROM usersTable WHERE name = :name")
    fun getUserByName(name: String): User

    @Query("SELECT * FROM usersTable WHERE isLogged = 1")
    fun getLoggedUser(): User
}