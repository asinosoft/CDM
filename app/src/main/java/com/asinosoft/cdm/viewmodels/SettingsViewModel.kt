package com.asinosoft.cdm.viewmodels

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.asinosoft.cdm.activities.SettingsActivity
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.helpers.Metoths
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val settings: Settings = Loader.loadSettings(getApplication())
    val buttonColor: MutableLiveData<Int> = MutableLiveData(settings.colorBorder)

    fun save(): Int {
        val oldSettings = Loader.loadSettings(getApplication())

        if (settings == oldSettings) {
            return Activity.RESULT_OK
        } else {
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

            return if (settings.theme == oldSettings.theme) SettingsActivity.SETTINGS_CHANGED else SettingsActivity.THEME_CHANGED
        }
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
}
