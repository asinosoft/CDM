package com.asinosoft.cdm.viewmodels

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val settings: Settings = Loader.loadSettings(getApplication())
    val buttonColor: MutableLiveData<Int> = MutableLiveData(settings.colorBorder)
    val backgroundImages: MutableLiveData<List<Int>> = MutableLiveData()

    fun save() {
        val oldSettings = Loader.loadSettings(getApplication())

        if (settings == oldSettings) {
            return
        }

        Loader.saveSettings(getApplication(), settings)

        Firebase.analytics.logEvent(
            "global_settings",
            Bundle().apply {
                putInt("size", settings.sizeCirs)
                putInt("columns", settings.columnsCirs)
                putInt("borderWidth", settings.borderWidthCirs)
                putInt("borderColor", settings.colorBorder)
                putString("action_up", settings.topButton.name)
                putString("action_down", settings.bottomButton.name)
                putString("action_left", settings.leftButton.name)
                putString("action_right", settings.rightButton.name)
                putString("layout", if (settings.historyButtom) "down" else "up")
            }
        )
    }

    fun setAction(direction: Metoths.Companion.Direction, action: Action.Type) {
        when (direction) {
            Metoths.Companion.Direction.TOP -> settings.topButton = action
            Metoths.Companion.Direction.DOWN -> settings.bottomButton = action
            Metoths.Companion.Direction.LEFT -> settings.leftButton = action
            Metoths.Companion.Direction.RIGHT -> settings.rightButton = action
            Metoths.Companion.Direction.UNKNOWN -> TODO("Неизвестное направление")
        }

        Firebase.analytics.logEvent(
            "global_set_action",
            Bundle().apply {
                putString("direction", direction.name)
                putString("action", action.name)
            }
        )
    }

    fun loadBackgroundImages() {
        backgroundImages.postValue(
            listOf(
                R.drawable.background_1,
                R.drawable.background_2,
                R.drawable.background_3,
                R.drawable.background_4,
                R.drawable.background_5,
                R.drawable.background_6,
            )
        )
    }

    fun setBackgroundImage(uri: String?) {
        (getApplication() as Context)
            .getSharedPreferences(Keys.Preference, Context.MODE_PRIVATE)
            .edit()
            .putString(Keys.BACKGROUND_IMAGE, uri.toString())
            .apply()
    }
}
