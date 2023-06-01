package com.asinosoft.cdm.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.telecom.Call
import android.telecom.Call.*
import android.telecom.CallAudioState
import android.telecom.PhoneAccountHandle
import android.view.*
import android.widget.ImageView
import androidx.core.view.isVisible
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.databinding.ActivityOngoingCallBinding
import com.asinosoft.cdm.dialer.CallService
import com.asinosoft.cdm.dialer.getCallStateText
import com.asinosoft.cdm.dialer.getFormattedDuration
import com.asinosoft.cdm.helpers.*
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import timber.log.Timber
import java.util.*
import kotlin.math.absoluteValue

/**
 * Активность текущего звонка
 */
class OngoingCallActivity : BaseActivity() {
    private lateinit var v: ActivityOngoingCallBinding
    private var call: Call? = null
    private var showIncomingAnimation = false

    private var handleAnimation: ViewPropertyAnimator? = null
    private var arrowUpAnimation: ViewPropertyAnimator? = null
    private var arrowDownAnimation: ViewPropertyAnimator? = null

    // Интерфейс входящего звонка
    private var touchPosition = 0f
    private var maxHandleDistance = 0f
    private var thresholdDistance = 0f
    private var isHandleDragged = false
    private var velocityTracker: VelocityTracker? = null

    companion object {
        private const val ONE_SECOND = 1000

        fun intent(context: Context, call: Call) =
            Intent(context, OngoingCallActivity::class.java)
                .setData(call.details.handle)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    }

    private var callStartTime: Long = 0L
    private var callTimer: Timer? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Timber.d("Call # %s | state → %s", call.details.handle, getCallStateText(state))

            when (state) {
                STATE_DISCONNECTED, STATE_ACTIVE, STATE_HOLDING -> vibrator.vibrateSafety(
                    Keys.VIBRO,
                    255
                )
                else -> {}
            }
            updateCallState(state)
            updateSimSlotInfo(call.details.accountHandle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate # %s", intent.data)

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

        CallService.instance?.getCall(intent?.data)?.let {
            setCurrentCall(it)
        } ?: finishAndRemoveTask()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent # %s", intent?.data?.schemeSpecificPart)
        setIntent(intent)
    }

    override fun onBackPressed() {
        Timber.d("onBackPressed")
        // In case the dialpad is opened, pressing the back button will close it
        if (v.keyboard.root.isVisible) {
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
        Timber.d("setCurrentCall %s", call.phone)
        this.call = call
        call.registerCallback(callCallback)
        setCallerInfo(call.details.handle.schemeSpecificPart)
        updateCallState(call.callState)
        updateSimSlotInfo(call.details.accountHandle)

        if (STATE_HOLDING == call.callState) {
            call.unhold()
        }
    }

    private fun setCallerInfo(phone: String) {
        val contact = ContactRepositoryImpl(this).getContactByPhone(phone)

        if (0L == contact.id) {
            v.info.textCaller.text = phone
            v.info.textCallerNumber.text = null
        } else {
            v.info.textCaller.text = contact.name
            v.info.textCallerNumber.text = phone
        }

        val photo = contact.getPhoto(this)
        if (null == photo) {
            v.info.avatar.setImageResource(R.drawable.ic_user_circle)
            v.incoming.handle.setImageResource(R.drawable.ic_user_circle)
            val color = ColorStateList.valueOf(AvatarHelper.getBackgroundColor(phone))
            v.info.avatar.imageTintList = color
            v.incoming.handle.imageTintList = color
        } else {
            v.info.avatar.setImageDrawable(photo)
            v.incoming.handle.setImageDrawable(photo)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventListeners() {

        v.ongoing.disconnect.setOnClickListener {
            call?.reject()
        }

        v.ongoing.buttonSpeaker.setOnClickListener {
            toggleSpeaker()
        }

        v.ongoing.buttonHold.setOnClickListener {
            toggleHold()
        }

        v.ongoing.buttonMute.setOnClickListener {
            toggleMicrophone()
        }

        v.ongoing.buttonKeypad.setOnClickListener {
            toggleKeyboard()
        }

        v.keyboard.star.setOnClickListener { dialpadPressed('*') }
        v.keyboard.star.setOnLongClickListener { dialpadPressed('#'); true }

        v.keyboard.zeroBtn.setOnClickListener { dialpadPressed('0') }
        v.keyboard.oneBtn.setOnClickListener { dialpadPressed('1') }
        v.keyboard.twoBtn.setOnClickListener { dialpadPressed('2') }
        v.keyboard.threeBtn.setOnClickListener { dialpadPressed('3') }
        v.keyboard.fourBtn.setOnClickListener { dialpadPressed('4') }
        v.keyboard.fiveBtn.setOnClickListener { dialpadPressed('5') }
        v.keyboard.sixBtn.setOnClickListener { dialpadPressed('6') }
        v.keyboard.sevenBtn.setOnClickListener { dialpadPressed('7') }
        v.keyboard.eightBtn.setOnClickListener { dialpadPressed('8') }
        v.keyboard.nineBtn.setOnClickListener { dialpadPressed('9') }

        v.keyboard.close.setOnClickListener { toggleKeyboard() }
        v.keyboard.hangup.setOnClickListener { endCall() }

        v.incoming.root.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> startDrag(e)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> endDrag(e)
                MotionEvent.ACTION_MOVE -> drag(e)
                else -> false
            }
        }
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
            if (null == proximityWakeLock) {
                proximityWakeLock = powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                    "com.asinosoft.cdm:wake_lock"
                )
            }
            if (false == proximityWakeLock?.isHeld) {
                proximityWakeLock?.acquire(/* 1 hour */ 60 * 60 * 1000L)
            }
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

    private fun acceptCall() {
        Timber.d("activateCall # %s", call?.phone)
        switchToCallingUI()
        call?.accept()
    }

    private fun endCall() {
        Timber.d("endCall # %s", call?.phone)
        v.incoming.root.visibility = View.GONE
        showIncomingAnimation = false
        call?.reject()
    }

    private fun onCallEnded() {
        Timber.d("onCallEnded # %s", call?.phone)

        CallService.instance?.getNextCall()?.let {
            setCurrentCall(it)
            initTimer()
            return
        }

        releaseProximitySensor()
        callTimer?.cancel()

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (ignored: Exception) {
            Timber.e(ignored)
        }

        if (Date().time - callStartTime > ONE_SECOND) {
            runOnUiThread {
                v.info.textStatus.text =
                    (Date().time - callStartTime).getFormattedDuration()
                Handler().postDelayed(
                    { finishAndRemoveTask() },
                    1000
                )
            }
        } else {
            v.info.textStatus.text = getString(R.string.status_call_disconnected)
            finishAndRemoveTask()
        }
    }

    private fun getCallTimerUpdateTask() = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                if (STATE_ACTIVE == call?.callState) {
                    v.info.textStatus.text =
                        (Date().time - callStartTime).getFormattedDuration()
                }
            }
        }
    }

    /**
     * Интерфейс в режиме ожидания соединения
     */
    private fun switchToCallingUI() {
        showIncomingAnimation = false
        v.incoming.root.visibility = View.GONE
        v.keyboard.root.visibility = View.GONE
        v.ongoing.root.visibility = View.VISIBLE
        v.ongoing.disconnect.visibility = View.VISIBLE
        v.info.avatar.visibility = View.VISIBLE
        v.ongoing.buttonHold.off()
        v.ongoing.buttonMute.on()
        v.ongoing.buttonKeypad.off()
        v.ongoing.buttonSpeaker.on()
        acquireProximitySensor()
    }

    /**
     * Интерфейс в режиме входящего звонка
     */
    private fun switchToRingingUI() {
        showIncomingAnimation = true
        v.incoming.root.visibility = View.VISIBLE
        v.ongoing.root.visibility = View.GONE
        v.keyboard.root.visibility = View.GONE
        v.info.avatar.visibility = View.GONE
        animateArrowUp()
        animateArrowDown()
        animateHandle()
        releaseProximitySensor()
    }

    /**
     * Интерфейс в режиме активного/удерживаемого звонка
     */
    private fun switchToActiveUI() {
        showIncomingAnimation = false
        v.incoming.root.visibility = View.GONE
        v.keyboard.root.visibility = View.GONE
        v.ongoing.root.visibility = View.VISIBLE
        v.info.avatar.visibility = View.VISIBLE
        v.ongoing.buttonHold.on()
        v.ongoing.buttonMute.on()
        v.ongoing.buttonKeypad.on()
        v.ongoing.buttonSpeaker.on()
        acquireProximitySensor()
    }

    /**
     * Интерфейс в режиме клавиатуры
     */
    private fun switchToKeyboard() {
        v.ongoing.root.visibility = View.INVISIBLE
        v.keyboard.root.visibility = View.VISIBLE
        releaseProximitySensor()
    }

    private fun toggleKeyboard() {
        if (v.keyboard.root.isVisible) {
            switchToActiveUI()
        } else {
            switchToKeyboard()
        }
    }

    private fun toggleMicrophone() {
        Timber.d("toggleMicrophone # %s", call?.phone)
        v.ongoing.buttonMute.isActivated = !v.ongoing.buttonMute.isActivated
        audioManager.isMicrophoneMute = v.ongoing.buttonMute.isActivated
        val microphoneIcon =
            if (v.ongoing.buttonMute.isActivated) R.drawable.ic_mic_off_black_24dp else R.drawable.ic_mic_black_24dp
        v.ongoing.buttonMute.setImageResource(microphoneIcon)
        CallService.instance?.setMuted(v.ongoing.buttonMute.isActivated)
    }

    private fun toggleSpeaker() {
        Timber.d("toggleSpeaker # %s", call?.phone)
        v.ongoing.buttonSpeaker.isActivated =
            !v.ongoing.buttonSpeaker.isActivated
        audioManager.isSpeakerphoneOn = v.ongoing.buttonSpeaker.isActivated
        val speakerIcon =
            if (v.ongoing.buttonSpeaker.isActivated) R.drawable.ic_volume_off else R.drawable.ic_volume_on
        v.ongoing.buttonSpeaker.setImageResource(speakerIcon)
        val newRoute =
            if (v.ongoing.buttonSpeaker.isActivated) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        CallService.instance?.setAudioRoute(newRoute)
    }

    private fun toggleHold() {
        Timber.d("toggleHold # %s", call?.phone)
        if (v.ongoing.buttonHold.isActivated) {
            v.ongoing.buttonHold.isActivated = false
            call?.hold()
        } else {
            v.ongoing.buttonHold.isActivated = true
            call?.unhold()
        }
    }

    private fun dialpadPressed(char: Char) {
        v.keyboard.inputText.text = v.keyboard.inputText.text.toString().plus(char)
        call?.playDtmfTone(char)
        call?.stopDtmfTone()
    }

    private fun updateCallState(callState: Int) {
        Timber.d("updateCallState → %s", getCallStateText(callState))
        when (callState) {
            STATE_NEW,
            STATE_DIALING,
            STATE_CONNECTING ->
                switchToCallingUI()

            STATE_RINGING ->
                switchToRingingUI()

            STATE_HOLDING,
            STATE_ACTIVE -> {
                initTimer()
                switchToActiveUI()
            }

            STATE_DISCONNECTING,
            STATE_DISCONNECTED ->
                onCallEnded()
        }

        v.info.textStatus.text = getCallStateText(callState)
    }

    private fun updateSimSlotInfo(handle: PhoneAccountHandle) {
        val simSlot = getSimSlot(handle)
        val simIcon = when (simSlot?.id) {
            1 -> R.drawable.ic_sim1
            2 -> R.drawable.ic_sim2
            else -> R.drawable.ic_sim3
        }

        v.info.textStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
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

    //region Incoming call handle interface
    private fun startDrag(e: MotionEvent): Boolean {
        maxHandleDistance = (v.incoming.accept.top - v.incoming.handle.top).absoluteValue.toFloat()
        thresholdDistance =
            maxHandleDistance * resources.getInteger(R.integer.incoming_swing_distance_threshold)
                .toFloat() / 100f
        touchPosition = e.rawY
        isHandleDragged = true
        velocityTracker = VelocityTracker.obtain()
        velocityTracker?.addMovement(e)
        handleAnimation?.cancel()

        return true
    }

    private fun endDrag(e: MotionEvent): Boolean {
        if (!isHandleDragged) {
            return false
        }

        velocityTracker?.apply {
            addMovement(e)
            computeCurrentVelocity(100)
        }
        val position = (e.rawY - touchPosition)
        val threshold = resources.getInteger(R.integer.incoming_swing_velocity_threshold).toFloat()

        when {
            (position > thresholdDistance) -> acceptCall()

            (position < -thresholdDistance) -> endCall()

            threshold < (velocityTracker?.yVelocity ?: 0f) -> endCall()

            -threshold > (velocityTracker?.yVelocity ?: 0f) -> acceptCall()

            else -> animateToStart()
        }

        velocityTracker?.recycle()
        velocityTracker = null
        isHandleDragged = false
        return true
    }

    private fun drag(e: MotionEvent): Boolean {
        if (!isHandleDragged) {
            return false
        }

        velocityTracker?.addMovement(e)
        val position = (e.rawY - touchPosition)
            .coerceAtLeast(-maxHandleDistance)
            .coerceAtMost(maxHandleDistance)

        when {
            (position < -thresholdDistance) -> animateToAccept()

            (position > thresholdDistance) -> animateToReject()

            else -> {
                v.incoming.handle.translationY = position

                ((maxHandleDistance - position) / maxHandleDistance).coerceAtLeast(1f).let {
                    v.incoming.accept.scaleX = it
                    v.incoming.accept.scaleY = it
                }

                ((maxHandleDistance + position) / maxHandleDistance).coerceAtLeast(1f).let {
                    v.incoming.reject.scaleX = it
                    v.incoming.reject.scaleY = it
                }
            }
        }

        return true
    }

    private fun animateArrowUp() {
        if (!showIncomingAnimation || isHandleDragged) return

        v.incoming.animatedArrowUp.translationY = 400f

        arrowUpAnimation = v.incoming.animatedArrowUp.animate()
            .translationY(0.0f)
            .setDuration(
                resources.getInteger(R.integer.incoming_arrow_up_animation_duration).toLong()
            )
            .withEndAction { animateArrowUp() }
            .apply { start() }
    }

    private fun animateArrowDown() {
        if (!showIncomingAnimation || isHandleDragged) return

        v.incoming.animatedArrowDown.translationY = -400f

        arrowDownAnimation = v.incoming.animatedArrowDown.animate()
            .translationY(0.0f)
            .setDuration(
                resources.getInteger(R.integer.incoming_arrow_down_animation_duration).toLong()
            )
            .withEndAction { animateArrowDown() }
            .apply { start() }
    }

    private fun animateHandle(
        dy: Float = resources.getInteger(R.integer.incoming_handle_animation_amplitude).toFloat()
    ) {
        if (!showIncomingAnimation || isHandleDragged) return

        handleAnimation = v.incoming.handle.animate()
            .translationY(dy)
            .setDuration(
                resources.getInteger(R.integer.incoming_handle_animation_duration).toLong()
            )
            .withEndAction { animateHandle(-dy) }
            .apply { start() }
    }

    private fun animateToStart() {
        val duration = resources.getInteger(R.integer.incoming_reset_animation_duration).toLong()

        v.incoming.handle.animate()
            .translationY(0f)
            .setDuration(duration)
            .withEndAction {
                animateHandle()
                animateArrowUp()
                animateArrowDown()
            }
            .start()

        v.incoming.accept.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()

        v.incoming.reject.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()
    }

    private fun animateToAccept() {
        isHandleDragged = false
        v.incoming.handle.animate()
            .translationY(-maxHandleDistance)
            .setDuration(
                resources.getInteger(R.integer.incoming_accept_animation_duration).toLong()
            )
            .withEndAction { acceptCall() }
            .start()
    }

    private fun animateToReject() {
        isHandleDragged = false
        v.incoming.handle.animate()
            .translationY(maxHandleDistance)
            .setDuration(
                resources.getInteger(R.integer.incoming_reject_animation_duration).toLong()
            )
            .withEndAction { endCall() }
            .start()
    }
    //endregion
}
