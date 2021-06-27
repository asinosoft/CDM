package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.asinosoft.cdm.R
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

/**
 * Класс экрана настроек приложения
 */
class SettingsActivity : BaseActivity(), ColorPickerDialogListener {

    private val model: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.analytics.logEvent("activity_settings", Bundle.EMPTY)
        setContentView(R.layout.activity_settings)
    }

    override fun onPause() {
        model.save()
        super.onPause()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        model.settings.colorBorder = color
        model.buttonColor.postValue(color)
    }

    override fun onDialogDismissed(dialogId: Int) {
    }
}
