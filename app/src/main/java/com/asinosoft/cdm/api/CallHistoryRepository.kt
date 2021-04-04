package com.asinosoft.cdm.api

/**
 * Доступ к истории звонков
 */
interface CallHistoryRepository {
    /**
     * Возвращает список последних звонков
     */
    fun getLatestHistory(): List<CallHistoryItem>

    /**
     * Возвращает список звонков, относящихся к данному контакту
     */
    fun getHistoryByContactId(contactId: Long): List<CallHistoryItem>

    /**
     * Возвращает список звонков, относящихся к данному номеру телефона
     */
    fun getHistoryByPhone(phone: String): List<CallHistoryItem>
}
