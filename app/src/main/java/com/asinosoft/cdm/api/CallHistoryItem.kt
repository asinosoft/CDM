package com.asinosoft.cdm.api

import com.asinosoft.cdm.data.Contact
import java.util.*

/**
 * Информационный класс элементов истории.
 */
data class CallHistoryItem(
    val id: Long,
    val timestamp: Date, // Точное время звонка
    val phone: String, // Номер телефона
    val prettyPhone: String, // Отформатированный номер телефона
    val sim: Int, // Номер симки
    val date: String, // Дата звонка
    val time: String, // Время звонка
    val typeCall: Int, // Тип звонка (входящий/исходящий и т.д. @see CallLog.Calls)
    val duration: Long, // Длительность звонка в секундах
    val contact: Contact // Контакт, которому принадлежит этот номер телефона
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is CallHistoryItem ->
                other.timestamp == timestamp &&
                    other.phone == phone &&
                    other.typeCall == typeCall
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + typeCall
        return result
    }
}
