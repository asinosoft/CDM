package com.asinosoft.cdm.api

import android.net.Uri
import com.asinosoft.cdm.data.Contact

class SingleContactRepository(val contact: Contact) : ContactRepository {
    override fun getContacts(): Collection<Contact> = listOf(contact)

    override fun getContactById(id: Long): Contact? = contact

    override fun getContactByUri(uri: Uri): Contact? = contact

    override fun getContactByPhone(phone: String): Contact? = contact
}
