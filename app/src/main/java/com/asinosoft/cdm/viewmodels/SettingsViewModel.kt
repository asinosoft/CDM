package com.asinosoft.cdm.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.helpers.Metoths

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val config: Config = App.instance!!.config
    val buttonColor: MutableLiveData<Int?> = MutableLiveData(config.favoritesBorderColor)
    val backgroundImages: MutableLiveData<List<Int>> = MutableLiveData()

    fun setAction(direction: Metoths.Companion.Direction, action: Action.Type) {
        when (direction) {
            Metoths.Companion.Direction.TOP -> config.swipeUpAction = action
            Metoths.Companion.Direction.DOWN -> config.swipeDownAction = action
            Metoths.Companion.Direction.LEFT -> config.swipeLeftAction = action
            Metoths.Companion.Direction.RIGHT -> config.swipeRightAction = action
            Metoths.Companion.Direction.UNKNOWN -> {
            }
        }
    }

    fun setTheme(theme: Int) {
        config.theme = theme
        buttonColor.postValue(config.favoritesBorderColor)
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
                R.drawable.background_11,
            )
        )
    }

    fun setBackgroundImage(uri: Uri?) {
        config.background = uri
    }
}
