package com.asinosoft.cdm

import android.graphics.drawable.Drawable

data class HistoryItem(

//    var image: Drawable,
    var nameContact: String,
    var numberContact: String,
    var time: String,
    var typeCall: Int,
    var duration: String = "",
    var date: String = "",
    var contactID: String = "",
    var photoUrl: String? = null,
    var _ID: Long? = null,
    var _PhotoID: Int? = null
)