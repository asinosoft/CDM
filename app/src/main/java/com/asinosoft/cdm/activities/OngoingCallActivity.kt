package com.asinosoft.cdm.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.PhoneAccountHandle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.databinding.ActivityOngoingCallBinding
import com.asinosoft.cdm.dialer.CallService
import com.asinosoft.cdm.dialer.addCharacter
import com.asinosoft.cdm.dialer.getCallStateText
import com.asinosoft.cdm.dialer.getFormattedDuration
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
            Intent(context, OngoingCallActivity::class.java)
                .setData(call.details.handle)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private var callStartTime: Long = 0L
    private var callTimer: Timer? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Timber.d("Call # %s | state → %s", call.details.handle, getCallStateText(state))

            updateCallState(state)
            updateSimSlotInfo(call.details.accountHandle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate # %s", intent.data)
        super.onCreate(savedInstanceState)

        v = ActivityOngoingCallBinding.inflate(layoutInflater)
        setContentView(v.root)

        initEventListeners()
        initLockScreenFlags()

        audioManager.mode = AudioManager.MODE_IN_CALL
        // Detect a nav bar and adapt layout accordingly
        if (hasNavBar()) {
            v.frame.setPadding(0, 0, 0, navBarHeight())
        }
    }

    override fun onResume() {
        Timber.d("onResume # %s", intent.data)
        super.onResume()
        acquireProximitySensor()

        CallService.instance?.getCall(intent?.data)?.let {
            setCurrentCall(it)
        } ?: finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent # %s", intent?.data?.schemeSpecificPart)
        setIntent(intent)
    }

    override fun onBackPressed() {
        Timber.d("onBackPressed")
        // In case the dialpad is opened, pressing the back button will close it
        if (v.keyboardWrapper.isVisible) {
            toggleKeyboard()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        releaseProximitySensor()
        call?.unregisterCallback(callCallback)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        callTimer?.cancel()
        super.onDestroy()
    }

    private fun setCurrentCall(call: Call) {
        this.call = call
        call.registerCallback(callCallback)
        setCallerInfo(call.details.handle.schemeSpecificPart)
        updateCallState(call.callState)
        updateSimSlotInfo(call.details.accountHandle)

        if (Call.STATE_HOLDING == call.callState) {
            call.unhold()
        }
    }

    private fun setCallerInfo(phone: String) {
        val contact = ContactRepositoryImpl(this).getContactByPhone(phone)

        val photo: Drawable? =
            contact?.getAvatar(this) ?: ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_default_photo,
                null
            )

        v.ongoingCallLayout.textCaller.text = contact?.name ?: phone
        v.ongoingCallLayout.textCallerNumber.text = phone
        v.ongoingCallLayout.imagePlaceholder.setImageDrawable(photo)
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
    }

    private fun initLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        if (Build.VERSION.SDK_INT >= 26) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun acquireProximitySensor() {
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "com.asinosoft.cdm:wake_lock"
            )
            proximityWakeLock?.acquire(600 * 1000L) //
        }
    }

    private fun releaseProximitySensor() {
        if (true == proximityWakeLock?.isHeld) {
            proximityWakeLock?.release()
        }
    }

    private fun initTimer() {
        callStartTime = call?.details?.connectTimeMillis ?: Date().time
        try {
            callTimer?.cancel()
            callTimer = Timer().apply {
                scheduleAtFixedRate(getCallTimerUpdateTask(), 1000, 1000)
            }
        } catch (ignored: Exception) {
            Timber.e(ignored)
        }
    }

    private fun activateCall() {
        Timber.d("activateCall # %s", call?.phone)
        call?.accept()
    }

    private fun endCall() {
        Timber.d("endCall # %s", call?.phone)
        call?.reject()
    }

    private fun onCallEnded() {
        Timber.d("onCallEnded # %s", call?.phone)
        callTimer?.cancel()

        CallService.instance?.getNextCall()?.let {
            setCurrentCall(it)
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

        if (Date().time - callStartTime > ONE_SECOND) {
            runOnUiThread {
                v.ongoingCallLayout.textStatus.text =
                    (Date().time - callStartTime).getFormattedDuration()
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
                if (Call.STATE_ACTIVE == call?.callState) {
                    v.ongoingCallLayout.textStatus.text =
                        (Date().time - callStartTime).getFormattedDuration()
                }
            }
        }
    }

    /**
     * Интерфейс в режиме ожидания соединения
     */
    private fun switchToCallingUI() {
        v.keyboardWrapper.visibility = View.GONE
        v.ongoingCallLayout.answerBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonHold.off()
        v.ongoingCallLayout.buttonMute.on()
        v.ongoingCallLayout.buttonKeypad.off()
        v.ongoingCallLayout.buttonSpeaker.on()
    }

    /**
     * Интерфейс в режиме входящего звонка
     */
    private fun switchToRingingUI() {
        v.keyboardWrapper.visibility = View.GONE
        v.ongoingCallLayout.answerBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.INVISIBLE
    }

    /**
     * Интерфейс в режиме активного/удерживаемого звонка
     */
    private fun switchToActiveUI() {
        v.keyboardWrapper.visibility = View.GONE
        v.ongoingCallLayout.answerBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.INVISIBLE
        v.ongoingCallLayout.disconnect.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonHold.on()
        v.ongoingCallLayout.buttonMute.on()
        v.ongoingCallLayout.buttonKeypad.on()
        v.ongoingCallLayout.buttonSpeaker.on()
    }

    /**
     * Интерфейс в режиме клавиатуры
     */
    private fun switchToKeyboard() {
        v.keyboardWrapper.visibility = View.VISIBLE
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
            switchToActiveUI()
        } else {
            switchToKeyboard()
        }
    }

    private fun toggleMicrophone() {
        Timber.d("toggleMicrophone # %s", call?.phone)
        v.ongoingCallLayout.buttonMute.isActivated = !v.ongoingCallLayout.buttonMute.isActivated
        audioManager.isMicrophoneMute = v.ongoingCallLayout.buttonMute.isActivated
        val microphoneIcon =
            if (v.ongoingCallLayout.buttonMute.isActivated) R.drawable.ic_mic_off_black_24dp else R.drawable.ic_mic_black_24dp
        v.ongoingCallLayout.buttonMute.setImageResource(microphoneIcon)
        CallService.instance?.setMuted(v.ongoingCallLayout.buttonMute.isActivated)
    }

    private fun toggleSpeaker() {
        Timber.d("toggleSpeaker # %s", call?.phone)
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
        Timber.d("toggleHold # %s", call?.phone)
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
            Call.STATE_NEW,
            Call.STATE_DIALING,
            Call.STATE_CONNECTING ->
                switchToCallingUI()

            Call.STATE_RINGING ->
                switchToRingingUI()

            Call.STATE_HOLDING,
            Call.STATE_ACTIVE -> {
                initTimer()
                switchToActiveUI()
            }

            Call.STATE_DISCONNECTING,
            Call.STATE_DISCONNECTED ->
                onCallEnded()
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

    private fun ImageView.off() {
        visibility = View.VISIBLE
        isClickable = false
        imageAlpha = 64
    }

    private fun ImageView.on() {
        visibility = View.VISIBLE
        isClickable = true
        imageAlpha = 255
    }
}
