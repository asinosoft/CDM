package com.asinosoft.cdm.api

import android.net.Uri
import com.asinosoft.cdm.data.Contact

/**
 * Доступ к контактам
 */
interface ContactRepository {
    /**
     * Возвращает полный список контактов
     */
    fun getContacts(): Collection<Contact>

    /**
     * Возвращает контакт по его ID
     */
    fun getContactById(id: Long): Contact?

    /**
     * Возвращает контакт по его Uri
     */
    fun getContactByUri(uri: Uri): Contact?

    /**
     * Возвращает контакт, которому принадлежит указанный телефон
     */
    fun getContactByPhone(phone: String): Contact?
}
