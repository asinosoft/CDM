package com.asinosoft.cdm.dialer

import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log

class CallManager {

    companion object {
        private var call: Call? = null

        fun getCall() = call

        fun setCall(call: Call) {
            Log.d("CDM|CallManager", "setCall → ${call.details.handle} | ${call.state}")
            this.call = call
        }

        fun resetCall() {
            Log.d("CDM|CallManager", "resetCall")
            call = null
        }

        fun accept() {
            Log.d("CDM|CallManager", "accept")
            call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }

        fun reject() {
            Log.d("CDM|CallManager", "reject")
            call?.let { call ->
                if (call.state == Call.STATE_RINGING) {
                    call.reject(false, null)
                } else {
                    call.disconnect()
                }
            }
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
    }
}
