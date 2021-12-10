package com.asinosoft.cdm.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R

/**
 * Адаптер для режима, когда нет разрешений на чтение истории звонков
 */
class PermissionRationaleAdapter(
    private val favorites: View,
    private val onPermitClick: () -> Unit
) :
    RecyclerView.Adapter<PermissionRationaleAdapter.Holder>() {

    override fun getItemCount(): Int = 2

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        when (viewType) {
            0 -> favorites
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_permission_rationale, parent, false)
                .apply {
                    findViewById<Button>(R.id.give_permission).setOnClickListener { onPermitClick() }
                }
        }
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {}

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view)
}
