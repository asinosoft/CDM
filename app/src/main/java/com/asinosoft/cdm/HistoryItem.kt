package com.asinosoft.cdm

/**
 * Информационный класс элементов истории.
 */
data class HistoryItem(
    var nameContact: String,
    var numberContact: String,
    var time: String,
    var typeCall: Int,
    var duration: String,
    var date: String,
    var contactID: String
)