package com.asinosoft.cdm.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R

class SelectorAdapter(
    private val data: List<String>,
    private val callBack: (String) -> Unit
) : RecyclerView.Adapter<SelectorAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.holder_number,
            parent, false
        )
        return Holder(v) {
            val pos = (parent as RecyclerView).getChildAdapterPosition(v)
            if (pos >= 0) {
                onItemSelected(data[pos])
            }
        }
    }

    private fun onItemSelected(item: String) {
        callBack(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    class Holder(view: View, callBack: () -> Unit) : RecyclerView.ViewHolder(view) {
        private val numberText: TextView = view.findViewById(R.id.number)

        init {
            view.setOnClickListener { callBack() }
        }

        fun bind(number: String) {
            numberText.text = number
        }
    }
}
