package com.asinosoft.cdm.api

/**
 * Доступ к контактам
 */
interface ContactRepository {
    fun getContacts(): Collection<Contact>

    fun getContactById(id: Long): Contact?

    fun getContactByPhone(phone: String): Contact?
}
