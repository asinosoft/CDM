package com.asinosoft.cdm.api

import android.net.Uri
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions

/**
 * Настройки интерфейса и поведения приложения
 */
interface Config {
    val isFirstRun: Boolean

    fun applyRemoteConfig()

    // Выбранная тема
    var theme: Int

    // Фоновая картинка или null
    var background: Uri?

    // Поведение приложения при старте: проверять, что оно является дозвонщиком по-умолчанию
    var checkDefaultDialer: Boolean

    // Настройки внешнего вида блока избранных контактов
    var favoritesFirst: Boolean
    var favoritesColumnCount: Int
    var favoritesSize: Int
    var favoritesBorderColor: Int?
    var favoritesBorderWidth: Int

    // Общие настройки swipe-действий
    var swipeLeftAction: Action.Type
    var swipeRightAction: Action.Type
    var swipeUpAction: Action.Type
    var swipeDownAction: Action.Type

    // Горизонтальная черта, разделяющая записи в истории звонков
    var listDivider: Boolean

    // Настройки swipe-действия для конкретного контакта
    fun getContactSettings(contact: Contact): DirectActions
    fun setContactSettings(contact: Contact, actions: DirectActions)
}
