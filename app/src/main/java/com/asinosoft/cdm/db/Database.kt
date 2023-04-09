package com.asinosoft.cdm.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Contact::class, Action::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun contacts(): Contacts

    abstract fun actions(): Actions
}
