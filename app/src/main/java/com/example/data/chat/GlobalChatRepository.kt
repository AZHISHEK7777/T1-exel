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
            // Seed default community messages if empty
            val existing = chatDao.getAllMessages().first()
            if (existing.isEmpty()) {
                val now = System.currentTimeMillis()
                val seed = listOf(
                    ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        senderName = "Abhishek (Developer)",
                        senderRole = "ADMIN",
                        messageText = "Welcome to T1 Esports Community Hub! This is our official Free Fire squad chat. Share your drag sensitivities, loadouts and room codes here.",
                        timestamp = now - 180_000,
                        isFromMe = false
                    ),
                    ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        senderName = "Rahul_Pro",
                        senderRole = "VIP",
                        messageText = "The Tatsuya + Maro + Hayato combination from the Skills tab is totally broken in CS Ranked! Straight headshots with SMG.",
                        timestamp = now - 120_000,
                        isFromMe = false
                    ),
                    ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        senderName = "Viper_ES",
                        senderRole = "PRO",
                        messageText = "Anyone up for custom 1v1 room? I have 2 room cards ready.",
                        timestamp = now - 60_000,
                        isFromMe = false
                    )
                )
                chatDao.insertMessages(seed)
            }

            // Initialize Firebase Firestore Live Realtime Cloud Sync
            setupFirestoreRealtimeSync()
        }
    }

    private fun getFirestoreInstance(): FirebaseFirestore? {
        if (firestore != null) return firestore
        return try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance().also { firestore = it }
        } catch (e: Exception) {
            Log.e("GlobalChatRepository", "Failed to get Firestore instance: ${e.message}")
            null
        }
    }

    private fun setupFirestoreRealtimeSync() {
        try {
            val fs = getFirestoreInstance()
            if (fs == null) {
                Log.w("GlobalChatRepository", "Firestore instance is null, retrying in 2 seconds...")
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    setupFirestoreRealtimeSync()
                }
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
                                val senderRole = d.getString("senderRole") ?: "VIP"
                                val messageText = d.getString("messageText") ?: ""
                                val attachmentUri = d.getString("attachmentUri")
                                val attachmentType = d.getString("attachmentType") ?: "NONE"
                                val timestamp = d.getLong("timestamp") ?: System.currentTimeMillis()
                                val senderDeviceId = d.getString("senderDeviceId") ?: ""

                                val isFromMe = (senderDeviceId == deviceId)

                                val messageEntity = ChatMessageEntity(
                                    id = docId,
                                    senderName = senderName,
                                    senderRole = senderRole,
                                    messageText = messageText,
                                    attachmentUri = if (attachmentUri.isNullOrBlank()) null else attachmentUri,
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
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            senderName = senderName.ifBlank { "Pro Player" },
            senderRole = userRole,
            messageText = trimmedMsg,
            attachmentUri = attachmentUri,
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
                "attachmentUri" to (message.attachmentUri ?: ""),
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
