package com.asinosoft.cdm

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


/**
 * Класс загрузчика настроек.
 */
class Loader(contextBase: Context) {

    private var context: Context = contextBase
    private var myPref: SharedPreferences
    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val settingMoshi: JsonAdapter<Settings> by lazy {
        moshi.adapter(Settings().javaClass)
    }

    init {
        myPref = context.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
    }

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
            Toast.makeText(context, "Настройки не обнаружены!", Toast.LENGTH_SHORT).show()
            return  Settings()
        }
        return settingMoshi.fromJson(settings)?: Settings()
    }

    fun loadContactSettings(contactNumber : String): Settings{
        val settings = myPref.getString(contactNumber, null)
        if (settings == null) {
            //Toast.makeText(context, "Настройки не обнаружены!", Toast.LENGTH_SHORT).show()
            return loadSettings()
        }
        return settingMoshi.fromJson(settings)?: Settings()
    }

    /**
     * Сохранение настроек
     */
    fun saveSettings(settings: Settings){
        val e = myPref.edit()
        e.putString(Keys.Settings, settingMoshi.toJson(settings))
        e.apply()
    }

    fun saveContactSettings(contactNumber : String, settings: Settings){
        val e = myPref.edit()
        e.putString(contactNumber, settingMoshi.toJson(settings))
        e.apply()
    }

}
