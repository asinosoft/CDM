package com.asinosoft.cdm.data

class ViberItem(
    number: String,
    val videoId: String
) : ContactItem(Type.VIBER, number)
