package com.example.zadanie.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NearbyCompanyViewModel(application: Application): AndroidViewModel(application) {
    val readData: LiveData<MutableList<Element>>
    private val repository: NearbyCompanyRepository

    init {
        val companyDao = NearbyCompanyDatabase.getDatabase(application).nearbyCompanyDao()
        repository = NearbyCompanyRepository(companyDao)
        readData = repository.readData
    }

    fun addCompany(element: Element) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCompany(element)
        }
    }
}