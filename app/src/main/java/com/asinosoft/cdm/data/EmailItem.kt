package com.asinosoft.cdm.data

class EmailItem(
    val emailType: Int,
    val email: String
) : ContactItem(Type.EMAIL, email)
