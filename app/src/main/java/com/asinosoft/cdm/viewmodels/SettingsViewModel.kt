package com.asinosoft.cdm.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.helpers.Metoths
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val settings: Settings = Loader.loadSettings(getApplication())
    val buttonColor: MutableLiveData<Int> = MutableLiveData(settings.colorBorder)
    val backgroundImages: MutableLiveData<List<Int>> = MutableLiveData()

    fun setAction(direction: Metoths.Companion.Direction, action: Action.Type) {
        when (direction) {
            Metoths.Companion.Direction.TOP -> settings.topButton = action
            Metoths.Companion.Direction.DOWN -> settings.bottomButton = action
            Metoths.Companion.Direction.LEFT -> settings.leftButton = action
            Metoths.Companion.Direction.RIGHT -> settings.rightButton = action
            Metoths.Companion.Direction.UNKNOWN -> {
            }
        }
        save()

        Analytics.logGlobalSetAction(direction.name, action.name)
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
                R.drawable.background_7,
                R.drawable.background_8,
                R.drawable.background_9,
                R.drawable.background_10,
            )
        )
    }

    fun setBackgroundImage(uri: Uri?) {
        val context: Context = getApplication()

        if (null == uri) {
            context.deleteFile("background")
        } else {
            context.contentResolver.openInputStream(uri)?.copyTo(
                File(context.filesDir, "background").outputStream()
            )

            // Переключаемся в специальную тёмную тему
            settings.theme = 3
        }
        Analytics.logBackground()
    }

    fun save() {
        Loader.saveSettings(getApplication(), settings)
    }
}
