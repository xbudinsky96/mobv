package com.example.zadanie.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Query("SELECT * FROM user_table ORDER BY uid ASC")
    fun readUsers(): LiveData<MutableList<User>>

    @Query("SELECT * FROM user_table WHERE name = :name")
    fun getUserByName(name: String): User

    @Delete
    suspend fun deleteUser(user: User)
}