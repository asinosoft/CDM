package com.asinosoft.cdm.viewmodels

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.util.Log
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.helpers.getAvailableSimSlots

class CallViewModel : ViewModel() {
    private var callState: Int = Call.STATE_DISCONNECTED
    private var callerName: String = "Unknown"
    private var callerNumber: String = ""
    private var callerPhoto: Uri =
        Uri.parse("android.resource://com.asinosoft.cdm/drawable/${R.drawable.ic_default_photo}")
    private var operatorName: String = "Unknown"
    private var simSlotIcon: Int = R.drawable.ic_sim

    fun setContactUri(context: Context, contactUri: Uri?) {
        Log.d("CDM|call", "url = $contactUri")
        callerNumber = Uri.decode(contactUri.toString()).substringAfter("tel:")
        Log.d("CDM|call", "number = $callerNumber")
        ContactRepositoryImpl(context).getContactByPhone(callerNumber)?.let { contact ->
            callerName = contact.name
            callerPhoto = contact.photoUri
            Log.d("CDM|call", "contact = $callerName, photo = $callerPhoto")
        }
    }

    fun setCall(context: Context, call: Call) {
        setContactUri(context, call.details.handle)
        context.getAvailableSimSlots().find { slot -> call.details.accountHandle == slot.handle }
            ?.let { slot ->
                operatorName = slot.operator
                simSlotIcon = when (slot.id) {
                    1 -> R.drawable.ic_sim1
                    2 -> R.drawable.ic_sim2
                    3 -> R.drawable.ic_sim3
                    else -> R.drawable.ic_sim
                }
                Log.d("CDM|call", "sim = ${slot.id}, operator = ${slot.operator}")
            }
    }

    fun getCallState() = callState

    fun getCallStateName(context: Context): CharSequence = when (callState) {
        Call.STATE_RINGING -> context.getText(R.string.state_call_ringing)
        Call.STATE_DIALING -> context.getText(R.string.status_call_dialing)
        Call.STATE_ACTIVE -> context.getText(R.string.status_call_active)
        Call.STATE_HOLDING -> context.getText(R.string.status_call_holding)
        else -> context.getText(R.string.status_call_disconnected)
    }

    fun getCallerName() = callerName

    fun getCallerNumber() = callerNumber

    fun getCallerPhoto() = callerPhoto

    fun getOperatorName() = operatorName

    fun getSimSlotIcon() = simSlotIcon
}
