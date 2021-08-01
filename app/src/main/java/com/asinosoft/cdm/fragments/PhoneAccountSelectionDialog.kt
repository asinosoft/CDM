package com.asinosoft.cdm.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.fragment.app.DialogFragment
import com.asinosoft.cdm.R

/**
 * Диалог выбора сим-карты для исходящего звонка
 */
class PhoneAccountSelectionDialog(
    private val phoneAccounts: List<TelephonyManager>,
    private val onSelect: (Int) -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {

    @SuppressLint("MissingPermission")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragment ->
            val slots: Array<String> = phoneAccounts.mapIndexed { index, slot ->
                "SIM $index: ${slot.simOperatorName}"
            }.toTypedArray()

            AlertDialog.Builder(fragment)
                .setTitle(R.string.sim_selection_title)
                .setItems(slots) { dialog, choice ->
                    dialog.dismiss()
                    onSelect(choice)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                    onCancel()
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
