package com.example.zadanie.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.zadanie.model.CompanyWithMembers
import com.example.zadanie.model.Element

@Dao
interface CompanyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCompany(company: CompanyWithMembers)

    @Query("SELECT * FROM company_table ORDER BY bar_id ASC")
    fun readCompanies(): LiveData<MutableList<CompanyWithMembers>>

    @Query("SELECT * FROM company_table WHERE bar_id = :id")
    fun getCompanyById(id: String): CompanyWithMembers

    @Query("DELETE FROM company_table")
    suspend fun deleteCompanies()
}

@Dao
interface NearbyCompanyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCompany(element: Element)

    @Query("SELECT * FROM nearby_company_table ORDER BY id ASC")
    fun readCompanies(): LiveData<MutableList<Element>>

    @Query("DELETE FROM nearby_company_table")
    suspend fun deleteCompanies()
}