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
import java.util.concurrent.TimeUnit

class OngoingCallActivity : AppCompatActivity(), OnKeyDownListener {

    private val disposables = CompositeDisposable()
    private var number: String? = null
    lateinit var context: Context
    val contactDialer = Contact()

    //bools
    private var isSpeakerOn = false

    // Finals
    private val END_CALL_MILLIS: Long = 1500
    private val CALL_NOTIFICATION_ID = 1
    val MINUTE_SECONDS = 60

    // Call State
    private var mStateText: String? = null
    private var callContactAvatar: Bitmap? = null

    // Handler variables
    private val TIME_START = 1
    private val TIME_STOP = 0
    private val TIME_UPDATE = 2
    private val REFRESH_RATE = 100

    //Audio Manager
    lateinit var mAudioManager: AudioManager

    // Utilities
    var mCallTimer = Stopwatch()

    lateinit var mIncomingCallSwipeListener: AllPurposeTouchListener

    // Handlers
    var mCallTimeHandler: Handler = CallTimeHandler()
    lateinit var contact: Contact

    // PowerManager
    lateinit var wakeLock: PowerManager.WakeLock

    private var mOnKeyDownListener: OnKeyDownListener? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing_call)

        val uri = Uri.decode(OngoingCall.call!!.details.handle.toString())
        if (uri.startsWith("tel:")) {
            number = uri.substringAfter("tel:")
        }

        PreferenceUtils.getInstance(this)
        Utilities().setUpLocale(this)

        clickToButtons()

        // This activity needs to show even if the screen is off or locked
        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //Detect a nav bar and adapt layout accordingly
        val hasNavBar: Boolean = Utilities().hasNavBar(this)
        val navBarHeight: Int = Utilities().navBarHeight(this)
        if (hasNavBar) {
            ongoing_call_layout.setPadding(0, 0, 0, navBarHeight)
        }

        // Audio Manager
        mAudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.mode = AudioManager.MODE_IN_CALL

        setOnKeyDownListener(this)

        // Initiate Swipe listener
        mIncomingCallSwipeListener = object : AllPurposeTouchListener(this) {
            override fun onSwipeRight() {
                activateCall()
            }

            override fun onSwipeLeft() {
                endCall()
            }

            override fun onSwipeTop() {

            }
        }
        ongoing_call_layout.setOnTouchListener(mIncomingCallSwipeListener)

        initProximitySensor()

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

        //OngoingCall.registerCallback()
        OngoingCall.registerCallback(callCallback)
        OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)

        getInfoForContact()
        getContactInformation()
    }

    private fun initProximitySensor() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "com.simplemobiletools.dialer.pro:wake_lock"
        )
        wakeLock!!.acquire(10 * MINUTE_SECONDS * 1000L)
    }

    fun getContactInformation() {
        if (contactDialer.name != null) {
            text_caller.text = contactDialer.name
        } else {
            text_caller.text = number
        }
        if (contactDialer.photoUri != null) {
            image_placeholder.visibility = View.INVISIBLE
            image_photo.visibility = View.VISIBLE
            image_photo.setImageURI(Uri.parse(contactDialer.photoUri))
        }
    }

    fun getInfoForContact() {
        val id = Funcs.getContactID(this, "$number")
        if (id != null) {
            contactDialer.parseDataCursor(id, this)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle??) {
        super.onPostCreate(savedInstanceState)
        OngoingCall.registerCallback(callCallback)
        //updateUI(OngoingCall.getState())
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

        if (OngoingCall.getState() == Call.STATE_DIALING) {
            endCall()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        OngoingCall.unregisterCallback(callCallback)
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        releaseWakeLock()
        endCall()
    }


    fun activateCall() {
        OngoingCall.answer()
        swithToCallingUI()
    }

    fun endCall() {
        mCallTimeHandler.sendEmptyMessage(TIME_STOP)
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        OngoingCall.reject()
        releaseWakeLock()
        if (OngoingCall.isAutoCalling()) {
            finish()
        } else {
            Handler().postDelayed(this::finish, END_CALL_MILLIS) // Delay the closing of the call
        }

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (ignored: Exception) {
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    // -- Wake Lock -- //
    private fun acquireWakeLock() {
        if (!wakeLock.isHeld) wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    @SuppressLint("RestrictedApi")
    fun swithToCallingUI() {
        acquireWakeLock()
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
        acquireWakeLock()

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

    override fun onStop() {
        super.onStop()
        disposables.clear()
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
            isSpeakerOn = !isSpeakerOn
            Utilities().toggleViewActivation(button_speaker)
            mAudioManager.isSpeakerphoneOn = button_speaker.isActivated

            val newRoute =
                if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
            OngoingCall.inCallService?.setAudioRoute(newRoute)

        }

        button_hold.setOnClickListener {
            Utilities().toggleViewActivation(button_hold)
            OngoingCall.hold(button_hold.isActivated)
        }

        button_mute.setOnClickListener {
            Utilities().toggleViewActivation(button_mute)
            mAudioManager.isMicrophoneMute = button_mute.isActivated
            OngoingCall.inCallService?.setMuted(button_mute.isActivated)
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
        OngoingCall.keypad(char)
        input_text.addCharacter(char)
    }

    @SuppressLint("HandlerLeak")
    inner class CallTimeHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {

                TIME_START -> {
                    mCallTimer.start()
                    mCallTimeHandler.sendEmptyMessage(TIME_UPDATE)
                }

                TIME_STOP -> {
                    mCallTimeHandler.removeMessages(TIME_UPDATE)
                    mCallTimer.stop()
                    updateTimeUI()
                }

                TIME_UPDATE -> {
                    updateTimeUI()
                    mCallTimeHandler.sendEmptyMessageDelayed(TIME_UPDATE, REFRESH_RATE.toLong())
                }

            }
        }
    }

    private fun updateTimeUI() {
        text_stopwatch.text = mCallTimer.getStringTime()
    }

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, OngoingCallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }

    fun setOnKeyDownListener(onKeyDownListener: OnKeyDownListener) {
        mOnKeyDownListener = onKeyDownListener
    }

    override fun onKeyPressed(keyCode: Int, event: KeyEvent) {
        OngoingCall.keypad(event.unicodeChar as (Char))
    }

    private fun setupNotification() {
        val callState = OngoingCall.getState()
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

        var callerName = ""

        callerName = if (contactDialer.name != null) {
            contactDialer.name!!
        } else {
            number!!
        }

        callContactAvatar = getCallContactAvatar()

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
        if (contactDialer?.photoUri?.isNotEmpty() == true) {
            val photoUri = Uri.parse(contactDialer!!.photoUri)
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
            updateUi(state)
        }
    }

    @SuppressLint("SetTextI18n", "RestrictedApi")
    private fun updateUi(state: Int) {

        text_status.text = "${state.asString().toLowerCase().capitalize()}"
        mStateText = "${state.asString().toLowerCase().capitalize()}"

        if (state == Call.STATE_DIALING) {
            swithToCallingUI()
        }

        if (state == Call.STATE_RINGING) {
            visibilityInomingCall()
        }

        if (state == Call.STATE_ACTIVE) {
            mCallTimeHandler.sendEmptyMessage(TIME_START)
        }

        setupNotification()
    }



}

