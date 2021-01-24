package com.asinosoft.cdm.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R

class NumbeAdapter(val callBack : (String) -> Unit) : RecyclerView.Adapter<NumberHolder>(){

    private val data : MutableList<String> = mutableListOf()
    private var enabled = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.holder_number,
            parent, false
        )
        return NumberHolder(v){
            val pos = (parent as RecyclerView).getChildAdapterPosition(v)
            if (pos < 0) return@NumberHolder
            onItemSelected(data[pos])
        }
    }

    private fun onItemSelected(item : String){
        callBack(item)
    }

    override fun onBindViewHolder(holder: NumberHolder, position: Int) {
        holder.bind(data[position])
    }

    fun enable(enable : Boolean){
        enabled = enable
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

    fun setData(sizes : List<String>){
        data.clear()
        data.addAll(sizes)
        notifyDataSetChanged()
    }
}