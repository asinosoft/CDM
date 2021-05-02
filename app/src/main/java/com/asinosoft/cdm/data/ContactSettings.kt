package com.asinosoft.cdm.data

/**
 * Настройки действий для контакта
 *
 * Внимание! В настройках хранится только ID действия (ContactsContract.Data._ID)
 */
class ContactSettings(
    var leftButton: Int,
    var rightButton: Int,
    var topButton: Int,
    var bottomButton: Int,
)
