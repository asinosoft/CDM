package com.asinosoft.cdm.adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import kotlinx.android.synthetic.main.holder_number.view.*

class NumberHolder (view : View, callBack : ()->Unit) : RecyclerView.ViewHolder(view){
    val numberTxt = view.findViewById<TextView>(R.id.number)

    init {
        view.setOnClickListener { callBack() }
    }

    fun bind(number : String){
        numberTxt.setText(number)
    }
}