package com.asinosoft.cdm.dialer

import android.net.Uri

// a simpler Contact model containing just info needed at the call screen
data class CallContact(var name: String, var photoUri: Uri, var number: String)
