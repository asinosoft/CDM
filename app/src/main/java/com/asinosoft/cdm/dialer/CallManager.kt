package com.asinosoft.cdm.dialer

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import com.asinosoft.cdm.detail_contact.Contact

class CallManager {

    companion object {
        var call: Call? = null
        var callDialer = Contact()
        var inCallService: InCallService? = null

        fun accept() {
            call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }

        fun reject() {
            if (call != null) {
                if (call!!.state == Call.STATE_RINGING) {
                    call!!.reject(false, null)
                } else {
                    call!!.disconnect()
                }
            }
        }

        fun registerCallback(callback: Call.Callback) {
            if (call != null) {
                call!!.registerCallback(callback)
            }
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
                if(hold) call!!.hold()
                else call!!.unhold()
            }
        }

        fun getCallContact(context: Context, callback: (CallContact?) -> Unit) {
            val callContact = CallContact("", "", "")
            if (call == null || call!!.details == null || call!!.details!!.handle == null) {
                callback(callContact)
                return
            }

            val uri = Uri.decode(call!!.details.handle.toString())
            if (uri.startsWith("tel:")) {
                val number = uri.substringAfter("tel:")
                callContact.number = number
                callContact.name = Utilities().getNameFromPhoneNumber(context, number).toString()
                callContact.photoUri = Utilities().getPhotoUriFromPhoneNumber(context, number)

                if (callContact.name != callContact.number) {
                    callback(callContact)
                }
            }
        }

    }
}