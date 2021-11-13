package com.asinosoft.cdm.dialer

import android.telecom.Call
import android.telecom.VideoProfile
import timber.log.Timber

class CallManager {

    companion object {
        private var call: Call? = null

        fun getCall() = call

        fun setCall(call: Call) {
            Timber.d("setCall → ${call.details.handle} | ${call.state}")
            this.call = call
        }

        fun resetCall() {
            Timber.d("resetCall")
            call = null
        }

        fun accept() {
            Timber.d("accept")
            call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }

        fun reject() {
            Timber.d("reject")
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
