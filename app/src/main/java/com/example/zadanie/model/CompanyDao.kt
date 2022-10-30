package com.example.zadanie.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface CompanyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCompany(element: Element)

    @Query("SELECT * FROM company_table ORDER BY id ASC")
    fun readData(): LiveData<MutableList<Element>>
}