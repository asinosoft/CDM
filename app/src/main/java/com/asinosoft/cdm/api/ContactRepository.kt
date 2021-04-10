package com.asinosoft.cdm.api

/**
 * Доступ к контактам
 */
interface ContactRepository {
    /**
     * Обновляет внутренний кэш репозитория (который используется для получения контактов по ID, по телефону)
     */
    fun initialize()

    /**
     * Возвращает полный список контактов
     */
    fun getContacts(): Collection<Contact>

    /**
     * Возвращает контакт по его ID
     */
    fun getContactById(id: Long): Contact?

    /**
     * Возвращает контакт, которому принадлежит указанный телефон
     */
    fun getContactByPhone(phone: String): Contact?
}
