package com.example.zadanie.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CompanyWithMembers::class], version = 1, exportSchema = false)
abstract class CompaniesWithMembersDB: RoomDatabase() {
    abstract fun companyDao(): CompanyDao

    companion object {
        @Volatile
        private var INSTANCE: CompaniesWithMembersDB? = null

        fun getDatabase(context: Context): CompaniesWithMembersDB {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompaniesWithMembersDB::class.java,
                    "companies_with_members_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

@Database(entities = [Element::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NearbyCompanyDatabase: RoomDatabase() {
    abstract fun nearbyCompanyDao(): NearbyCompanyDao

    companion object {
        @Volatile
        private var INSTANCE: NearbyCompanyDatabase? = null

        fun getDatabase(context: Context): NearbyCompanyDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NearbyCompanyDatabase::class.java,
                    "nearby_company_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}