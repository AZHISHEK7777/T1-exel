package com.example.data.notifications

import android.content.Context
import android.util.Log
import com.example.data.call.SquadCallNotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class T1FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "T1FCMService"
        private const val PREFS_FCM = "t1_fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_device_token"

        fun getSavedFcmToken(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_FCM, Context.MODE_PRIVATE)
            return prefs.getString(KEY_FCM_TOKEN, "") ?: ""
        }

        fun saveFcmTokenLocally(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_FCM, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token.trim()).apply()
        }

        fun uploadTokenToFirestore(context: Context, token: String, userName: String = "") {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "token" to token,
                    "userName" to userName,
                    "deviceModel" to android.os.Build.MODEL,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("device_fcm_tokens").document(token).set(data)
                if (userName.isNotBlank()) {
                    db.collection("user_fcm_tokens").document(userName).set(data)
                }
                Log.d(TAG, "Uploaded FCM token to Firestore for: $userName")
            } catch (e: Exception) {
                Log.w(TAG, "Failed uploading FCM token: ${e.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Token received: $token")
        saveFcmTokenLocally(applicationContext, token)

        val authPrefs = applicationContext.getSharedPreferences("t1_auth_prefs", Context.MODE_PRIVATE)
        val userName = authPrefs.getString("pref_user_name", "") ?: ""
        uploadTokenToFirestore(applicationContext, token, userName)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val messageType = data["type"] ?: "CHAT"

        when (messageType.uppercase()) {
            "CALL", "CALL_INVITE" -> {
                val caller = data["callerName"] ?: remoteMessage.notification?.title ?: "Squad Mate"
                SquadCallNotificationHelper.showIncomingCallNotification(applicationContext, caller)
            }
            "CHAT", "MESSAGE" -> {
                val sender = data["senderName"] ?: remoteMessage.notification?.title ?: "T1 Global Chat"
                val text = data["text"] ?: data["message"] ?: remoteMessage.notification?.body ?: "Sent you a message"
                SquadCallNotificationHelper.showChatMessageNotification(applicationContext, sender, text)
            }
            else -> {
                val title = remoteMessage.notification?.title ?: data["title"] ?: "T1 Esports"
                val body = remoteMessage.notification?.body ?: data["body"] ?: "New Squad Notification"
                SquadCallNotificationHelper.showChatMessageNotification(applicationContext, title, body)
            }
        }
    }
}
