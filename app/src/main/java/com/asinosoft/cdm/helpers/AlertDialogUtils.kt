package com.asinosoft.cdm.helpers

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
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

        val popupColor = TypedValue()
        context.theme.resolveAttribute(R.attr.popupColor, popupColor, true)
        popup.window?.setBackgroundDrawable(ColorDrawable(popupColor.data))
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
