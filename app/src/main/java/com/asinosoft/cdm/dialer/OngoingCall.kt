package com.asinosoft.cdm.dialer

import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import android.telephony.SubscriptionManager
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.net.URLDecoder

object OngoingCall {

    // Variables
    private const val sIsAutoCalling = false
    private const val sAutoCallPosition = 0
    private val mSubscriptionManager: SubscriptionManager? = null

    val state: BehaviorSubject<Int> = BehaviorSubject.create()

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

    fun registerCallback() {
        if (call == null)
            return
        call!!.registerCallback(callback)
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

    fun getState(): Int {
        return if (call == null){
            Call.STATE_DISCONNECTED
        } else {
            call!!.getState()
        } // if no call, return disconnected
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

    fun addCall(call: Call){
        if(call != null){
            call.conference(call)
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