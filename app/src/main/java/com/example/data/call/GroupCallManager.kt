package com.example.data.call

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CallParticipant(
    val name: String,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false
)

data class SquadCallRoom(
    val roomId: String = "squad_main_room",
    val callerName: String = "",
    val status: String = "IDLE", // IDLE, RINGING, ACTIVE, ENDED
    val startedAt: Long = 0L,
    val channelName: String = AgoraVoiceEngine.DEFAULT_CHANNEL,
    val participants: List<String> = emptyList()
)

enum class LocalCallStatus {
    IDLE,
    INCOMING,
    OUTGOING_RINGING,
    CONNECTED
}

class GroupCallManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var firestoreListener: ListenerRegistration? = null
    private var isFirebaseAvailable: Boolean? = null
    private var firestore: FirebaseFirestore? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    val agoraEngine = AgoraVoiceEngine(context)

    private val _currentRoom = MutableStateFlow<SquadCallRoom?>(null)
    val currentRoom: StateFlow<SquadCallRoom?> = _currentRoom.asStateFlow()

    private val _localStatus = MutableStateFlow(LocalCallStatus.IDLE)
    val localStatus: StateFlow<LocalCallStatus> = _localStatus.asStateFlow()

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private var myUserName: String = ""
    private var lastDismissedCallTime: Long = 0L

    init {
        setupFirestoreListener()
    }

    fun setUserName(name: String) {
        myUserName = name
    }

    private fun getFirestore(): FirebaseFirestore? {
        if (firestore != null) return firestore
        if (isFirebaseAvailable == false) return null
        return try {
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            val apps = FirebaseApp.getApps(context)
            if (resId == 0 && apps.isEmpty()) {
                isFirebaseAvailable = false
                return null
            }
            val app = if (apps.isEmpty()) FirebaseApp.initializeApp(context) else apps[0]
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

    private fun setupFirestoreListener() {
        val fs = getFirestore() ?: return
        firestoreListener?.remove()
        firestoreListener = fs.collection("group_calls").document("squad_live_room")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                val status = snapshot.getString("status") ?: "IDLE"
                val caller = snapshot.getString("callerName") ?: ""
                val startedAt = snapshot.getLong("startedAt") ?: 0L
                val channel = snapshot.getString("channelName") ?: AgoraVoiceEngine.DEFAULT_CHANNEL
                @Suppress("UNCHECKED_CAST")
                val participants = (snapshot.get("participants") as? List<String>) ?: emptyList()

                val room = SquadCallRoom(
                    roomId = "squad_live_room",
                    callerName = caller,
                    status = status,
                    startedAt = startedAt,
                    channelName = channel,
                    participants = participants
                )
                _currentRoom.value = room

                handleRoomUpdate(room)
            }
    }

    private fun handleRoomUpdate(room: SquadCallRoom) {
        when (room.status) {
            "RINGING", "ACTIVE" -> {
                val isInCall = room.participants.contains(myUserName)
                if (isInCall) {
                    _localStatus.value = LocalCallStatus.CONNECTED
                    SquadCallNotificationHelper.cancelIncomingNotification(context)
                    SquadCallNotificationHelper.showOngoingCallNotification(context, room.participants.size)
                } else if (_localStatus.value == LocalCallStatus.IDLE && room.startedAt > lastDismissedCallTime) {
                    // Incoming call from another user!
                    if (room.callerName.isNotBlank() && room.callerName != myUserName) {
                        _localStatus.value = LocalCallStatus.INCOMING
                        triggerRingVibration()
                        SquadCallNotificationHelper.showIncomingCallNotification(context, room.callerName)
                    }
                }
            }
            "ENDED", "IDLE" -> {
                if (_localStatus.value != LocalCallStatus.IDLE) {
                    _localStatus.value = LocalCallStatus.IDLE
                }
                SquadCallNotificationHelper.cancelAll(context)
            }
        }
    }

    fun startGroupCall(userName: String) {
        myUserName = userName
        _localStatus.value = LocalCallStatus.CONNECTED
        _isMicMuted.value = false
        enableSpeaker(true)

        val targetChannel = agoraEngine.getSavedChannel()

        // Connect Agora voice channel
        agoraEngine.joinChannel(channelName = targetChannel)
        SquadCallNotificationHelper.showOngoingCallNotification(context, 1)

        val newRoom = SquadCallRoom(
            roomId = "squad_live_room",
            callerName = userName,
            status = "ACTIVE",
            startedAt = System.currentTimeMillis(),
            channelName = targetChannel,
            participants = listOf(userName)
        )
        _currentRoom.value = newRoom

        scope.launch(Dispatchers.IO) {
            try {
                getFirestore()?.collection("group_calls")?.document("squad_live_room")?.set(
                    mapOf(
                        "roomId" to newRoom.roomId,
                        "callerName" to newRoom.callerName,
                        "status" to "ACTIVE",
                        "startedAt" to newRoom.startedAt,
                        "channelName" to targetChannel,
                        "participants" to listOf(userName)
                    )
                )
            } catch (e: Exception) {
                Log.e("GroupCallManager", "Failed to start call: ${e.message}")
            }
        }
    }

    fun joinCall(userName: String) {
        myUserName = userName
        _localStatus.value = LocalCallStatus.CONNECTED
        enableSpeaker(true)

        // Cancel incoming notification and show ongoing
        SquadCallNotificationHelper.cancelIncomingNotification(context)

        val room = _currentRoom.value ?: SquadCallRoom(callerName = userName, startedAt = System.currentTimeMillis())
        val targetChannel = room.channelName.ifBlank { agoraEngine.getSavedChannel() }

        // Connect Agora voice channel
        agoraEngine.joinChannel(channelName = targetChannel)

        val updatedParticipants = (room.participants + userName).distinct()
        _currentRoom.value = room.copy(participants = updatedParticipants, status = "ACTIVE")
        SquadCallNotificationHelper.showOngoingCallNotification(context, updatedParticipants.size)

        scope.launch(Dispatchers.IO) {
            try {
                getFirestore()?.collection("group_calls")?.document("squad_live_room")?.update(
                    "participants", updatedParticipants,
                    "status", "ACTIVE",
                    "channelName", targetChannel
                )
            } catch (e: Exception) {
                Log.e("GroupCallManager", "Failed to join call: ${e.message}")
            }
        }
    }

    fun declineIncomingCall() {
        lastDismissedCallTime = System.currentTimeMillis()
        _localStatus.value = LocalCallStatus.IDLE
        SquadCallNotificationHelper.cancelIncomingNotification(context)
    }

    fun leaveCall() {
        val currentParticipants = _currentRoom.value?.participants?.filter { it != myUserName } ?: emptyList()
        _localStatus.value = LocalCallStatus.IDLE
        SquadCallNotificationHelper.cancelAll(context)

        // Disconnect Agora voice
        agoraEngine.leaveChannel()

        scope.launch(Dispatchers.IO) {
            try {
                if (currentParticipants.isEmpty()) {
                    getFirestore()?.collection("group_calls")?.document("squad_live_room")?.update(
                        "status", "ENDED",
                        "participants", emptyList<String>()
                    )
                } else {
                    getFirestore()?.collection("group_calls")?.document("squad_live_room")?.update(
                        "participants", currentParticipants
                    )
                }
            } catch (e: Exception) {
                Log.e("GroupCallManager", "Failed to leave call: ${e.message}")
            }
        }
    }

    fun toggleMic() {
        _isMicMuted.value = !_isMicMuted.value
        agoraEngine.setMute(_isMicMuted.value)
    }

    fun toggleSpeaker() {
        val newState = !_isSpeakerOn.value
        enableSpeaker(newState)
    }

    private fun enableSpeaker(enable: Boolean) {
        _isSpeakerOn.value = enable
        agoraEngine.setSpeakerphone(enable)
        try {
            audioManager?.isSpeakerphoneOn = enable
        } catch (e: Exception) {
            Log.e("GroupCallManager", "Audio routing error: ${e.message}")
        }
    }

    private fun triggerRingVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (_: Exception) {}
    }

    // Direct phone dial option (easiest external GSM dialer)
    fun dialPhoneNumber(phoneNumber: String = "") {
        try {
            val intent = if (phoneNumber.isNotBlank()) {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}"))
            } else {
                Intent(Intent.ACTION_DIAL)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("GroupCallManager", "Cannot open dialer: ${e.message}")
        }
    }
}
