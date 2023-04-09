package com.asinosoft.cdm.db

import androidx.room.*

@Dao
interface Contacts {
    @Upsert
    fun upsert(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT * FROM contacts JOIN actions ON actions.contactId = contacts.id")
    fun contactsWithActions(): Map<Contact, List<Action>>
}
