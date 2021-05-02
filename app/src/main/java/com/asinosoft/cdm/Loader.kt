package com.asinosoft.cdm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.asinosoft.cdm.data.*
import com.google.gson.Gson

/**
 * Класс загрузчика настроек.
 */
object Loader {

    private var myPref: SharedPreferences =
        App.INSTANCE.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)

    /**
     * Загрузка настроек
     */
    fun loadSettings(): Settings {
        val settings = myPref.getString(Keys.Settings, null)
        if (settings == null) {
            return Settings()
        }
        return Gson().fromJson(settings, Settings().javaClass) ?: Settings()
    }

    fun loadContactSettings(contact: Contact): DirectActions {
        val settings = myPref.getString(Keys.CONTACT_PREFERENCES + contact.id, null)
        return if (settings == null) {
            getDefaultContactActions(contact)
        } else {
            Gson().fromJson(settings, ContactSettings::class.java)
                ?.let { getContactActions(contact, it) }
                ?: getDefaultContactActions(contact)
        }
    }

    /**
     * Возвращает набор действий для контакта в соответствии с его настройками
     */
    private fun getContactActions(
        contact: Contact,
        settings: ContactSettings
    ): DirectActions {
        val defaultSettings = loadSettings()
        val left = contact.actions.find { it.id == settings.leftButton } ?: getActionByType(
            contact,
            defaultSettings.leftButton
        )
        val right = contact.actions.find { it.id == settings.rightButton } ?: getActionByType(
            contact,
            defaultSettings.rightButton
        )
        val top = contact.actions.find { it.id == settings.topButton } ?: getActionByType(
            contact,
            defaultSettings.topButton
        )
        val bottom = contact.actions.find { it.id == settings.bottomButton } ?: getActionByType(
            contact,
            defaultSettings.bottomButton
        )
        return DirectActions(left, right, top, bottom)
    }

    /**
     * Возвращает набор действий для контакта в соответствии с глобальными дефолтными настройками
     */
    private fun getDefaultContactActions(contact: Contact): DirectActions {
        val settings = loadSettings()
        val left = getActionByType(contact, settings.leftButton)
        val right = getActionByType(contact, settings.rightButton)
        val top = getActionByType(contact, settings.topButton)
        val bottom = getActionByType(contact, settings.bottomButton)
        return DirectActions(left, right, top, bottom)
    }

    /**
     * Сохранение настроек
     */
    fun saveSettings(settings: Settings) {
        val e = myPref.edit()
        e.putString(Keys.Settings, Gson().toJson(settings))
        e.apply()
    }

    fun saveContactSettings(contact: Contact, directActions: DirectActions) {
        Log.d("saveContactSettings # ${contact.name}", "left: ${directActions.left.type.name},\n right: ${directActions.right.type.name},\n top: ${directActions.top.type.name},\n bottom: ${directActions.down.type.name}")
        val settings = ContactSettings(
            directActions.left.id,
            directActions.right.id,
            directActions.top.id,
            directActions.down.id
        )
        val e = myPref.edit()
        e.putString(Keys.CONTACT_PREFERENCES + contact.id, Gson().toJson(settings))
        e.apply()
    }

    private fun getActionByType(contact: Contact, type: Action.Type): Action {
        return contact.actions.firstOrNull { it.type == type }
            ?: Action(0, type, "", "")
    }
}
