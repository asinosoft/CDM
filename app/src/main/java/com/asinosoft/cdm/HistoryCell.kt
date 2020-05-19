package com.asinosoft.cdm

import android.graphics.drawable.Drawable
import java.io.Serializable

class HistoryCell(image: Drawable, name: String, number: String, time: String, type: Int, duration: String, date: String,  ContactID: String = ""): Serializable {

    var image: Drawable
    var nameContact: String
    var numberContact: String
    var time: String
    var typeCall: Int
    var duration = ""
    var date = ""
    var contactID = ""

    init {
        this.image = image
        nameContact = name
        numberContact = number
        this.time = time
        typeCall = type
        this.duration = duration
        this.date = date
        contactID = ContactID
    }

}