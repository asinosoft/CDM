package com.asinosoft.cdm

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.telecom.Call
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.asinosoft.cdm.dialer.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_ongoing_call.*
import kotlinx.android.synthetic.main.on_going_call.*
import java.util.concurrent.TimeUnit

class OngoingCallActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    private lateinit var number: String

    lateinit var context: Context

    // Finals
    private val END_CALL_MILLIS: Long = 1500
    private val CHANNEL_ID = "notification"
    private val NOTIFICATION_ID = 42069
    val ACTION_ANSWER = "Ответить"
    val ACTION_HANGUP = "Отклонить"

    // Call State
    private var mState = 0
    private var mStateText: String? = null

    // Handler variables
    private val TIME_START = 1
    private val TIME_STOP = 0
    private val TIME_UPDATE = 2
    private val REFRESH_RATE = 100

    // BottomSheet
    var mBottomSheetBehavior: BottomSheetBehavior<*>? = null

    //  Current states
    var mIsCallingUI = false
    var mIsCreatingUI = true

    //Audio Manager
    lateinit var mAudioManager: AudioManager

    // Utilities
    var mCallTimer = Stopwatch()

    lateinit var mSmsOverlaySwipeListener: AllPurposeTouchListener
    lateinit var mIncomingCallSwipeListener: AllPurposeTouchListener

    // Handlers
    var mCallTimeHandler: Handler = CallTimeHandler()

    // PowerManager
    lateinit var powerManager: PowerManager
    lateinit var wakeLock: PowerManager.WakeLock
    private var field = 0x00000020

    // Notification
    lateinit var mBuilder: NotificationCompat.Builder
    lateinit var mNotificationManager: NotificationManager

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing_call)
        number = intent.data!!.schemeSpecificPart
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

        // Initiate PowerManager and WakeLock (turn screen on/off according to distance from face)
        try {
            field = PowerManager::class.java.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null)
        } catch (ignored: Throwable) {
        }
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(field, localClassName)

        // Audio Manager
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.mode = AudioManager.MODE_IN_CALL

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

        createNotificationChannel()
        createNotification()

    }

    override fun onStart() {
        super.onStart()
        mIsCreatingUI = false
    }

    override fun onPostCreate(savedInstanceState: Bundle??) {
        super.onPostCreate(savedInstanceState)
        OngoingCall.registerCallback()
        OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)
    }

    override fun onBackPressed() {
        // In case the dialpad is opened, pressing the back button will close it
        if (mBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED){
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // You cant press the back button in order to get out of the call
    }

    override fun onDestroy() {
        super.onDestroy()
        OngoingCall.registerCallback()
        releaseWakeLock()
        cancelNotification()
    }

    fun activateCall() {
        OngoingCall.answer()
        swithToCallingUI()
    }

    fun endCall() {
        mCallTimeHandler.sendEmptyMessage(TIME_STOP)
        OngoingCall.reject()
        releaseWakeLock()
        if (OngoingCall.isAutoCalling()) {
            finish()
        } else {
            Handler().postDelayed(this::finish, END_CALL_MILLIS) // Delay the closing of the call
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    // -- Wake Lock -- //
    private fun acquireWakeLock() {
        if (!wakeLock.isHeld) wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    @SuppressLint("RestrictedApi")
    fun swithToCallingUI() {
        if (mIsCallingUI) {
            return
        } else {
            mIsCallingUI = true
        }
        mAudioManager.mode = AudioManager.MODE_IN_CALL
        acquireWakeLock()
        mCallTimeHandler.sendEmptyMessage(TIME_START)

        // Change the buttons layout
        answer_btn.visibility = View.INVISIBLE
        reject_btn.visibility = View.INVISIBLE
        reject_btn2.visibility = View.VISIBLE
        button_hold.visibility = View.VISIBLE
        button_mute.visibility = View.VISIBLE
        button_keypad.visibility = View.VISIBLE
        button_speaker.visibility = View.VISIBLE
        button_add_call.visibility = View.VISIBLE
    }


    @SuppressLint("RestrictedApi")
    private fun visibilityInomingCall() {

        // Change the buttons layout
        reject_btn2.visibility - View.INVISIBLE
        answer_btn.visibility = View.VISIBLE
        reject_btn.visibility = View.VISIBLE
        reject_btn2.visibility = View.INVISIBLE
        button_hold.visibility = View.INVISIBLE
        button_mute.visibility = View.INVISIBLE
        button_keypad.visibility = View.INVISIBLE
        button_speaker.visibility = View.INVISIBLE
        button_add_call.visibility = View.INVISIBLE

    }

    @SuppressLint("SetTextI18n", "RestrictedApi")
    private fun updateUi(state: Int) {


        text_status.text = "${state.asString().toLowerCase().capitalize()}"
        text_caller.text = "$number"

        if (state == Call.STATE_DIALING) {
            swithToCallingUI()
        }

        if (state == Call.STATE_RINGING) {
            visibilityInomingCall()
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    @SuppressLint("ResourceAsColor")
    private fun clickToButtons(){

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
            Utilities().toggleViewActivation(button_speaker)
            mAudioManager.isSpeakerphoneOn = button_speaker.isActivated

        }

        button_hold.setOnClickListener {
            Utilities().toggleViewActivation(button_hold)
            OngoingCall.hold(button_hold.isActivated)
        }

        button_mute.setOnClickListener {
            Utilities().toggleViewActivation(button_mute)
            mAudioManager.isMicrophoneMute = button_mute.isActivated
        }

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

    private fun createNotification() {

        val callerName = "$number"
        val touchNotification = Intent(this, OngoingCallActivity::class.java)
        touchNotification.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        val pendingIntent = PendingIntent.getActivity(this, 0, touchNotification, 0)

        // Answer Button Intent
        val answerIntent = Intent(this, NotificationActionReceiver::class.java)
        answerIntent.action = ACTION_ANSWER
        answerIntent.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0)
        val answerPendingIntent = PendingIntent.getBroadcast(this, 0, answerIntent, PendingIntent.FLAG_CANCEL_CURRENT)


        // Hangup Button Intent
        val hangupIntent = Intent(this, NotificationActionReceiver::class.java)
        hangupIntent.action = OngoingCallActivity().ACTION_HANGUP
        hangupIntent.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0)
        val hangupPendingIntent = PendingIntent.getBroadcast(this, 1, hangupIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_full_144)
            .setContentTitle(callerName)
            .setContentText(mStateText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setColor(Utilities().getAccentColor(this))
            .setOngoing(true)
            .setAutoCancel(true)

        // Adding the action buttons
        mBuilder.addAction(R.drawable.outline_call_24, getString(R.string.action_answer), answerPendingIntent)
        mBuilder.addAction(R.drawable.outline_call_end_24, getString(R.string.action_hangup), hangupPendingIntent)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build())

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            mNotificationManager = getSystemService(NotificationManager::class.java)
            mNotificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelNotification() {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

}

