package com.asinosoft.cdm

import androidx.room.Database
import androidx.room.RoomDatabase
import com.asinosoft.cdm.dao.ContactDao
import com.asinosoft.cdm.data.Contact

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun contacts(): ContactDao
}
