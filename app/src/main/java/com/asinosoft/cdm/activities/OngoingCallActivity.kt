package com.asinosoft.cdm.activities

import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.telecom.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.StringsWithIconsAdapter
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.databinding.ActivityOngoingCallBinding
import com.asinosoft.cdm.dialer.*
import org.jetbrains.anko.audioManager
import org.jetbrains.anko.telecomManager
import org.jetbrains.anko.telephonyManager
import java.util.*

class OngoingCallActivity : BaseActivity() {
    private lateinit var v: ActivityOngoingCallBinding

    // bools
    private var isCallEnded = false
    private var callDuration = 0
    private lateinit var callContact: CallContact
    private val channelId = "simple_dialer_channel"
    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var callTimer = Timer()

    // Finals
    private val CALL_NOTIFICATION_ID = 1
    val MINUTE_SECONDS = 60

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Call", "Created")
        super.onCreate(savedInstanceState)
        v = ActivityOngoingCallBinding.inflate(layoutInflater)
        setContentView(v.root)

        val contactUri: Uri?
        if (intent.action == Intent.ACTION_CALL && intent.data != null) {
            // Исходящий звонок
            contactUri = intent.data
            withPermission(arrayOf(CALL_PHONE, READ_PHONE_STATE)) { permitted ->
                if (permitted) placeCall(contactUri)
            }
        } else {
            // Входящий звонок
            contactUri = CallManager.getCallPhone()

            CallManager.getCallDetails()?.let {
                initSimInfo(it)
            }
        }

        if (null == contactUri) {
            return finish()
        }

        callContact = findContact(contactUri)
        updateOtherPersonsInfo()

        clickToButtons()
        addLockScreenFlags()
        initProximitySensor()

        audioManager.mode = AudioManager.MODE_IN_CALL
        // Detect a nav bar and adapt layout accordingly
        val hasNavBar: Boolean = Utilities().hasNavBar(this)
        val navBarHeight: Int = Utilities().navBarHeight(this)
        if (hasNavBar) {
            v.frame.setPadding(0, 0, 0, navBarHeight)
        }

        CallManager.registerCallback(callCallback)
        if (CallManager.isCalled()) {
            updateCallState(CallManager.getState())
        }
        Log.d("Call", "Creation complete")
    }

    private fun initSimInfo(details: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val info = telephonyManager.createForPhoneAccountHandle(details.accountHandle)
            v.ongoingCallLayout.textOperator.text = info?.simOperatorName ?: "Неизвестно"
        }
    }

    private fun placeCall(contact: Uri?) {
        Log.d("Call::placeCall", contact.toString())
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(CALL_PHONE)) {
            selectPhoneAccount { phoneAccount ->
                Bundle().apply {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccount)
                    putBoolean(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, false)
                    putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
                    telecomManager.placeCall(contact, this)
                }
            }
        } else {
            Log.d("PLACE CALL", "NO PERMISSION!!!")
        }
    }

    @SuppressLint("MissingPermission")
    private fun selectPhoneAccount(onSelect: (PhoneAccountHandle) -> Unit) {
        val accounts = telecomManager.callCapablePhoneAccounts
        if (1 == accounts.size || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            onSelect(accounts[0])
        } else {
            val slots: Array<String> =
                accounts.mapNotNull { telephonyManager.createForPhoneAccountHandle(it)?.simOperatorName }
                    .toTypedArray()
            val icons: Array<Int> =
                arrayOf(R.drawable.ic_sim1, R.drawable.ic_sim2, R.drawable.ic_sim3)
            val adapter = StringsWithIconsAdapter(this, slots, icons)

            AlertDialog.Builder(this)
                .setTitle(R.string.sim_selection_title)
                .setAdapter(adapter) { dialog, index ->
                    dialog.dismiss()
                    onSelect(accounts[index])
                }
                .setOnDismissListener {
                    finish()
                }
                .create()
                .show()
        }
    }

    private fun findContact(uri: Uri): CallContact {
        Log.d("Call", "findContact $uri")
        val number = Uri.decode(uri.toString()).substringAfter("tel:")
        return ContactRepositoryImpl(this).getContactByPhone(number)?.let {
            Log.d("Call", "found = ${it.name}")
            return CallContact(
                it.name,
                it.photoUri,
                number
            )
        } ?: CallContact(
            "",
            Uri.parse("android.resource://com.asinosoft.cdm/drawable/${R.drawable.ic_default_photo}"),
            number
        )
    }

    private fun updateOtherPersonsInfo() {
        v.ongoingCallLayout.textStatus.text =
            if (callContact.name.isNotEmpty()) callContact.name else getString(R.string.unknown_caller)
        if (callContact.name != "null") {
            v.ongoingCallLayout.textCaller.text = callContact.name
            v.ongoingCallLayout.textCallerNumber.text = callContact.number
        } else {
            v.ongoingCallLayout.textCaller.text = callContact.number
            v.ongoingCallLayout.textCallerNumber.visibility = View.GONE
        }

        v.ongoingCallLayout.imagePlaceholder.setImageURI(callContact.photoUri)
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
        Log.d("Call", "initProximitySensor")
        proximityWakeLock = getSystemService(PowerManager::class.java).newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "com.asinosoft.cdm:wake_lock"
        )
        proximityWakeLock!!.acquire(10 * MINUTE_SECONDS * 1000L)
    }

    override fun onBackPressed() {
        Log.d("Call", "Back pressed")
        // In case the dialpad is opened, pressing the back button will close it
        if (v.keyboardWrapper.isVisible) {
            v.keyboardWrapper.visibility = View.GONE
            switchToCallingUI()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        Log.d("Call", "Destroy")
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
        Log.d("Call", "activateCall")
        CallManager.accept()
        switchToCallingUI()
    }

    fun endCall() {
        Log.d("Call", "endCall")
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
                v.ongoingCallLayout.textStopwatch.text =
                    "${callDuration.getFormattedDuration()}"
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
                    v.ongoingCallLayout.textStopwatch.text = callDuration.getFormattedDuration()
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

    fun switchToCallingUI() {
        // Change the buttons layout
        v.ongoingCallLayout.textStatus.visibility = View.VISIBLE
        v.ongoingCallLayout.textCaller.visibility = View.VISIBLE
        v.ongoingCallLayout.answerBtn.visibility = View.GONE
        v.ongoingCallLayout.rejectBtn.visibility = View.GONE
        v.ongoingCallLayout.rejectBtn2.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.VISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.VISIBLE
    }

    private fun visibilityIncomingCall() {
        // Change the buttons layout
        v.ongoingCallLayout.answerBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.rejectBtn.visibility = View.VISIBLE
        v.ongoingCallLayout.rejectBtn2.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonHold.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonMute.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonKeypad.visibility = View.INVISIBLE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.INVISIBLE
    }

    private fun viewKeyboard() {
        v.ongoingCallLayout.textCallerNumber.visibility = View.GONE
        v.ongoingCallLayout.textStatus.visibility = View.GONE
        v.ongoingCallLayout.textCaller.visibility = View.GONE
        v.ongoingCallLayout.rejectBtn2.visibility - View.GONE
        v.ongoingCallLayout.answerBtn.visibility = View.GONE
        v.ongoingCallLayout.rejectBtn.visibility = View.GONE
        v.ongoingCallLayout.buttonHold.visibility = View.GONE
        v.ongoingCallLayout.buttonMute.visibility = View.GONE
        v.ongoingCallLayout.buttonKeypad.visibility = View.GONE
        v.ongoingCallLayout.buttonSpeaker.visibility = View.GONE
    }

    private fun toggleMicrophone() {
        Log.d("Call", "Toggle MIC")
        Utilities().toggleViewActivation(v.ongoingCallLayout.buttonMute)
        audioManager.isMicrophoneMute = v.ongoingCallLayout.buttonMute.isActivated
        val drawable =
            if (v.ongoingCallLayout.buttonMute.isActivated) R.drawable.ic_mic_off_black_24dp else R.drawable.ic_mic_black_24dp
        v.ongoingCallLayout.buttonMute.setImageDrawable(getDrawable(drawable))
        getSystemService(InCallService::class.java)?.setMuted(v.ongoingCallLayout.buttonMute.isActivated)
    }

    private fun toggleSpeaker() {
        Log.d("Call", "Toggle SPEAKER")
        Utilities().toggleViewActivation(v.ongoingCallLayout.buttonSpeaker)
        audioManager.isSpeakerphoneOn = v.ongoingCallLayout.buttonSpeaker.isActivated
        val drawable =
            if (v.ongoingCallLayout.buttonSpeaker.isActivated) R.drawable.ic_baseline_volume_off_24 else R.drawable.outline_volume_up_24
        v.ongoingCallLayout.buttonSpeaker.setImageDrawable(getDrawable(drawable))
        val newRoute =
            if (v.ongoingCallLayout.buttonSpeaker.isActivated) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        getSystemService(InCallService::class.java)?.setAudioRoute(newRoute)
    }

    private fun toggleHold() {
        Log.d("Call", "Toggle HOLD")
        Utilities().toggleViewActivation(v.ongoingCallLayout.buttonHold)
        CallManager.hold(v.ongoingCallLayout.buttonHold.isActivated)
    }

    @SuppressLint("ResourceAsColor")
    private fun clickToButtons() {

        v.ongoingCallLayout.answerBtn.setOnClickListener {
            activateCall()
        }
        v.ongoingCallLayout.rejectBtn2.setOnClickListener {
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

    private fun registerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("Call", "Register notification channel: $channelId")
            with(NotificationManagerCompat.from(this)) {
                createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    private fun setupNotification() {
        val callState = CallManager.getState()
        registerNotificationChannel()

        val openAppIntent = Intent(this, OngoingCallActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0)

        val acceptCallIntent = Intent(this, NotificationActionReceiver::class.java)
        acceptCallIntent.action = ACCEPT_CALL
        val acceptPendingIntent =
            PendingIntent.getBroadcast(this, 0, acceptCallIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val muteCallIntent = Intent(this, NotificationActionReceiver::class.java)
        muteCallIntent.action = MUTE_CALL
        val mutePendingIntent = PendingIntent.getActivity(this, 0, muteCallIntent, 0)

        val speakerCallIntent = Intent(this, NotificationActionReceiver::class.java)
        speakerCallIntent.action = SPEAKER_CALL
        val speakerPendingIntent = PendingIntent.getActivity(this, 0, speakerCallIntent, 0)

        val declineCallIntent = Intent(this, NotificationActionReceiver::class.java)
        declineCallIntent.action = DECLINE_CALL
        val declinePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            declineCallIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val callerName =
            if (callContact.name.isNotEmpty()) callContact.name else getString(
                R.string.unknown_caller
            )
        val contentTextId = when (callState) {
            Call.STATE_RINGING -> R.string.state_call_ringing
            Call.STATE_DIALING -> R.string.status_call_dialing
            Call.STATE_DISCONNECTED -> R.string.status_call_disconnected
            Call.STATE_DISCONNECTING -> R.string.status_call_disconnected
            else -> R.string.state_call_active
        }

        val collapsedView = RemoteViews(packageName, R.layout.call_notification_two).apply {
            setTextViewText(R.id.notification_caller_name, callerName)
            setTextViewText(R.id.notification_call_status, getString(contentTextId))
            setViewVisibility(
                R.id.notification_accept_call,
                if (callState == Call.STATE_RINGING) View.VISIBLE else View.GONE
            )
            setViewVisibility(
                R.id.notification_mic_off,
                if (callState == Call.STATE_ACTIVE) View.VISIBLE else View.GONE
            )
            setViewVisibility(
                R.id.notification_speaker,
                if (callState == Call.STATE_ACTIVE) View.VISIBLE else View.GONE
            )

            setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
            setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)
            setOnClickPendingIntent(R.id.notification_speaker, speakerPendingIntent)
            setOnClickPendingIntent(R.id.notification_mic_off, mutePendingIntent)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_icon_512)
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(collapsedView)
            .setOngoing(true)
            .setSound(null)
            .setUsesChronometer(callState == Call.STATE_ACTIVE)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        builder.setLargeIcon(
            contentResolver.openAssetFileDescriptor(callContact.photoUri, "r")?.let {
                BitmapFactory.decodeStream(
                    it.createInputStream(),
                )
            }
        )

        val notification = builder.build()
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    @SuppressLint("NewApi")
    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            updateCallState(state)
        }
    }

    private fun updateCallState(state: Int) {
        Log.d("Call", "STATE → $state")
        when (state) {
            Call.STATE_RINGING -> visibilityIncomingCall()
            Call.STATE_ACTIVE -> {
                callStarted()
                switchToCallingUI()
            }
            Call.STATE_DISCONNECTED -> endCall()
            Call.STATE_CONNECTING, Call.STATE_DIALING -> switchToCallingUI()
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
            v.ongoingCallLayout.textStatus.text = getString(statusTextId)
        }

        setupNotification()
    }
}
