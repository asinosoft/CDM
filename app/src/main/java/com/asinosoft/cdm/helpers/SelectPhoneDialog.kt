package com.asinosoft.cdm.helpers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.SelectorAdapter
import com.asinosoft.cdm.data.Action

class SelectPhoneDialog(
    val context: Context,
    val actions: List<Action>,
    val onSelect: (Action) -> Unit,
    val onCancel: () -> Unit = {}
) {

    fun show() {
        val dialog =
            AlertDialogUtils.dialogListWithoutConfirm(
                context,
                context.getString(R.string.select_number)
            )
        val adapter = SelectorAdapter(actions) { action ->
            onSelect(action)
            dialog.dismiss()
        }
        dialog.setOnCancelListener { onCancel() }

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_popup)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        dialog.show()
    }
}
