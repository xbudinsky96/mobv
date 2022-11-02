package com.example.zadanie.model

import androidx.lifecycle.LiveData

class CompanyRepository(private val companyDao: CompanyDao) {
    val readData: LiveData<MutableList<Element>> = companyDao.readCompanies()

    suspend fun addCompany(element: Element) {
        companyDao.addCompany(element)
    }

    suspend fun deleteCompany(element: Element) {
        companyDao.deleteCompany(element)
    }
}