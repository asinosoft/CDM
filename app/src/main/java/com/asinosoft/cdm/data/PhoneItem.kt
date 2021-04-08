package com.asinosoft.cdm.data

import com.asinosoft.cdm.detail_contact.StHelper

class PhoneItem(
    val phoneType: Int,
    value: String
) : ContactItem(Type.PHONE, value) {
    val prettyNumber by lazy { StHelper.convertNumber(value) ?: value }
}
