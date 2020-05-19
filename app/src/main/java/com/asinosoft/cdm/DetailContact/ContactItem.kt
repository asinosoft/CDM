package com.asinosoft.cdm.DetailContact

import android.net.Uri

class ContactItem(
    var name: String?,
    var number: String?,
    var numberType: Int?,
    var email: String?,
    var emailType: Int?,
    var rawContactId: String?,
    var whatsAppCallId: Int,
    var whatsAppVideoCallId: Int,
    var hasViber: Int?,
    var viberUri: Uri?,
    var hasTelegram: String?,
    var telegramId: String?,
    var skypeName: String?,
    var skypeId: String?
){

}