package com.example.zadanie.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UsersViewModel(application: Application): AndroidViewModel(application) {
    val readUsers: LiveData<MutableList<User>>
    private val repository: UserRepository

    init {
        val userDao = UserDB.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readUsers = repository.readData
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun updateUser(log: Boolean, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(log, id)
        }
    }

    fun getUserByName(name: String): User {
        return runBlocking {
            withContext(Dispatchers.IO) {
                repository.getUserByName(name)
            }
        }
    }
}