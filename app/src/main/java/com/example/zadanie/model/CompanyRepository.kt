package com.example.zadanie.model

import androidx.lifecycle.LiveData

class CompanyRepository(private val companyDao: CompanyDao) {
    val readData: LiveData<MutableList<CompanyWithMembers>> = companyDao.readCompanies()

    suspend fun addCompany(companyWithMembers: CompanyWithMembers) {
        companyDao.addCompany(companyWithMembers)
    }

    suspend fun deleteCompany(companyWithMembers: CompanyWithMembers) {
        companyDao.deleteCompany(companyWithMembers)
    }
}

class NearbyCompanyRepository(private val companyDao: NearbyCompanyDao) {
    val readData: LiveData<MutableList<Element>> = companyDao.readCompanies()

    suspend fun addCompany(element: Element) {
        companyDao.addCompany(element)
    }
}