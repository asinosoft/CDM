package com.asinosoft.cdm.api

import com.asinosoft.cdm.data.Contact

/**
 * Информационный класс элементов истории.
 */
data class CallHistoryItem(
    val phone: String, // Номер телефона
    val prettyPhone: String, // Отформатированный номер телефона
    val date: String, // Дата звонка
    val time: String, // Время звонка
    val typeCall: Int, // Тип звонка (входящий/исходящий и т.д. @see CallLog.Calls)
    val duration: Long, // Длительность звонка в секундах
    val contact: Contact // Контакт, которому принадлежит этот номер телефона
)
