package com.asinosoft.cdm

import com.asinosoft.cdm.detail_contact.Contact

data class CirPairData(
    val contact: Contact? = null,
    val contactSettings: ContactSettings? = null,
    val selectedNumber : String? = null
)