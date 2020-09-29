package com.asinosoft.cdm.dialer

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.detail_contact.Contact
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber


object OngoingCall {

    // Variables
    private const val sIsAutoCalling = false
    val state: BehaviorSubject<Int> = BehaviorSubject.create()
    var inCallService: InCallService? = null
    var number: String? = null
    var contact: Contact? = null

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            Timber.d(call.toString())
            state.onNext(newState)
        }

        override fun onDetailsChanged(call: Call?, details: Call.Details) {
            super.onDetailsChanged(call, details)
            Timber.i("Details changed: %s", details.toString())
        }

    }

    fun registerCallback(callback: Call.Callback) {
        try {
            call!!.registerCallback(callback)
        }catch (e: Exception){

        }
    }

    fun getState() = if(call == null){
        Call.STATE_DISCONNECTED
    }else{
        call!!.state
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state.onNext(it.state)
            }
            field = value
        }

    fun answer() {
        if (call != null) {
            call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
        }
    }

    fun unregisterCallback(callback: Call.Callback){
        try {
            call!!.unregisterCallback(callback)
        }catch (e: Exception){

        }

    }

    fun keypad(c: Char) {
        if (call != null) {
            call!!.playDtmfTone(c)
            call!!.stopDtmfTone()
        }
    }

    fun reject() {
        if(call != null){
            if(call!!.state == Call.STATE_RINGING){
                call!!.reject(false, null)
            }else {
                call!!.disconnect()
            }
        }
    }

    fun addCall(){
        if(call != null){
            call!!.conference(call)
        }
    }

    fun hold(hold: Boolean) {
        if (call != null) {
            if(hold) call!!.hold()
            else call!!.unhold()
        }
    }

    fun isAutoCalling(): Boolean {
        return sIsAutoCalling
    }



}