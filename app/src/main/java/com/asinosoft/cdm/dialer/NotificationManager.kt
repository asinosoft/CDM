package com.asinosoft.cdm.dialer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.asinosoft.cdm.R
import com.asinosoft.cdm.activities.OngoingCallActivity

/**
 * Уведомления о входящем/текущем звонке
 */
class NotificationManager(private val context: Context) {
    private val CALL_NOTIFICATION_ID = 1
    private val CHANNEL_ID = "simple_dialer_channel"

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            Log.d("Call", "Register notification channel: $CHANNEL_ID")
            with(NotificationManagerCompat.from(context)) {
                createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    fun show(callState: Int) {
        val collapsedView = RemoteViews(context.packageName, R.layout.call_notification).apply {
            setTextViewText(R.id.notification_caller_name, CallManager.getCallerName())
            setTextViewText(R.id.notification_call_status, context.getCallStateText(callState))
            setViewVisibility(
                R.id.notification_accept_call,
                if (Call.STATE_RINGING == callState) View.VISIBLE else View.GONE
            )
            setViewVisibility(
                R.id.notification_mic_off,
                if (Call.STATE_ACTIVE == callState) View.VISIBLE else View.GONE
            )
            setViewVisibility(
                R.id.notification_speaker,
                if (Call.STATE_ACTIVE == callState) View.VISIBLE else View.GONE
            )

            setOnClickPendingIntent(R.id.notification_decline_call, declineCallIntent())
            setOnClickPendingIntent(R.id.notification_accept_call, acceptCallIntent())
            setOnClickPendingIntent(R.id.notification_speaker, toggleSpeakerIntent())
            setOnClickPendingIntent(R.id.notification_mic_off, toggleMuteIntent())

            setImageViewResource(R.id.sim, CallManager.getSimSlotIcon() ?: R.drawable.ic_sim3)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.call)
            .setContentIntent(openAppIntent())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(collapsedView)
            .setOngoing(true)
            .setSound(null)
            .setUsesChronometer(Call.STATE_ACTIVE == callState)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        builder.setLargeIcon(CallManager.getCallerPhoto())

        context.notificationManager.notify(CALL_NOTIFICATION_ID, builder.build())
    }

    fun hide() {
        context.notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    private fun openAppIntent() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, OngoingCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            },
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun acceptCallIntent() =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACCEPT_CALL
            },
            PendingIntent.FLAG_CANCEL_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )

    private fun declineCallIntent() =
        PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, NotificationActionReceiver::class.java).apply { action = DECLINE_CALL },
            PendingIntent.FLAG_CANCEL_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )

    private fun toggleMuteIntent() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = MUTE_CALL
            },
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun toggleSpeakerIntent() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, NotificationActionReceiver::class.java).apply { action = SPEAKER_CALL },
            PendingIntent.FLAG_IMMUTABLE
        )
}
