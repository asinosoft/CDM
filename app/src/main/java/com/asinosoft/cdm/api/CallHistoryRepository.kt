package com.asinosoft.cdm.api

import android.content.Context
import com.asinosoft.cdm.data.Contact
import java.util.*

/**
 * Доступ к истории звонков
 */
interface CallHistoryRepository {

    interface Filter {
        fun filter(call: CallHistoryItem): Boolean
    }

    /**
     * Возвращает список последних звонков, произошедших ДО указанного времени
     */
    fun getLatestHistory(
        context: Context,
        before: Date,
        limit: Int,
        filter: CallHistoryFilter
    ): List<CallHistoryItem>

    /**
     * Возвращает список звонков, произошедших ПОСЛЕ указанного времени
     */
    fun getNewestHistory(context: Context, after: Date): List<CallHistoryItem>

    /**
     * Возвращает список звонков, относящихся к данному контакту
     */
    fun getHistoryByContact(context: Context, contact: Contact): List<CallHistoryItem>

    /**
     * Возвращает список звонков, относящихся к данному номеру телефона
     */
    fun getHistoryByPhone(context: Context, phone: String): List<CallHistoryItem>
}
