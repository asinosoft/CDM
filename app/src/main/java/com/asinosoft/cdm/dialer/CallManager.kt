package com.asinosoft.cdm.dialer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.helpers.getAvailableSimSlots
import com.asinosoft.cdm.helpers.loadResourceAsBitmap
import com.asinosoft.cdm.helpers.loadUriAsBitmap

class CallManager {

    companion object {
        private var call: Call? = null

        private var phoneNumber: String? = null
        private var callerName: String? = null
        private var callerPhoto: Bitmap? = null
        private var operatorName: String? = null
        private var simSlotIcon: Int? = null

        fun getCall() = call

        fun setCall(context: Context, call: Call) {
            Log.d("CDM|CallManager::setCall", "→ ${call.details.handle} | ${call.state}")

            this.call = call

            val phone = Uri.decode(call.details.handle.toString()).substringAfter("tel:")
            val contact = ContactRepositoryImpl(context).getContactByPhone(phone)

            phoneNumber = phone

            callerName = contact?.name ?: phoneNumber

            callerPhoto =
                if (null == contact) context.loadResourceAsBitmap(R.drawable.ic_default_photo)
                else context.loadUriAsBitmap(contact.photoUri)

            context.getAvailableSimSlots()
                .find { slot -> call.details.accountHandle == slot.handle }
                ?.let { slot ->
                    operatorName = slot.operator
                    simSlotIcon = when (slot.id) {
                        1 -> R.drawable.sim1
                        2 -> R.drawable.sim2
                        else -> R.drawable.sim3
                    }
                }
        }

        fun resetCall() {
            Log.d("CDM|CallManager::resetCall", "")
            call = null
        }

        fun accept() {
            Log.d("CDM|CallManager::accept", "")
            call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }

        fun reject() {
            Log.d("CDM|CallManager::reject", "")
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

        fun getPhoneNumber() = phoneNumber

        fun getCallerName() = callerName

        fun getCallerPhoto() = callerPhoto

        fun getOperatorName() = operatorName

        fun getSimSlotIcon() = simSlotIcon
    }
}
