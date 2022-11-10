package com.example.zadanie.model

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {
    val readUsers: LiveData<MutableList<User>> = userDao.readUsers()

    fun getUserByName(name: String): User {
        return userDao.getUserByName(name)
    }

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}