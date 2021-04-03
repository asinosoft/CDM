package com.asinosoft.cdm.globals

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.asinosoft.cdm.R

object AlertDialogUtils {

    fun dialogListWithoutConfirm(
        context: Context,
        titleString: String
    ): Dialog {
        val popup = Dialog(context)
        popup.setCanceledOnTouchOutside(true)
        popup.setContentView(R.layout.alert_dialog_product_search_without_confirm)
        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.window?.attributes?.gravity = Gravity.CENTER
        popup.window?.setLayout(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        val closeBtn = popup.findViewById<ImageView>(R.id.close)
        val title = popup.findViewById<TextView>(R.id.title)

        title.text = titleString
        closeBtn.setOnClickListener { popup.dismiss() }

        return popup
    }
}
