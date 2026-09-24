package com.example.data.call

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object SquadCallNotificationHelper {

    const val CHANNEL_ID_INCOMING = "squad_incoming_call_channel_v2"
    const val CHANNEL_ID_ONGOING = "squad_ongoing_call_channel_v2"
    const val CHANNEL_ID_CHAT = "squad_chat_messages_channel"
    private const val NOTIFICATION_ID_INCOMING = 9910
    private const val NOTIFICATION_ID_ONGOING = 9911
    private const val NOTIFICATION_ID_CHAT = 9912

    const val ACTION_ACCEPT_CALL = "com.example.ACTION_ACCEPT_CALL"
    const val ACTION_DECLINE_CALL = "com.example.ACTION_DECLINE_CALL"
    const val ACTION_LEAVE_CALL = "com.example.ACTION_LEAVE_CALL"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            // 1. Incoming Call Channel (High Priority Heads-Up with Ringtone & Vibration)
            val incomingChannel = NotificationChannel(
                CHANNEL_ID_INCOMING,
                "Incoming Squad Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for incoming T1 Squad voice calls"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 500)
                setSound(ringtoneUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(incomingChannel)

            // 2. Ongoing Call Channel
            val ongoingChannel = NotificationChannel(
                CHANNEL_ID_ONGOING,
                "Ongoing Squad Voice Call",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Status for active squad voice call"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(ongoingChannel)

            // 3. Squad Chat Messages Channel (WhatsApp-style notification)
            val chatNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val chatChannel = NotificationChannel(
                CHANNEL_ID_CHAT,
                "Squad Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming squad messages"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setSound(chatNotificationSound, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build())
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(chatChannel)
        }
    }

    fun showIncomingCallNotification(context: Context, callerName: String) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent when clicking the notification body
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_CALL_SCREEN", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            101,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Accept Intent
        val acceptIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_ACCEPT_CALL
        }
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            102,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline Intent
        val declineIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_DECLINE_CALL
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            103,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle("Incoming Squad Voice Call")
            .setContentText("$callerName is calling you to join the voice squad!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSound(ringtoneUri)
            .setVibrate(longArrayOf(0, 500, 300, 500, 300, 500))
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .addAction(android.R.drawable.sym_action_call, "ACCEPT", acceptPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DECLINE", declinePendingIntent)

        notificationManager.notify(NOTIFICATION_ID_INCOMING, builder.build())
    }

    fun showOngoingCallNotification(context: Context, participantsCount: Int) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_CALL_SCREEN", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            201,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val leaveIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_LEAVE_CALL
        }
        val leavePendingIntent = PendingIntent.getActivity(
            context,
            202,
            leaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ONGOING)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle("T1 Squad Call Active")
            .setContentText("Connected with $participantsCount players • Mic Active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "LEAVE CALL", leavePendingIntent)

        notificationManager.notify(NOTIFICATION_ID_ONGOING, builder.build())
    }

    fun showChatMessageNotification(context: Context, senderName: String, messageText: String) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_CHAT_SCREEN", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            301,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CHAT)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(NOTIFICATION_ID_CHAT, builder.build())
    }

    fun cancelIncomingNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_INCOMING)
    }

    fun cancelOngoingNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_ONGOING)
    }

    fun cancelAll(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_INCOMING)
        notificationManager.cancel(NOTIFICATION_ID_ONGOING)
    }
}
