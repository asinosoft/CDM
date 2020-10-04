package com.asinosoft.cdm

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.telecom.Call
import android.telecom.CallAudioState
import android.util.Size
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import com.agik.AGIKSwipeButton.Controller.OnSwipeCompleteListener
import com.agik.AGIKSwipeButton.View.Swipe_Button_View
import com.asinosoft.cdm.detail_contact.Contact
import com.asinosoft.cdm.dialer.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_ongoing_call.*
import kotlinx.android.synthetic.main.keyboard.*
import kotlinx.android.synthetic.main.on_going_call.*
import org.jetbrains.anko.audioManager
import java.util.*
import java.util.concurrent.TimeUnit

class OngoingCallActivity : AppCompatActivity() {

    lateinit var context: Context
    val contactDialer = Contact()

    //bools
    private var isSpeakerOn = false
    private var isMicrophoneOn = true
    private var isCallEnded = false
    private var callDuration = 0
    private var callContact: CallContact? = null
    private var callContactAvatar: Bitmap? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var callTimer = Timer()

    // Finals
    private val CALL_NOTIFICATION_ID = 1
    val MINUTE_SECONDS = 60

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing_call)

        clickToButtons()
        addLockScreenFlags()
        initProximitySensor()

        audioManager.mode = AudioManager.MODE_IN_CALL
        CallManager.getCallContact(applicationContext) { contact ->
            callContact = contact
            callContactAvatar = getCallContactAvatar()
            runOnUiThread {
                setupNotification()
                updateOtherPersonsInfo()
            }
        }

        start.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(swipeView: Swipe_Button_View) {
                activateCall()
            }

            @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
            override fun onSwipe_Reverse(swipeView: Swipe_Button_View) {
            }
        })

        stop.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(swipeView: Swipe_Button_View) {
            }

            @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
            override fun onSwipe_Reverse(swipeView: Swipe_Button_View) {
                endCall()
                start.visibility = View.GONE
                stop.visibility = View.GONE
            }
        })

        CallManager.registerCallback(callCallback)
        updateCallState(CallManager.getState())
    }

    private fun updateOtherPersonsInfo() {
        if (callContact == null) {
            return
        }

        text_status.text =
            if (callContact!!.name.isNotEmpty()) callContact!!.name else getString(R.string.unknown_caller)
        if (callContact!!.name != "") {
            text_caller.text = callContact!!.name
        } else {
            text_caller.text = callContact!!.number
        }

        if (callContactAvatar != null) {
            image_placeholder.setImageBitmap(callContactAvatar)
        }
    }

    @SuppressLint("NewApi")
    private fun addLockScreenFlags() {
        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        if (isOreoPlus()) {
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(
                this,
                null
            )
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun initProximitySensor() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "com.simplemobiletools.dialer.pro:wake_lock"
        )
        proximityWakeLock!!.acquire(10 * MINUTE_SECONDS * 1000L)
    }

    override fun onBackPressed() {
        // In case the dialpad is opened, pressing the back button will close it
        if (keyboard_wrapper.isVisible) {
            keyboard_wrapper.visibility = View.GONE
            swithToCallingUI()
            return
        } else {
            super.onBackPressed()
        }

        if (CallManager.getState() == Call.STATE_DIALING) {
            endCall()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        CallManager.unregisterCallback(callCallback)
        callTimer.cancel()
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock!!.release()
        }

        endCall()
    }


    fun activateCall() {
        CallManager.accept()
        swithToCallingUI()
    }

    fun endCall() {
        CallManager.reject()
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock!!.release()
        }

        if (isCallEnded) {
            finish()
            return
        }

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (ignored: Exception) {
        }

        isCallEnded = true
        if (callDuration > 0) {
            runOnUiThread {
                text_stopwatch.text =
                    "${callDuration.getFormattedDuration()}"
                Handler().postDelayed({
                    finish()
                }, 3000)
            }
        } else {
            text_status.text = getString(R.string.status_call_disconnected)
            finish()
        }
    }

    private fun getCallTimerUpdateTask() = object : TimerTask() {
        override fun run() {
            callDuration++
            runOnUiThread {
                if (!isCallEnded) {
                    text_stopwatch.text = callDuration.getFormattedDuration()
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

    @SuppressLint("RestrictedApi")
    fun swithToCallingUI() {
        // Change the buttons layout
        start.visibility = View.GONE
        stop.visibility = View.GONE
        text_status.visibility = View.VISIBLE
        text_caller.visibility = View.VISIBLE
        answer_btn.visibility = View.GONE
        reject_btn.visibility = View.GONE
        reject_btn2.visibility = View.VISIBLE
        button_hold.visibility = View.VISIBLE
        button_mute.visibility = View.VISIBLE
        button_keypad.visibility = View.VISIBLE
        button_speaker.visibility = View.VISIBLE
    }


    @SuppressLint("RestrictedApi")
    private fun visibilityInomingCall() {
        // Change the buttons layout
        start.visibility = View.VISIBLE
        stop.visibility = View.VISIBLE
        reject_btn2.visibility - View.GONE
        answer_btn.visibility = View.GONE
        reject_btn.visibility = View.GONE
        reject_btn2.visibility = View.INVISIBLE
        button_hold.visibility = View.INVISIBLE
        button_mute.visibility = View.INVISIBLE
        button_keypad.visibility = View.INVISIBLE
        button_speaker.visibility = View.INVISIBLE
    }

    private fun viewKeyboard() {
        start.visibility = View.GONE
        stop.visibility = View.GONE
        text_status.visibility = View.GONE
        text_caller.visibility = View.GONE
        reject_btn2.visibility - View.GONE
        answer_btn.visibility = View.GONE
        reject_btn.visibility = View.GONE
        reject_btn2.visibility = View.GONE
        button_hold.visibility = View.GONE
        button_mute.visibility = View.GONE
        button_keypad.visibility = View.GONE
        button_speaker.visibility = View.GONE
    }

    private fun toggleMicrophone() {
        Utilities().toggleViewActivation(button_mute)
        audioManager.isMicrophoneMute = button_mute.isActivated
        val drawable =
            if (button_mute.isActivated) R.drawable.ic_mic_off_black_24dp else R.drawable.ic_mic_black_24dp
        button_mute.setImageDrawable(getDrawable(drawable))
        CallManager.inCallService?.setMuted(button_mute.isActivated)
    }

    private fun toggleSpeaker() {
        Utilities().toggleViewActivation(button_speaker)
        audioManager.isSpeakerphoneOn = button_speaker.isActivated
        val drawable =
            if (button_speaker.isActivated) R.drawable.ic_baseline_volume_off_24 else R.drawable.outline_volume_up_24
        button_speaker.setImageDrawable(getDrawable(drawable))
        val newRoute =
            if (button_speaker.isActivated) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        CallManager.inCallService?.setAudioRoute(newRoute)
    }

    private fun toggleHold() {
        Utilities().toggleViewActivation(button_hold)
        CallManager.hold(button_hold.isActivated)
    }

    @SuppressLint("ResourceAsColor")
    private fun clickToButtons() {

        answer_btn.setOnClickListener() {
            activateCall()
        }
        reject_btn2.setOnClickListener {
            endCall()
        }
        reject_btn.setOnClickListener {
            endCall()
        }

        button_speaker.setOnClickListener {
            toggleSpeaker()
        }

        button_hold.setOnClickListener {
            toggleHold()
        }

        button_mute.setOnClickListener {
            toggleMicrophone()
        }

        button_keypad.setOnClickListener {
            if (keyboard_wrapper.isVisible) {
                keyboard_wrapper.visibility = View.GONE
                swithToCallingUI()
            } else {
                keyboard_wrapper.visibility = View.VISIBLE
                viewKeyboard()
            }
        }

        ripple0.setOnClickListener { dialpadPressed('0') }
        one_btn.setOnClickListener { dialpadPressed('1') }
        two_btn.setOnClickListener { dialpadPressed('2') }
        three_btn.setOnClickListener { dialpadPressed('3') }
        four_btn.setOnClickListener { dialpadPressed('4') }
        five_btn.setOnClickListener { dialpadPressed('5') }
        six_btn.setOnClickListener { dialpadPressed('6') }
        seven_btn.setOnClickListener { dialpadPressed('7') }
        eight_btn.setOnClickListener { dialpadPressed('8') }
        nine_btn.setOnClickListener { dialpadPressed('9') }

        ripple0.setOnLongClickListener { dialpadPressed('+'); true }

        keyboard_wrapper.setBackgroundColor(R.color.white)
    }

    private fun dialpadPressed(char: Char) {
        CallManager.keypad(char)
        input_text.addCharacter(char)
    }

    private fun setupNotification() {
        val callState = CallManager.getState()
        val channelId = "simple_dialer_channel"
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val name = "call_notification_channel"

            NotificationChannel(channelId, name, importance).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val openAppIntent = Intent(this, OngoingCallActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0)

        val acceptCallIntent = Intent(this, NotificationActionReceiver::class.java)
        acceptCallIntent.action = ACCEPT_CALL
        val acceptPendingIntent =
            PendingIntent.getBroadcast(this, 0, acceptCallIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val declineCallIntent = Intent(this, NotificationActionReceiver::class.java)
        declineCallIntent.action = DECLINE_CALL
        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            declineCallIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val callerName =
            if (callContact != null && callContact!!.name.isNotEmpty()) callContact!!.name else getString(
                R.string.unknown_caller
            )
        val contentTextId = when (callState) {
            Call.STATE_RINGING -> R.string.state_call_ringing
            Call.STATE_DIALING -> R.string.status_call_dialing
            Call.STATE_DISCONNECTED -> R.string.status_call_disconnected
            Call.STATE_DISCONNECTING -> R.string.status_call_disconnected
            else -> R.string.state_call_active
        }

        val collapsedView = RemoteViews(packageName, R.layout.call_notification).apply {
            setText(R.id.notification_caller_name, callerName)
            setText(R.id.notification_call_status, getString(contentTextId))
            setVisibleIf(R.id.notification_accept_call, callState == Call.STATE_RINGING)

            setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
            setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)

            if (contactDialer.photoUri != null) {
                setImageViewBitmap(
                    R.id.notification_thumbnail,
                    getCircularBitmap(callContactAvatar!!)
                )
            }
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_full_144)
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(collapsedView)
            .setOngoing(true)
            .setSound(null)
            .setUsesChronometer(callState == Call.STATE_ACTIVE)
            .setChannelId(channelId)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val notification = builder.build()
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)

    }

    private fun getCallContactAvatar(): Bitmap? {
        var bitmap: Bitmap? = null
        if (callContact?.photoUri?.isNotEmpty() == true) {
            val photoUri = Uri.parse(callContact!!.photoUri)
            try {
                bitmap = if (isQPlus()) {
                    val tmbSize = resources.getDimension(R.dimen.list_avatar_size).toInt()
                    contentResolver.loadThumbnail(photoUri, Size(tmbSize, tmbSize), null)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, photoUri)

                }

                bitmap = getCircularBitmap(bitmap!!)
            } catch (ignored: Exception) {
                return null
            }
        }

        return bitmap
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val radius = bitmap.width / 2.toFloat()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(radius, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    @SuppressLint("NewApi")
    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            updateCallState(state)
        }
    }

    private fun updateCallState(state: Int) {
        when (state) {
            Call.STATE_RINGING -> visibilityInomingCall()
            Call.STATE_ACTIVE -> {
                callStarted()
                swithToCallingUI()
            }
            Call.STATE_DISCONNECTED -> endCall()
            Call.STATE_CONNECTING, Call.STATE_DIALING -> swithToCallingUI()
        }

        if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
            callTimer.cancel()
        }

        val statusTextId = when (state) {
            Call.STATE_RINGING -> R.string.state_call_ringing
            Call.STATE_DIALING -> R.string.status_call_dialing
            Call.STATE_ACTIVE -> R.string.status_call_active
            Call.STATE_HOLDING -> R.string.status_call_holding
            else -> 0
        }

        if (statusTextId != 0) {
            text_status.text = getString(statusTextId)
        }

        setupNotification()
    }
}

