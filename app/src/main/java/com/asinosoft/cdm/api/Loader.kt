package com.asinosoft.cdm.api

import android.content.Context
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.helpers.Keys
import com.google.gson.Gson

/**
 * Класс загрузчика настроек.
 */
object Loader {
    /**
     * Загрузка настроек
     */
    fun loadSettings(context: Context): Settings {
        val preferences = context.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
        val settings = preferences.getString(Keys.Settings, null)
        if (settings == null) {
            return Settings()
        }
        return Gson().fromJson(settings, Settings().javaClass) ?: Settings()
    }

    fun loadContactSettings(context: Context, contact: Contact): DirectActions {
        val preferences = context.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
        val settings = preferences.getString(Keys.CONTACT_PREFERENCES + contact.id, null)
        val globalSettings = loadSettings(context)
        return if (settings == null) {
            getDefaultContactActions(contact, globalSettings)
        } else {
            Gson().fromJson(settings, ContactSettings::class.java)
                ?.let { getContactActions(contact, it) }
                ?: getDefaultContactActions(contact, globalSettings)
        }
    }

    /**
     * Возвращает набор действий для контакта в соответствии с его настройками
     */
    private fun getContactActions(
        contact: Contact,
        settings: ContactSettings
    ): DirectActions {
        val left = getContactAction(contact, settings.left)
        val right = getContactAction(contact, settings.right)
        val top = getContactAction(contact, settings.top)
        val bottom = getContactAction(contact, settings.bottom)
        return DirectActions(left, right, top, bottom)
    }

    /**
     * Ищет подходящее действие: сначала по ID+типу, затем просто по типу
     */
    private fun getContactAction(contact: Contact, like: ActionSettings): Action {
        return contact.actions.find { it.id == like.id && it.type == like.type }
            ?: contact.actions.firstOrNull { it.type == like.type }
            ?: Action(0, like.type, "", "")
    }

    /**
     * Возвращает набор действий для контакта в соответствии с глобальными дефолтными настройками
     */
    private fun getDefaultContactActions(
        contact: Contact,
        globalSettings: Settings
    ): DirectActions {
        val left = getActionByType(contact, globalSettings.leftButton)
        val right = getActionByType(contact, globalSettings.rightButton)
        val top = getActionByType(contact, globalSettings.topButton)
        val bottom = getActionByType(contact, globalSettings.bottomButton)
        return DirectActions(left, right, top, bottom)
    }

    /**
     * Сохранение настроек
     */
    fun saveSettings(context: Context, settings: Settings) {
        context.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
            .edit()
            .putString(Keys.Settings, Gson().toJson(settings))
            .apply()
    }

    fun saveContactSettings(context: Context, contact: Contact, directActions: DirectActions) {
        val settings = ContactSettings(
            ActionSettings(directActions.left.id, directActions.left.type),
            ActionSettings(directActions.right.id, directActions.right.type),
            ActionSettings(directActions.top.id, directActions.top.type),
            ActionSettings(directActions.down.id, directActions.down.type),
        )
        context.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
            .edit()
            .putString(Keys.CONTACT_PREFERENCES + contact.id, Gson().toJson(settings))
            .apply()
    }

    private fun getActionByType(contact: Contact, type: Action.Type): Action {
        return contact.actions.firstOrNull { it.type == type }
            ?: Action(0, type, "", "")
    }

    class ContactSettings(
        var left: ActionSettings,
        var right: ActionSettings,
        var top: ActionSettings,
        var bottom: ActionSettings,
    )

    class ActionSettings(
        val id: Int,
        val type: Action.Type,
    )
}
