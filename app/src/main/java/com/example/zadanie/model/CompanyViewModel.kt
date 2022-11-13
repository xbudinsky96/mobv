package com.example.zadanie.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompanyViewModel(application: Application): AndroidViewModel(application) {
    val readData: LiveData<MutableList<CompanyWithMembers>>
    private val repository: CompanyRepository

    init {
        val companyDao = CompanyDatabase.getDatabase(application).companyDao()
        repository = CompanyRepository(companyDao)
        readData = repository.readData
    }

    fun addCompany(company: CompanyWithMembers) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCompany(company)
        }
    }

    fun deleteCompany(company: CompanyWithMembers) {
        viewModelScope.launch {
            repository.deleteCompany(company)
        }
    }

    fun deleteTable() {
        viewModelScope.launch {
            repository.deleteTable()
        }
    }
}