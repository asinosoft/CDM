package com.asinosoft.cdm

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView

data class BackupHistoryItem(val imageCon: CircleImageView, val imageLeft: CircleImageView, val imageRight: CircleImageView, val timeCon: TextView, val dateCon: TextView, val marStart: Int, val marEndTime: Int) {

    fun reset(){
            imageCon.setMargins(start = marStart)
            timeCon.setMargins(end = marEndTime)
            dateCon.setMargins(end = marEndTime)
            imageLeft.visibility = View.INVISIBLE
            imageRight.visibility = View.INVISIBLE
        }

    fun View.setMargins(start:Int = MainActivity.nullNum, top:Int = MainActivity.nullNum, end:Int = MainActivity.nullNum, bottom:Int = MainActivity.nullNum){
        var layoutParams = this.layoutParams as RelativeLayout.LayoutParams
        layoutParams.marginStart = if(start != MainActivity.nullNum) start else layoutParams.marginStart
        layoutParams.topMargin = if(top != MainActivity.nullNum) top else layoutParams.topMargin
        layoutParams.marginEnd = if(end != MainActivity.nullNum) end else layoutParams.marginEnd
        layoutParams.bottomMargin = if(bottom != MainActivity.nullNum) bottom else layoutParams.bottomMargin
        this.layoutParams = layoutParams
    }
}