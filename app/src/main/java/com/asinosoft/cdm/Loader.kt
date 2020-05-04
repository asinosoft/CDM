package com.asinosoft.cdm

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap

class Loader(contextBase: Context) {

    private var context: Context = contextBase
    private var myPref: SharedPreferences
    private var gson: Gson = Gson()

    init {
        myPref = context.getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
    }

    fun settingsExists(): Boolean{
        return myPref.getString(Keys.Settings, null) != null
    }

    fun loadSettings(): Settings{
        val settings = myPref.getString(Keys.Settings, null)
        if (settings == null) {
            Toast.makeText(context, "Настройки не обнаружены!", Toast.LENGTH_SHORT).show()
            return  Settings()
        }
        return gson.fromJson(settings, Settings::class.java)
    }

    fun saveSettings(settings: Settings){
        val e = myPref.edit()
        e.putString(Keys.Settings, gson.toJson(settings))
        e.apply()
    }

    fun saveCirs(cirs: ArrayList<CirView>){
        val e = myPref.edit()
        val cirSers = ArrayList<Cir>()
        cirs.forEach { cir ->
            cirSers.add(cir.toCir())
        }
        e.putString(Keys.Cirs, gson.toJson(cirSers).toString())
        e.apply()
    }

    fun loadCirs(): ArrayList<Cir>?{
        val cirs = myPref.getString(Keys.Cirs, null)
        if (cirs == null) {
            Toast.makeText(context, "Сохранения не обнаружены!", Toast.LENGTH_SHORT)
            return  null
        }
        val map =  gson.fromJson(cirs, ArrayList<LinkedTreeMap<String, String>>()::class.java)
        var listCirs = ArrayList<Cir>()
        map.forEach { cir ->
            var CirT = Cir(
            IdContact = cir["IdContact"]!!.toString(),
            Number = cir["Number"]!!.toString(),
            Email = cir["Email"]!!.toString(),
            Name = cir["Name"]!!.toString())
            listCirs.add(CirT)
        }
        return listCirs
    }

    /*fun save(param: Any){
        val e = myPref.edit()
        var key = ""
        when(param){
             param.equals(Settings::class.java) -> key = Keys.Settings
             param is ArrayList<*> -> key = Keys.Cirs
        }
        e.putString(Keys.Settings, gson.toJson(param))
        e.apply()
    }*/
}
