package com.asinosoft.cdm

import androidx.room.RoomDatabase
import com.asinosoft.cdm.dao.ContactDao
import com.asinosoft.cdm.data.Contact

@androidx.room.Database(entities = [Contact::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun contacts(): ContactDao
}
