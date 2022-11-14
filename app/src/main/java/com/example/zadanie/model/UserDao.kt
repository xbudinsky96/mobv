package com.example.zadanie.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Query("UPDATE usersTable SET isLogged = :log, lat = :lat, lon = :lon, refresh = :refresh, access = :access WHERE uid = :id")
    suspend fun updateUser(log: Boolean, id: String, lat: Double?, lon: Double?, refresh: String, access: String)

    @Query("SELECT * FROM usersTable ORDER BY uid ASC")
    fun readUsers(): LiveData<MutableList<User>>

    @Query("SELECT * FROM usersTable WHERE name = :name")
    fun getUserByName(name: String): User

    @Query("SELECT * FROM usersTable WHERE isLogged = 1")
    fun getLoggedUser(): User
}