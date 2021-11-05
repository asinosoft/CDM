package com.asinosoft.cdm.adapters

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Адаптер списка опций с иконками! для AlertDialog
 */
class StringsWithIconsAdapter(
    context: Context,
    items: Array<String>,
    private val icons: Array<Int>,
) : ArrayAdapter<String>(context, android.R.layout.select_dialog_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val text = view.findViewById<TextView>(android.R.id.text1)
        text.setCompoundDrawablesRelativeWithIntrinsicBounds(icons[position], 0, 0, 0)
        text.compoundDrawablePadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12f,
            context.resources.displayMetrics
        ).toInt()
        return view
    }
}
