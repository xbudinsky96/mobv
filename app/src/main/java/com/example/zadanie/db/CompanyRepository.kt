package com.example.zadanie.db

import androidx.lifecycle.LiveData
import com.example.zadanie.model.CompanyWithMembers
import com.example.zadanie.model.Element

class CompanyRepository(private val companyDao: CompanyDao) {
    val readData: LiveData<MutableList<CompanyWithMembers>> = companyDao.readCompanies()

    suspend fun addCompany(companyWithMembers: CompanyWithMembers) {
        companyDao.addCompany(companyWithMembers)
    }

    suspend fun deleteCompanies() {
        companyDao.deleteCompanies()
    }

    fun getCompanyById(id: String): CompanyWithMembers {
        return companyDao.getCompanyById(id)
    }
}

class NearbyCompanyRepository(private val companyDao: NearbyCompanyDao) {
    val readData: LiveData<MutableList<Element>> = companyDao.readCompanies()

    suspend fun addCompany(element: Element) {
        companyDao.addCompany(element)
    }

    suspend fun deleteCompanies() {
        companyDao.deleteCompanies()
    }
}