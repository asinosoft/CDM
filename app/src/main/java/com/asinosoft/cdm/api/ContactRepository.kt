package com.asinosoft.cdm.api

import com.asinosoft.cdm.detail_contact.Contact

/**
 * Доступ к контактам пользователя
 */
interface ContactRepository {
    fun getContacts(): Collection<Contact>

    fun getContactById(id: Long): Contact?

    fun getContactByPhone(phone: String): Contact?
}
