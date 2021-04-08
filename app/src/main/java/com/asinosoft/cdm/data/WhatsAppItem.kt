package com.asinosoft.cdm.data

class WhatsAppItem(number: String) : ContactItem(Type.WHATSAPP, number) {
    var chatId: String? = null
    var audioId: String? = null
    var videoId: String? = null
}
