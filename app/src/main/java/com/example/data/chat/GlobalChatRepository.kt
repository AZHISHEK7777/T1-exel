package com.example.data.chat

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DatabaseProvider
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class GlobalChatRepository(private val context: Context) {
    private val db: AppDatabase = DatabaseProvider.getDatabase(context)
    private val chatDao = db.chatDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val prefs = context.getSharedPreferences("t1_chat_prefs", Context.MODE_PRIVATE)
    val deviceId: String = prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("device_id", it).apply()
    }

    private val _incomingNotification = MutableSharedFlow<ChatMessageEntity>(extraBufferCapacity = 10)
    val incomingNotification = _incomingNotification.asSharedFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        scope.launch {
            // Purge any fake messages from local database
            try {
                chatDao.deleteFakeMessages()
            } catch (e: Exception) {
                Log.w("GlobalChatRepository", "Could not delete local fake messages: ${e.message}")
            }

            // Purge fake messages from Firestore if present
            try {
                val fs = getFirestoreInstance()
                fs?.collection("global_chat")
                    ?.whereIn("senderName", listOf("Abhishek (Developer)", "Rahul_Pro", "Viper_ES"))
                    ?.get()?.addOnSuccessListener { docs ->
                        for (doc in docs) {
                            doc.reference.delete()
                        }
                    }
            } catch (_: Exception) {}

            // Initialize Firebase Firestore Live Realtime Cloud Sync
            setupFirestoreRealtimeSync()
        }
    }

    private var isFirebaseAvailable: Boolean? = null

    private fun getFirestoreInstance(): FirebaseFirestore? {
        if (firestore != null) return firestore
        if (isFirebaseAvailable == false) return null

        return try {
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            val apps = com.google.firebase.FirebaseApp.getApps(context)
            if (resId == 0 && apps.isEmpty()) {
                isFirebaseAvailable = false
                return null
            }

            val app = if (apps.isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(context)
            } else {
                apps[0]
            }
            if (app != null) {
                FirebaseFirestore.getInstance(app).also {
                    firestore = it
                    isFirebaseAvailable = true
                }
            } else {
                isFirebaseAvailable = false
                null
            }
        } catch (_: Exception) {
            isFirebaseAvailable = false
            null
        }
    }

    private fun setupFirestoreRealtimeSync() {
        try {
            val fs = getFirestoreInstance()
            if (fs == null) {
                // Firebase is not configured or google-services.json is absent;
                // do not retry in a loop. Local Room storage is fully active.
                Log.i("GlobalChatRepository", "Cloud sync inactive; local offline mode enabled.")
                return
            }

            fs.collection("global_chat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limitToLast(150)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w("GlobalChatRepository", "Firestore listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch {
                            for (doc in snapshots.documentChanges) {
                                val d = doc.document
                                val docId = d.getString("id") ?: d.id
                                val senderName = d.getString("senderName") ?: "Player"
                                // Ignore any leftover fake messages
                                if (senderName in listOf("Abhishek (Developer)", "Rahul_Pro", "Viper_ES")) {
                                    continue
                                }
                                val senderRole = d.getString("senderRole") ?: "VIP"
                                val messageText = d.getString("messageText") ?: ""
                                val rawAttachmentUri = d.getString("attachmentUri")
                                val attachmentType = d.getString("attachmentType") ?: "NONE"
                                val timestamp = d.getLong("timestamp") ?: System.currentTimeMillis()
                                val senderDeviceId = d.getString("senderDeviceId") ?: ""

                                val isFromMe = (senderDeviceId == deviceId)

                                // If this is an image from cloud (base64 data URL), cache to local file for zero-lag display
                                val finalAttachmentUri = if (!rawAttachmentUri.isNullOrBlank() && rawAttachmentUri.startsWith("data:image/")) {
                                    ChatImageHelper.saveBase64ToCache(context, docId, rawAttachmentUri)
                                } else if (!rawAttachmentUri.isNullOrBlank()) {
                                    rawAttachmentUri
                                } else {
                                    null
                                }

                                val messageEntity = ChatMessageEntity(
                                    id = docId,
                                    senderName = senderName,
                                    senderRole = senderRole,
                                    messageText = messageText,
                                    attachmentUri = finalAttachmentUri,
                                    attachmentType = attachmentType,
                                    timestamp = timestamp,
                                    isFromMe = isFromMe
                                )

                                chatDao.insertMessage(messageEntity)

                                // If message arrived from another device live, broadcast in-app notification
                                if (!isFromMe && doc.type == DocumentChange.Type.ADDED) {
                                    _incomingNotification.tryEmit(messageEntity)
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("GlobalChatRepository", "Error setting up Firestore: ${e.message}", e)
        }
    }

    fun getMessages(): Flow<List<ChatMessageEntity>> {
        return chatDao.getAllMessages()
    }

    suspend fun sendMessage(
        senderName: String,
        messageText: String,
        senderRole: String = "VIP",
        attachmentUri: String? = null,
        attachmentType: String = "NONE"
    ): ChatMessageEntity {
        val trimmedMsg = messageText.trim()
        val userRole = when {
            senderRole == "ADMIN" || senderName.contains("Abhishek", ignoreCase = true) -> "ADMIN"
            senderRole == "PRO" -> "PRO"
            else -> "BASIC"
        }

        var localAttachmentUri = attachmentUri
        var cloudAttachmentUri = attachmentUri ?: ""

        // If user is sending a photo, compress and prepare Base64 data URL for cross-phone delivery
        if (attachmentType == "IMAGE" && !attachmentUri.isNullOrBlank()) {
            try {
                val parsedUri = android.net.Uri.parse(attachmentUri)
                val processed = ChatImageHelper.processAndCompressImage(context, parsedUri)
                if (processed != null) {
                    localAttachmentUri = processed.localFilePath
                    cloudAttachmentUri = processed.dataUrl
                }
            } catch (e: Exception) {
                Log.e("GlobalChatRepository", "Failed to compress outbound image: ${e.message}", e)
            }
        }

        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            senderName = senderName.ifBlank { "Pro Player" },
            senderRole = userRole,
            messageText = trimmedMsg,
            attachmentUri = localAttachmentUri,
            attachmentType = attachmentType,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        chatDao.insertMessage(message)

        // Broadcast to Firebase Firestore for ALL devices globally
        try {
            val firestoreData = hashMapOf(
                "id" to message.id,
                "senderName" to message.senderName,
                "senderRole" to message.senderRole,
                "messageText" to message.messageText,
                "attachmentUri" to cloudAttachmentUri,
                "attachmentType" to message.attachmentType,
                "timestamp" to message.timestamp,
                "senderDeviceId" to deviceId
            )
            getFirestoreInstance()?.collection("global_chat")?.document(message.id)?.set(firestoreData)
                ?.addOnFailureListener { e ->
                    Log.w("GlobalChatRepository", "Cloud broadcast failed: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w("GlobalChatRepository", "Could not send to Firestore: ${e.message}")
        }

        return message
    }
}
