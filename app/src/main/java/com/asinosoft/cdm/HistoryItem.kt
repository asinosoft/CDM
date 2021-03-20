package com.asinosoft.cdm

import com.asinosoft.cdm.detail_contact.Contact

/**
 * Информационный класс элементов истории.
 */
data class HistoryItem(
    var numberContact: String,
    var time: String,
    var typeCall: Int,
    var duration: String,
    var date: String,
    val contact: Contact
)