package com.asinosoft.cdm.data

import android.net.Uri

data class Contact(
    val id: Long,
    val name: String,
    val photoUri: Uri
) {
    var birthday: String? = null
    var age: Int = 0
    var starred = false

    var actions = mutableSetOf<Action>()
    val phones: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.PhoneCall }
    }

    val chats: List<Action> by lazy {
        actions.filter { action -> action.type == Action.Type.WhatsAppChat }
    }
}
