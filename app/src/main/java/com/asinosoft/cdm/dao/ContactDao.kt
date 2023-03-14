package com.asinosoft.cdm.dao

import androidx.room.*
import com.asinosoft.cdm.data.Contact

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    fun all(): List<Contact>

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun find(id: Int): Contact?

    @Insert
    fun insert(contact: Contact)

    @Upsert
    fun upsert(contact: Contact)

    @Delete
    fun delete(contact: Contact)
}
