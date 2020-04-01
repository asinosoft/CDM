package ru.n0de.manager

import android.graphics.drawable.Drawable
import android.provider.CallLog
import java.io.Serializable
import java.net.URL
import java.time.Duration

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