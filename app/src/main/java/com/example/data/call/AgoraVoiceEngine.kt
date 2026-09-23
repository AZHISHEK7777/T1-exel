package com.example.data.call

import android.content.Context
import android.util.Log
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AgoraStatus {
    IDLE,
    CONFIGURING,
    JOINING,
    CONNECTED,
    ERROR
}

class AgoraVoiceEngine(private val context: Context) {

    companion object {
        private const val TAG = "AgoraVoiceEngine"
        private const val PREFS_NAME = "agora_voice_prefs"
        private const val KEY_APP_ID = "agora_app_id"
        private const val KEY_TOKEN = "agora_token"
        private const val KEY_CHANNEL = "agora_channel_name"
        const val DEFAULT_CHANNEL = "t1_squad_voice_channel"
        const val DEFAULT_APP_ID = "06bbadabada8496c990481fb881b38b5"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var rtcEngine: RtcEngine? = null

    private val _status = MutableStateFlow(AgoraStatus.IDLE)
    val status: StateFlow<AgoraStatus> = _status.asStateFlow()

    private val _statusMessage = MutableStateFlow("Agora Voice Ready")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _activeRemoteUsers = MutableStateFlow<Set<Int>>(emptySet())
    val activeRemoteUsers: StateFlow<Set<Int>> = _activeRemoteUsers.asStateFlow()

    private val _speakingUsers = MutableStateFlow<Set<Int>>(emptySet())
    val speakingUsers: StateFlow<Set<Int>> = _speakingUsers.asStateFlow()

    private val _isEngineInitialized = MutableStateFlow(false)
    val isEngineInitialized: StateFlow<Boolean> = _isEngineInitialized.asStateFlow()

    private var currentMyUid: Int = 0

    fun getSavedAppId(): String {
        val saved = prefs.getString(KEY_APP_ID, "") ?: ""
        return if (saved.isNotBlank()) saved else DEFAULT_APP_ID
    }

    fun getSavedToken(): String {
        return prefs.getString(KEY_TOKEN, "") ?: ""
    }

    fun getSavedChannel(): String {
        val saved = prefs.getString(KEY_CHANNEL, "") ?: ""
        return if (saved.isNotBlank()) saved else DEFAULT_CHANNEL
    }

    fun saveConfig(appId: String, token: String = "", channel: String = DEFAULT_CHANNEL) {
        prefs.edit()
            .putString(KEY_APP_ID, appId.trim())
            .putString(KEY_TOKEN, token.trim())
            .putString(KEY_CHANNEL, channel.trim().ifBlank { DEFAULT_CHANNEL })
            .apply()

        // Reset and re-initialize with new credentials
        destroyEngine()
        if (appId.isNotBlank()) {
            initEngine(appId.trim())
        }
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "Successfully joined Agora voice channel: $channel with UID: $uid")
            currentMyUid = uid
            _status.value = AgoraStatus.CONNECTED
            _statusMessage.value = "Voice Live (Channel: $channel | Your UID: $uid)"
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Teammate joined Agora room: $uid")
            _activeRemoteUsers.value = _activeRemoteUsers.value + uid
            _statusMessage.value = "Teammate Connected (UID: $uid)"
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Teammate left Agora room: $uid (reason: $reason)")
            _activeRemoteUsers.value = _activeRemoteUsers.value - uid
            _speakingUsers.value = _speakingUsers.value - uid
        }

        override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
            if (speakers == null) return
            val currentSpeakers = mutableSetOf<Int>()
            for (speaker in speakers) {
                if (speaker.volume > 5) {
                    val id = if (speaker.uid == 0) currentMyUid else speaker.uid
                    currentSpeakers.add(id)
                }
            }
            _speakingUsers.value = currentSpeakers
        }

        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "Remote audio state changed for UID: $uid, state: $state, reason: $reason")
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora RTC Error code: $err")
            _status.value = AgoraStatus.ERROR
            val explanation = when (err) {
                110 -> "Agora Error 110: Token Required! In console.agora.io project settings, switch to 'Testing Mode' (No Certificate) or paste Temp Token."
                109 -> "Agora Error 109: Token Expired! Please generate a fresh token from Agora console."
                101 -> "Agora Error 101: Invalid App ID or channel format."
                17 -> "Agora Error 17: Request to join rejected (Already in channel)."
                else -> "Agora Engine Code: $err"
            }
            _statusMessage.value = explanation
        }
    }

    fun initEngine(appId: String = getSavedAppId()): Boolean {
        val cleanAppId = appId.trim()
        if (cleanAppId.isBlank()) {
            _status.value = AgoraStatus.IDLE
            _statusMessage.value = "No Agora App ID configured"
            _isEngineInitialized.value = false
            return false
        }

        if (rtcEngine != null) {
            _isEngineInitialized.value = true
            return true
        }

        return try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = cleanAppId
                mEventHandler = rtcEventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING
            }

            rtcEngine = RtcEngine.create(config).apply {
                enableAudio()
                enableLocalAudio(true)
                muteLocalAudioStream(false)
                muteAllRemoteAudioStreams(false)
                adjustRecordingSignalVolume(100)
                adjustPlaybackSignalVolume(100)
                setDefaultAudioRoutetoSpeakerphone(true)
                setEnableSpeakerphone(true)
                enableAudioVolumeIndication(200, 3, true)
            }
            _isEngineInitialized.value = true
            _status.value = AgoraStatus.IDLE
            _statusMessage.value = "Agora Engine Ready"
            Log.i(TAG, "Agora Voice Engine initialized with App ID: $cleanAppId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora RTC: ${e.message}", e)
            _status.value = AgoraStatus.ERROR
            _statusMessage.value = "Agora Init Failed: ${e.message}"
            _isEngineInitialized.value = false
            false
        }
    }

    fun joinChannel(
        channelName: String = getSavedChannel(),
        token: String = getSavedToken(),
        uid: Int = 0
    ) {
        val isReady = initEngine()
        if (!isReady || rtcEngine == null) {
            Log.w(TAG, "Cannot join Agora channel: Engine not ready")
            return
        }

        _status.value = AgoraStatus.JOINING
        _statusMessage.value = "Connecting to Voice Channel..."

        try {
            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                autoSubscribeAudio = true
                publishMicrophoneTrack = true
            }

            // Assign unique positive integer UID so separate devices never clash
            val assignedUid = if (uid > 0) uid else (10000..99999).random()
            currentMyUid = assignedUid

            rtcEngine?.enableAudio()
            rtcEngine?.enableLocalAudio(true)
            rtcEngine?.muteLocalAudioStream(false)
            rtcEngine?.muteAllRemoteAudioStreams(false)
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
            rtcEngine?.setEnableSpeakerphone(true)

            val cleanToken = if (token.isNotBlank()) token.trim() else null
            val targetChannel = channelName.ifBlank { DEFAULT_CHANNEL }

            val res = rtcEngine?.joinChannel(cleanToken, targetChannel, assignedUid, options)
            Log.i(TAG, "rtcEngine.joinChannel invoked: result=$res, channel=$targetChannel, uid=$assignedUid")
        } catch (e: Exception) {
            Log.e(TAG, "Exception joining Agora channel: ${e.message}", e)
            _status.value = AgoraStatus.ERROR
            _statusMessage.value = "Failed to connect: ${e.message}"
        }
    }

    fun leaveChannel() {
        try {
            rtcEngine?.leaveChannel()
            _status.value = AgoraStatus.IDLE
            _statusMessage.value = "Call Disconnected"
            _activeRemoteUsers.value = emptySet()
            _speakingUsers.value = emptySet()
        } catch (e: Exception) {
            Log.e(TAG, "Exception leaving Agora channel: ${e.message}", e)
        }
    }

    fun setMute(isMuted: Boolean) {
        try {
            rtcEngine?.muteLocalAudioStream(isMuted)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Agora mic mute: ${e.message}")
        }
    }

    fun setSpeakerphone(isSpeaker: Boolean) {
        try {
            rtcEngine?.setEnableSpeakerphone(isSpeaker)
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(isSpeaker)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting Agora speakerphone: ${e.message}")
        }
    }

    fun destroyEngine() {
        try {
            leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
            _isEngineInitialized.value = false
            _status.value = AgoraStatus.IDLE
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying Agora Engine: ${e.message}")
        }
    }
}
