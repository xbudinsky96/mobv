package com.example.zadanie.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CompanyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCompany(company: CompanyWithMembers)

    @Query("SELECT * FROM company_table ORDER BY bar_id ASC")
    fun readCompanies(): LiveData<MutableList<CompanyWithMembers>>

    @Delete
    suspend fun deleteCompany(company: CompanyWithMembers)

    @Query("DELETE FROM company_table")
    fun deleteTable()
}

@Dao
interface NearbyCompanyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCompany(element: Element)

    @Query("SELECT * FROM nearby_company_table ORDER BY id ASC")
    fun readCompanies(): LiveData<MutableList<Element>>
}