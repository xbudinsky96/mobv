package com.example.zadanie.model

import androidx.lifecycle.LiveData

class CompanyRepository(private val companyDao: CompanyDao) {
    val readData: LiveData<MutableList<Element>> = companyDao.readData()

    suspend fun addCompany(element: Element) {
        companyDao.addCompany(element)
    }
}