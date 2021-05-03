package com.asinosoft.cdm.api

import android.content.Context
import com.asinosoft.cdm.data.Contact

/**
 * Доступ к истории звонков
 */
interface CallHistoryRepository {
    /**
     * Возвращает список последних звонков
     */
    fun getLatestHistory(context: Context): List<CallHistoryItem>

    /**
     * Возвращает список звонков, относящихся к данному контакту
     */
    fun getHistoryByContact(context: Context, contact: Contact): List<CallHistoryItem>

    /**
     * Возвращает список звонков, относящихся к данному номеру телефона
     */
    fun getHistoryByPhone(context: Context, phone: String): List<CallHistoryItem>
}
