package com.asinosoft.cdm.data

data class ContactWithActions(
    val contact: Contact,
    val actions: Set<Action>
)
