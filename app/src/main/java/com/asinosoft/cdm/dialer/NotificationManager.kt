package com.asinosoft.cdm.dialer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.asinosoft.cdm.R
import com.asinosoft.cdm.activities.OngoingCallActivity
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.helpers.*
import timber.log.Timber

/**
 * Уведомления о входящем/текущем звонке
 */
class NotificationManager(private val context: Context) {
    companion object {
        private const val INCOMING_CHANNEL = "Incoming"
        private const val ONGOING_CHANNEL = "Ongoing"
    }

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            Timber.d("Register notification channels")
            with(NotificationManagerCompat.from(context)) {
                createNotificationChannel(
                    NotificationChannel(
                        INCOMING_CHANNEL,
                        context.getString(R.string.incoming_notification_channel),
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        setSound(null, null)
                    }
                )

                createNotificationChannel(
                    NotificationChannel(
                        ONGOING_CHANNEL,
                        context.getString(R.string.ongoing_notification_channel),
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        setSound(null, null)
                    }
                )
            }
        }
    }

    fun show(calls: List<Call>) {
        // Удаляем завершённые звонки
        context.notificationManager.activeNotifications.iterator().forEach { notification ->
            if (calls.all { call -> call.id != notification.id }) {
                context.notificationManager.cancel(notification.id)
            }
        }

        // Добавляем уведомления о новых звонках и обновляем текущие
        calls.forEach { update(it) }
    }

    fun update(call: Call) {
        Timber.d("update # %d → %s", call.id, call.phone)
        val phone = call.details.handle.schemeSpecificPart
        val contact = ContactRepositoryImpl(context).getContactByPhone(phone)
        val callState = call.callState

        val view = RemoteViews(context.packageName, R.layout.call_notification).apply {
            setTextViewText(R.id.notification_caller_name, contact.title)
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

            setOnClickPendingIntent(R.id.notification_decline_call, declineCallIntent(call))
            setOnClickPendingIntent(R.id.notification_accept_call, acceptCallIntent(call))
            setOnClickPendingIntent(R.id.notification_speaker, toggleSpeakerIntent())
            setOnClickPendingIntent(R.id.notification_mic_off, toggleMuteIntent())

            val simSlot = context.getSimSlot(call.details.accountHandle)
            val simIcon = when (simSlot?.id) {
                1 -> R.drawable.ic_sim1
                2 -> R.drawable.ic_sim2
                else -> R.drawable.ic_sim3
            }
            setImageViewResource(R.id.sim, simIcon)
        }

        val channel = if (Call.STATE_RINGING == callState) INCOMING_CHANNEL else ONGOING_CHANNEL
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.call)
            .setLargeIcon(contact.getAvatar(context, AvatarHelper.SHORT).toBitmap())
            .setContentIntent(openAppIntent(call))
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCustomContentView(view)
            .setOngoing(true)
            .setSound(null)
            .setSilent(Call.STATE_RINGING == callState) // Для входящих звонков всплывающее уведомление не показываем
            .setUsesChronometer(Call.STATE_ACTIVE == callState)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setWhen(call.details.connectTimeMillis)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setFullScreenIntent(openAppIntent(call), Call.STATE_RINGING == callState)

        context.notificationManager.notify(call.id, builder.build())
    }

    private fun openAppIntent(call: Call) =
        PendingIntent.getActivity(
            context,
            0,
            OngoingCallActivity.intent(context, call),
            if (Build.VERSION.SDK_INT >= 31)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun acceptCallIntent(call: Call) =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NotificationActionReceiver::class.java).setAction(ACCEPT_CALL)
                .setData(call.details.handle),
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun declineCallIntent(call: Call) =
        PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, NotificationActionReceiver::class.java).setAction(DECLINE_CALL)
                .setData(call.details.handle),
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun toggleMuteIntent() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, NotificationActionReceiver::class.java).setAction(MUTE_CALL),
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun toggleSpeakerIntent() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, NotificationActionReceiver::class.java).setAction(SPEAKER_CALL),
            PendingIntent.FLAG_IMMUTABLE
        )
}
