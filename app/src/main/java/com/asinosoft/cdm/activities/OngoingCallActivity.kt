package com.asinosoft.cdm.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.PhoneAccountHandle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.databinding.ActivityOngoingCallBinding
import com.asinosoft.cdm.dialer.*
import com.asinosoft.cdm.helpers.*
import timber.log.Timber
import java.util.*

/**
 * Активность текущего звонка
 */
class OngoingCallActivity : BaseActivity() {
    private lateinit var v: ActivityOngoingCallBinding
    private var call: Call? = null

    companion object {
        private const val ONE_SECOND = 1000

        fun intent(context: Context, call: Call) =
            Intent(context, OngoingCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("call_id", call.id())
            }
    }

    // bools
    private var isCallEnded = false
    private var callStartTime = Date()
    private var callTimer = Timer()
    private var proximityWakeLock: PowerManager.WakeLock? = null

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Timber.d("Call # %d | state → %s", call.id(), getCallStateText(state))

            updateCallState(state)
            updateSimSlotInfo(call.details.accountHandle)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callId = intent.getIntExtra("call_id", 0)
        Timber.d("onCreate # %d", callId)
        call = CallService.instance?.getCallById(callId)

        v = ActivityOngoingCallBinding.inflate(layoutInflater)
        setContentView(v.root)

        initEventListeners()
        initLockScreenFlags()
        initProximitySensor()

        audioManager.mode = AudioManager.MODE_IN_CALL
        // Detect a nav bar and adapt layout accordingly
        if (hasNavBar()) {
            v.frame.setPadding(0, 0, 0, navBarHeight())
        }
    }

    override fun onResume() {
        Timber.d("onResume # %d", call?.id())

        (application as App).notification.setAppActive(true)

        call?.let {
            it.registerCallback(callCallback)
            setCallerInfo(it.details.handle.schemeSpecificPart)
            updateCallState(it.getCallState())
            updateSimSlotInfo(it.details.accountHandle)
        }
        super.onResume()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent # %d", intent?.getIntExtra("call_id", 0))
        setIntent(intent)
    }

    override fun onBackPressed() {
        Timber.d("onBackPressed")
        // In case the dialpad is opened, pressing the back button will close it
        if (v.keyboardWrapper.isVisible) {
            v.keyboardWrapper.visibility = View.GONE
            switchToCallingUI()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        (application as App).notification.setAppActive(false)
        call?.unregisterCallback(callCallback)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        callTimer.cancel()
        if (true == proximityWakeLock?.isHeld()) {
            proximityWakeLock?.release()
        }

        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        call?.let { outState.putInt("call_id", it.id()) }
        super.onSaveInstanceState(outState)
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

    @SuppressLint("ResourceAsColor")
    private fun initEventListeners() {

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
            toggleKeyboard()
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

        v.keyboard.imageClear.setOnClickListener { toggleKeyboard() }
        v.keyboard.btnCall.setOnClickListener { endCall() }

        v.keyboard.ripple0.setOnLongClickListener { dialpadPressed('+'); true }

        v.keyboardWrapper.setBackgroundColor(R.color.white)
    }

    private fun initLockScreenFlags() {
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

    private fun activateCall() {
        Timber.d("activateCall # %d", call?.id())
        call?.accept()
        switchToCallingUI()
    }

    private fun endCall() {
        Timber.d("endCall # %d", call?.id())
        call?.reject()
    }

    private fun onCallStarted() {
        Timber.d("onCallStarted # %d", call?.id())
        callStartTime = Date()
        try {
            callTimer.scheduleAtFixedRate(getCallTimerUpdateTask(), 1000, 1000)
        } catch (ignored: Exception) {
            Timber.e(ignored)
        }
    }

    private fun onCallEnded() {
        Timber.d("onCallEnded # %d", call?.id())
        if (isCallEnded) {
            finish()
            return
        }

        try {
            if (true == proximityWakeLock?.isHeld()) {
                proximityWakeLock?.release()
            }

            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (ignored: Exception) {
            Timber.e(ignored)
        }

        isCallEnded = true
        if (Date().time - callStartTime.time > ONE_SECOND) {
            runOnUiThread {
                v.ongoingCallLayout.textStatus.text =
                    (Date().time - callStartTime.time).getFormattedDuration()
                Handler().postDelayed(
                    { finish() },
                    1000
                )
            }
        } else {
            v.ongoingCallLayout.textStatus.text = getString(R.string.status_call_disconnected)
            finish()
        }
    }

    private fun getCallTimerUpdateTask() = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                if (!isCallEnded) {
                    v.ongoingCallLayout.textStatus.text =
                        (Date().time - callStartTime.time).getFormattedDuration()
                }
            }
        }
    }

    private fun switchToCallingUI() {
        v.ongoingCallLayout.answerBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.VISIBLE
    }

    private fun switchToRingingUI() {
        v.ongoingCallLayout.answerBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.INVISIBLE
    }

    private fun switchToKeyboard() {
        v.ongoingCallLayout.answerBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.INVISIBLE
        v.keyboard.settingsButton.visibility = View.INVISIBLE
        v.keyboard.btnCall.setImageResource(R.drawable.ic_phone_hangup)
    }

    private fun toggleKeyboard() {
        if (v.keyboardWrapper.isVisible) {
            v.keyboardWrapper.visibility = View.GONE
            switchToCallingUI()
        } else {
            v.keyboardWrapper.visibility = View.VISIBLE
            switchToKeyboard()
        }
    }

    private fun toggleMicrophone() {
        Timber.d("toggleMicrophone # %d", call?.id())
        v.ongoingCallLayout.buttonMute.isActivated = !v.ongoingCallLayout.buttonMute.isActivated
        audioManager.isMicrophoneMute = v.ongoingCallLayout.buttonMute.isActivated
        val microphoneIcon =
            if (v.ongoingCallLayout.buttonMute.isActivated) R.drawable.ic_mic_off_black_24dp else R.drawable.ic_mic_black_24dp
        v.ongoingCallLayout.buttonMute.setImageResource(microphoneIcon)
        CallService.instance?.setMuted(v.ongoingCallLayout.buttonMute.isActivated)
    }

    private fun toggleSpeaker() {
        Timber.d("toggleSpeaker # %d", call?.id())
        v.ongoingCallLayout.buttonSpeaker.isActivated =
            !v.ongoingCallLayout.buttonSpeaker.isActivated
        audioManager.isSpeakerphoneOn = v.ongoingCallLayout.buttonSpeaker.isActivated
        val speakerIcon =
            if (v.ongoingCallLayout.buttonSpeaker.isActivated) R.drawable.ic_volume_off else R.drawable.ic_volume_on
        v.ongoingCallLayout.buttonSpeaker.setImageResource(speakerIcon)
        val newRoute =
            if (v.ongoingCallLayout.buttonSpeaker.isActivated) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        CallService.instance?.setAudioRoute(newRoute)
    }

    private fun toggleHold() {
        Timber.d("toggleHold # %d", call?.id())
        if (v.ongoingCallLayout.buttonHold.isActivated) {
            v.ongoingCallLayout.buttonHold.isActivated = false
            call?.hold()
        } else {
            v.ongoingCallLayout.buttonHold.isActivated = true
            call?.unhold()
        }
    }

    private fun dialpadPressed(char: Char) {
        call?.playDtmfTone(char)
        call?.stopDtmfTone()
        v.keyboard.inputText.addCharacter(char)
    }

    private fun updateCallState(callState: Int) {
        Timber.d("updateCallState → %s", getCallStateText(callState))
        when (callState) {
            Call.STATE_RINGING -> switchToRingingUI()
            Call.STATE_ACTIVE -> {
                onCallStarted()
                switchToCallingUI()
            }
            Call.STATE_DISCONNECTED -> onCallEnded()
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
