package com.asinosoft.cdm.activities

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.PhoneAccountHandle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.databinding.ActivityOngoingCallBinding
import com.asinosoft.cdm.dialer.CallManager
import com.asinosoft.cdm.dialer.addCharacter
import com.asinosoft.cdm.dialer.getCallStateText
import com.asinosoft.cdm.dialer.getFormattedDuration
import com.asinosoft.cdm.helpers.*
import java.util.*

/**
 * Активность текущего звонка
 */
class OngoingCallActivity : BaseActivity() {
    private lateinit var v: ActivityOngoingCallBinding
    private lateinit var call: Call

    // bools
    private var isCallEnded = false
    private var callDuration = 0
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var callTimer = Timer()

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d("CDM|Call", "onStateChanged → $state")
            updateCallState(state)
            updateSimSlotInfo(call.details.accountHandle)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CDM|Call", "onCreate")
        super.onCreate(savedInstanceState)

        call = CallManager.getCall() ?: return finish()

        v = ActivityOngoingCallBinding.inflate(layoutInflater)
        setContentView(v.root)

        call.registerCallback(callCallback)
        setCallerInfo(call.details.handle.schemeSpecificPart)
        updateCallState(call.state)

        clickToButtons()
        addLockScreenFlags()
        initProximitySensor()

        audioManager.mode = AudioManager.MODE_IN_CALL
        // Detect a nav bar and adapt layout accordingly
        if (hasNavBar()) {
            v.frame.setPadding(0, 0, 0, navBarHeight())
        }
    }

    private fun setCallerInfo(phone: String) {
        val contact = ContactRepositoryImpl(this).getContactByPhone(phone)

        val photo =
            if (null == contact) loadResourceAsBitmap(R.drawable.ic_default_photo)
            else loadUriAsBitmap(contact.photoUri)

        v.ongoingCallLayout.textCaller.text = contact?.name ?: phone
        v.ongoingCallLayout.textCallerNumber.text = phone
        v.ongoingCallLayout.imagePlaceholder.setImageBitmap(photo)
    }

    private fun addLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        if (Build.VERSION.SDK_INT >= 26) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun initProximitySensor() {
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "com.asinosoft.cdm:wake_lock"
            )
            proximityWakeLock?.acquire(600 * 1000L) //
        }
    }

    override fun onBackPressed() {
        Log.d("CDM|Call", "onBackPressed")
        // In case the dialpad is opened, pressing the back button will close it
        if (v.keyboardWrapper.isVisible) {
            v.keyboardWrapper.visibility = View.GONE
            switchToCallingUI()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        Log.d("CDM|Call", "Destroy")
        super.onDestroy()
        callTimer.cancel()
        if (true == proximityWakeLock?.isHeld()) {
            proximityWakeLock?.release()
        }

        endCall()
    }

    private fun activateCall() {
        Log.d("CDM|Call", "activateCall")
        CallManager.accept()
        switchToCallingUI()
    }

    private fun endCall() {
        Log.d("CDM|Call", "endCall")
        if (isCallEnded) {
            finish()
            return
        }

        try {
            call.unregisterCallback(callCallback)
            CallManager.reject()

            if (true == proximityWakeLock?.isHeld()) {
                proximityWakeLock?.release()
            }

            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (ignored: Exception) {
        }

        isCallEnded = true
        if (callDuration > 0) {
            runOnUiThread {
                v.ongoingCallLayout.textStatus.text = callDuration.getFormattedDuration()
                Handler().postDelayed(
                    { finish() },
                    3000
                )
            }
        } else {
            v.ongoingCallLayout.textStatus.text = getString(R.string.status_call_disconnected)
            finish()
        }
    }

    private fun getCallTimerUpdateTask() = object : TimerTask() {
        override fun run() {
            callDuration++
            runOnUiThread {
                if (!isCallEnded) {
                    v.ongoingCallLayout.textStatus.text = callDuration.getFormattedDuration()
                }
            }
        }
    }

    private fun callStarted() {
        try {
            callTimer.scheduleAtFixedRate(getCallTimerUpdateTask(), 1000, 1000)
        } catch (ignored: Exception) {
        }
    }

    private fun switchToCallingUI() {
        v.ongoingCallLayout.textStatus.visibility = View.VISIBLE
        v.ongoingCallLayout.textCaller.visibility = View.VISIBLE
        v.ongoingCallLayout.answerBtn.visibility = View.GONE
        v.ongoingCallLayout.rejectBtn.visibility = View.GONE
        v.ongoingCallLayout.disconnect.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.VISIBLE
    }

    private fun visibilityIncomingCall() {
        v.ongoingCallLayout.answerBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.INVISIBLE
    }

    private fun viewKeyboard() {
        v.ongoingCallLayout.textCallerNumber.visibility = View.GONE
        v.ongoingCallLayout.textStatus.visibility = View.GONE
        v.ongoingCallLayout.textCaller.visibility = View.GONE
        v.ongoingCallLayout.disconnect.visibility - View.GONE
        v.ongoingCallLayout.answerBtn.visibility = View.GONE
        v.ongoingCallLayout.rejectBtn.visibility = View.GONE
        v.ongoingCallLayout.buttonHold.visibility = View.GONE
        v.ongoingCallLayout.buttonMute.visibility = View.GONE
        v.ongoingCallLayout.buttonKeypad.visibility = View.GONE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.GONE
    }

    private fun toggleMicrophone() {
        Log.d("CDM|Call", "toggleMicrophone")
        v.ongoingCallLayout.buttonMute.isActivated = !v.ongoingCallLayout.buttonMute.isActivated
        audioManager.isMicrophoneMute = v.ongoingCallLayout.buttonMute.isActivated
        val microphoneIcon =
            if (v.ongoingCallLayout.buttonMute.isActivated) R.drawable.ic_mic_off_black_24dp else R.drawable.ic_mic_black_24dp
        v.ongoingCallLayout.buttonMute.setImageResource(microphoneIcon)
        callService.setMuted(v.ongoingCallLayout.buttonMute.isActivated)
    }

    private fun toggleSpeaker() {
        Log.d("CDM|Call", "toggleSpeaker")
        v.ongoingCallLayout.buttonSpeaker.isActivated =
            !v.ongoingCallLayout.buttonSpeaker.isActivated
        audioManager.isSpeakerphoneOn = v.ongoingCallLayout.buttonSpeaker.isActivated
        val speakerIcon =
            if (v.ongoingCallLayout.buttonSpeaker.isActivated) R.drawable.ic_volume_off else R.drawable.ic_volume_on
        v.ongoingCallLayout.buttonSpeaker.setImageResource(speakerIcon)
        val newRoute =
            if (v.ongoingCallLayout.buttonSpeaker.isActivated) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        callService.setAudioRoute(newRoute)
    }

    private fun toggleHold() {
        Log.d("CDM|Call", "toggleHold")
        v.ongoingCallLayout.buttonHold.isActivated = !v.ongoingCallLayout.buttonHold.isActivated
        CallManager.hold(v.ongoingCallLayout.buttonHold.isActivated)
    }

    @SuppressLint("ResourceAsColor")
    private fun clickToButtons() {

        v.ongoingCallLayout.answerBtn.setOnClickListener {
            activateCall()
        }
        v.ongoingCallLayout.disconnect.setOnClickListener {
            endCall()
        }
        v.ongoingCallLayout.rejectBtn.setOnClickListener {
            endCall()
        }

        v.ongoingCallLayout.buttonSpeaker.setOnClickListener {
            toggleSpeaker()
        }

        v.ongoingCallLayout.buttonHold.setOnClickListener {
            toggleHold()
        }

        v.ongoingCallLayout.buttonMute.setOnClickListener {
            toggleMicrophone()
        }

        v.ongoingCallLayout.buttonKeypad.setOnClickListener {
            if (v.keyboardWrapper.isVisible) {
                v.keyboardWrapper.visibility = View.GONE
                switchToCallingUI()
            } else {
                v.keyboardWrapper.visibility = View.VISIBLE
                viewKeyboard()
            }
        }

        v.keyboard.ripple0.setOnClickListener { dialpadPressed('0') }
        v.keyboard.oneBtn.setOnClickListener { dialpadPressed('1') }
        v.keyboard.twoBtn.setOnClickListener { dialpadPressed('2') }
        v.keyboard.threeBtn.setOnClickListener { dialpadPressed('3') }
        v.keyboard.fourBtn.setOnClickListener { dialpadPressed('4') }
        v.keyboard.fiveBtn.setOnClickListener { dialpadPressed('5') }
        v.keyboard.sixBtn.setOnClickListener { dialpadPressed('6') }
        v.keyboard.sevenBtn.setOnClickListener { dialpadPressed('7') }
        v.keyboard.eightBtn.setOnClickListener { dialpadPressed('8') }
        v.keyboard.nineBtn.setOnClickListener { dialpadPressed('9') }

        v.keyboard.ripple0.setOnLongClickListener { dialpadPressed('+'); true }

        v.keyboardWrapper.setBackgroundColor(R.color.white)
    }

    private fun dialpadPressed(char: Char) {
        CallManager.keypad(char)
        v.keyboard.inputText.addCharacter(char)
    }

    private fun updateCallState(callState: Int) {
        Log.d("CDM|Call", "updateCallState → $callState")
        when (callState) {
            Call.STATE_RINGING -> visibilityIncomingCall()
            Call.STATE_ACTIVE -> {
                callStarted()
                switchToCallingUI()
            }
            Call.STATE_DISCONNECTED -> endCall()
            Call.STATE_CONNECTING, Call.STATE_DIALING -> switchToCallingUI()
        }

        if (callState == Call.STATE_DISCONNECTED || callState == Call.STATE_DISCONNECTING) {
            callTimer.cancel()
        }

        v.ongoingCallLayout.textStatus.text = getCallStateText(callState)
    }

    private fun updateSimSlotInfo(handle: PhoneAccountHandle) {
        val simSlot = getSimSlot(handle)
        val simIcon = when (simSlot?.id) {
            1 -> R.drawable.sim1
            2 -> R.drawable.sim2
            else -> R.drawable.sim3
        }

        v.ongoingCallLayout.textStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
            simIcon,
            0,
            0,
            0
        )
    }
}
