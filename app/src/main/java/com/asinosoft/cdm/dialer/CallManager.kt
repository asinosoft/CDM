package com.asinosoft.cdm.dialer

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log
import com.asinosoft.cdm.api.ContactRepositoryImpl

class CallManager {

    companion object {
        private var call: Call? = null
        private var inCallService: InCallService? = null

        fun isCalled(): Boolean = null != call

        fun setCall(service: InCallService, value: Call) {
            Log.d("CallManager", "setCall")
            inCallService = service
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

        fun setMuted(muted: Boolean) {
            inCallService?.setMuted(muted)
        }

        fun setAudioRoute(route: Int) {
            inCallService?.setAudioRoute(route)
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

        fun getCallContact(context: Context, callback: (CallContact) -> Unit) {
            Log.d("CallManager", "getCallContact")
            if (call == null || call!!.details == null || call!!.details!!.handle == null) {
                Log.w("CallManager", "NO CONTACT")
                callback(CallContact())
                return
            }

            val uri = Uri.decode(call!!.details.handle.toString())
            Log.w("CallManager", uri)
            if (uri.startsWith("tel:")) {
                val number = uri.substringAfter("tel:")
                Log.w("CallManager", "number = $number")
                val callContact = CallContact(number)
                ContactRepositoryImpl(context).getContactByPhone(number)?.let {
                    Log.w("CallManager", "found = ${it.name}")
                    callContact.name = it.name
                    callContact.photoUri = it.photoUri ?: ""
                }

                callback(callContact)
            }
        }
    }
}
