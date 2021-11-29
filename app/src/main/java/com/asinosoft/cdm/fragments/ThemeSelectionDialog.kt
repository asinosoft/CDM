package com.asinosoft.cdm.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.R
import com.asinosoft.cdm.viewmodels.SettingsViewModel

/**
 * Диалог выбора темы оформления
 */
class ThemeSelectionDialog(
    val onSelect: (Int) -> Unit
) : DialogFragment() {
    private val model: SettingsViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.themes)
                .setSingleChoiceItems(R.array.themeNames, model.settings.theme) { dialog, choice ->
                    dialog.dismiss()
                    onSelect(choice)
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
