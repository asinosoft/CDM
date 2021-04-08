package com.asinosoft.cdm.data

class TelegramItem(
    value: String,
    val chatId: String
) : ContactItem(Type.TELEGRAM, value)
