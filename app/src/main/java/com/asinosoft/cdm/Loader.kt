package com.asinosoft.cdm

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * Класс загрузчика настроек.
 */
object Loader {

    private var myPref: SharedPreferences =
        App.INSTANCE.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)

    /**
     * Проверка на наличие настроек
     */
    fun settingsExists(): Boolean{
        return myPref.getString(Keys.Settings, null) != null
    }

    /**
     * Загрузка настроек
     */
    fun loadSettings(): Settings{
        val settings = myPref.getString(Keys.Settings, null)
        if (settings == null) {
            return  Settings()
        }
        return Gson().fromJson(settings!!, Settings().javaClass)?: Settings()
    }

    fun loadContactSettings(contactNumber : String): Settings{
        val settings = myPref.getString(contactNumber, null)
        if (settings == null) {
            return loadSettings()
        }
        return Gson().fromJson(settings!!, Settings().javaClass)?: Settings()
    }

    /**
     * Сохранение настроек
     */
    fun saveSettings(settings: Settings){
        val e = myPref.edit()
        e.putString(Keys.Settings, Gson().toJson(settings))
        e.apply()
    }

    fun saveContactSettings(contactNumber : String, settings: Settings){
        val e = myPref.edit()
        e.putString(contactNumber, Gson().toJson(settings))
        e.apply()
    }

}
