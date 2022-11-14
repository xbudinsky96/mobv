package com.example.zadanie.model

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {
    val readData: LiveData<MutableList<User>> = userDao.readUsers()

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun updateUser(log: Boolean, user: User) {
        userDao.updateUser(log, user.uid, user.lat, user.lon, user.refresh, user.access)
    }

    fun getUserByName(name: String): User {
        return userDao.getUserByName(name)
    }

    fun getLoggedUser(): User {
        return userDao.getLoggedUser()
    }
}