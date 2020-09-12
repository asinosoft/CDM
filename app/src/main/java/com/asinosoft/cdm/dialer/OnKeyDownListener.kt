package com.asinosoft.cdm.dialer

import android.view.KeyEvent

interface OnKeyDownListener {
    fun onKeyPressed(keyCode: Int, event: KeyEvent)
}