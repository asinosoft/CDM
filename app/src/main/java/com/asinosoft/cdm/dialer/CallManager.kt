package com.asinosoft.cdm.dialer

import android.net.Uri
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log

class CallManager {

    companion object {
        private var call: Call? = null

        fun isCalled(): Boolean = null != call

        fun setCall(value: Call) {
            Log.d("CallManager", "setCall")
            call = value
        }

        fun resetCall() {
            Log.d("CallManager", "resetCall")
            call = null
        }

        fun accept() {
            Log.d("CallManager", "accept")
            call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }

        fun reject() {
            Log.d("CallManager", "reject")
            call?.let { call ->
                if (call.state == Call.STATE_RINGING) {
                    call.reject(false, null)
                } else {
                    call.disconnect()
                }
            }
        }

        fun registerCallback(callback: Call.Callback) {
            call?.registerCallback(callback)
        }

        fun unregisterCallback(callback: Call.Callback) {
            call?.unregisterCallback(callback)
        }

        fun getState() = if (call == null) {
            Call.STATE_DISCONNECTED
        } else {
            call!!.state
        }

        fun keypad(c: Char) {
            call?.playDtmfTone(c)
            call?.stopDtmfTone()
        }

        fun hold(hold: Boolean) {
            if (call != null) {
                if (hold) call!!.hold()
                else call!!.unhold()
            }
        }

        fun getCallPhone(): Uri? {
            return call?.details?.handle
        }
    }
}
