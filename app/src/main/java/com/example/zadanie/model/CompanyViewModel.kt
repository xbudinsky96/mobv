package com.example.zadanie.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.zadanie.db.CompaniesWithMembersDB
import com.example.zadanie.db.CompanyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CompanyViewModel(application: Application): AndroidViewModel(application) {
    val readData: LiveData<MutableList<CompanyWithMembers>>
    private val repository: CompanyRepository

    init {
        val companyDao = CompaniesWithMembersDB.getDatabase(application).companyDao()
        repository = CompanyRepository(companyDao)
        readData = repository.readData
    }

    fun addCompany(company: CompanyWithMembers) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCompany(company)
        }
    }

    fun getCompanyById(id: String): CompanyWithMembers {
        return runBlocking {
            withContext(Dispatchers.IO) {
                repository.getCompanyById(id)
            }
        }
    }

    fun deleteCompany(company: CompanyWithMembers) {
        viewModelScope.launch {
            repository.deleteCompany(company)
        }
    }
}