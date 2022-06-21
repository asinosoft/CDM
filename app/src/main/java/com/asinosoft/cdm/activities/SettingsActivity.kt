package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

/**
 * Класс экрана настроек приложения
 */
class SettingsActivity : BaseActivity(), ColorPickerDialogListener {

    private val model: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Analytics.logActivitySettings()
        setContentView(R.layout.activity_settings)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        model.config.favoritesBorderColor = color
        model.buttonColor.postValue(color)
        Analytics.logFavoritesBorderColor()
    }

    override fun onDialogDismissed(dialogId: Int) {
    }
}
