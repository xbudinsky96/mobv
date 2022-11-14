package com.example.zadanie.model

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {
    val readData: LiveData<MutableList<User>> = userDao.readUsers()

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun updateUser(log: Boolean, id: String) {
        userDao.updateUser(log, id)
    }

    fun getUserByName(name: String): User {
        return userDao.getUserByName(name)
    }
}