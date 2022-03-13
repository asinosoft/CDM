package com.asinosoft.cdm.helpers

import android.content.Context
import android.widget.ArrayAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.data.Action
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SelectPhoneDialog(
    val context: Context,
    val actions: List<Action>,
    val onSelect: (Action) -> Unit,
    val onCancel: () -> Unit = {}
) {

    fun show() {
        val adapter = ArrayAdapter(context, android.R.layout.select_dialog_item, actions)

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.select_number)
            .setAdapter(adapter) { dialog, index ->
                onSelect(actions[index])
                dialog.dismiss()
            }
            .setOnDismissListener { onCancel() }
            .create()
            .show()
    }
}
