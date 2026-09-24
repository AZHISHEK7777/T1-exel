package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.local.SavedSensitivityEntity
import com.example.data.model.CharacterCombination
import com.example.data.model.CharacterSkillDatabase
import com.example.data.model.DeviceInfo
import com.example.data.model.SensitivityProfile
import com.example.data.network.UidInspectionResult
import com.example.data.network.UidInspectorRepository
import com.example.util.DeviceDetector
import com.example.util.FreeFireEdition
import com.example.util.GameLauncher
import com.example.util.InstalledGameStatus
import com.example.util.NetworkMonitor
import com.example.util.NetworkState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SensitivityField {
    GENERAL,
    RED_DOT,
    SCOPE_2X,
    SCOPE_4X,
    SNIPER_SCOPE,
    FREE_LOOK
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val database = DatabaseProvider.getDatabase(context)
    private val sensitivityDao = database.sensitivityDao()
    private val uidRepository = UidInspectorRepository(context)
    private val networkMonitor = NetworkMonitor(context)
    val authRepository = com.example.data.auth.AuthRepository(context)
    val globalChatRepository = com.example.data.chat.GlobalChatRepository(context)
    val adminViewModel by lazy { AdminViewModel(application, authRepository) }
    val groupCallManager = com.example.data.call.GroupCallManager(context)
    val agoraEngine = groupCallManager.agoraEngine
    val agoraStatus = agoraEngine.status
    val agoraStatusMessage = agoraEngine.statusMessage

    fun getAgoraAppId(): String = agoraEngine.getSavedAppId()
    fun getAgoraToken(): String = agoraEngine.getSavedToken()
    fun getAgoraChannel(): String = agoraEngine.getSavedChannel()
    fun saveAgoraConfig(appId: String, token: String = "", channel: String = "") = 
        agoraEngine.saveConfig(appId, token, channel)
    fun saveAgoraAppId(appId: String) = agoraEngine.saveConfig(appId, agoraEngine.getSavedToken(), agoraEngine.getSavedChannel())

    // Call state
    val callRoom = groupCallManager.currentRoom
    val callStatus = groupCallManager.localStatus
    val isCallMicMuted = groupCallManager.isMicMuted
    val isCallSpeakerOn = groupCallManager.isSpeakerOn

    fun startGroupCall() {
        val userName = getUserName()
        groupCallManager.startGroupCall(userName)
        sendChatMessage("📞 Squad Voice Call started by $userName! Tap Join to talk.", null, "CALL_INVITE")
    }

    fun joinGroupCall() {
        groupCallManager.joinCall(getUserName())
    }

    fun declineIncomingCall() {
        groupCallManager.declineIncomingCall()
    }

    fun leaveGroupCall() {
        groupCallManager.leaveCall()
    }

    fun toggleCallMic() {
        groupCallManager.toggleMic()
    }

    fun toggleCallSpeaker() {
        groupCallManager.toggleSpeaker()
    }

    fun dialPhoneNumber(number: String) {
        groupCallManager.dialPhoneNumber(number)
    }

    // Device Info
    val deviceInfo: DeviceInfo = DeviceDetector.getDeviceInfo(context)

    // Live Network State
    val networkState: StateFlow<NetworkState> = networkMonitor.networkState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkMonitor.checkCurrentState())

    // Authentication State
    private val _isAuthenticated = MutableStateFlow<Boolean>(authRepository.isUserLoggedIn())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isAdminPanelOpen = MutableStateFlow(false)
    val isAdminPanelOpen: StateFlow<Boolean> = _isAdminPanelOpen.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    // Global Chat State
    val chatMessages: StateFlow<List<com.example.data.local.ChatMessageEntity>> = globalChatRepository.getMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            globalChatRepository.incomingNotification.collect { incoming ->
                if (!incoming.isFromMe && _currentTab.value != 3) {
                    showToast("💬 ${incoming.senderName}: ${incoming.messageText.take(45)}")
                }
            }
        }
    }

    fun getUserName(): String = authRepository.getUserName()

    fun sendChatMessage(text: String, uri: String?, type: String) {
        viewModelScope.launch {
            globalChatRepository.sendMessage(
                senderName = authRepository.getUserName(),
                messageText = text,
                senderRole = authRepository.getUserTier().name,
                attachmentUri = uri,
                attachmentType = type
            )
        }
    }

    fun clearAllChat(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val success = globalChatRepository.clearAllChat()
            if (success) {
                showToast("🧹 All community chat messages cleared!")
            } else {
                showToast("⚠️ Chat cleared locally.")
            }
            onComplete?.invoke(success)
        }
    }

    // Validated Key state waiting for Google or Guest login selection
    private val _validatedKeyInfo = MutableStateFlow<com.example.data.auth.KeyValidationResult.KeyVerified?>(null)
    val validatedKeyInfo: StateFlow<com.example.data.auth.KeyValidationResult.KeyVerified?> = _validatedKeyInfo.asStateFlow()

    fun clearValidatedKeyInfo() {
        _validatedKeyInfo.value = null
    }

    /**
     * Step 1: Verify Key
     * - If Admin Passcode ("111" or "ABHISHEK-ADMIN-999" or ADMIN tier):
     *   Directly logs into ADMIN PANEL instantly! Does not enter normal app.
     * - If normal key: Sets validatedKeyInfo for Google or Guest login.
     */
    fun submitKey(key: String) {
        val trimmedKey = key.trim()
        if (trimmedKey.isBlank()) {
            _authErrorMessage.value = "Please enter an Access Key."
            return
        }

        when (val result = authRepository.validateKeyOnly(trimmedKey)) {
            is com.example.data.auth.KeyValidationResult.AdminInstantLogin -> {
                _authErrorMessage.value = null
                _validatedKeyInfo.value = null
                loginDirectToAdminPanel()
            }
            is com.example.data.auth.KeyValidationResult.KeyVerified -> {
                _authErrorMessage.value = null
                _validatedKeyInfo.value = result
            }
            is com.example.data.auth.KeyValidationResult.Error -> {
                _authErrorMessage.value = result.message
            }
            else -> {}
        }
    }

    /**
     * Instant Admin Login via 111:
     * Directly opens the Admin Panel Dialog without entering normal app!
     */
    fun loginDirectToAdminPanel() {
        authRepository.setLoggedIn(
            com.example.data.auth.AuthRepository.MASTER_KEY,
            "Abhishek (Admin)",
            com.example.data.auth.KeyTier.ADMIN,
            "1111"
        )
        _isAuthenticated.value = true
        _isAdminPanelOpen.value = true
        _authErrorMessage.value = null
        _validatedKeyInfo.value = null
        showToast("👑 Admin 111 Verified — Opened Admin Panel!")
    }

    /**
     * Direct Google Sign In from the Main Key Screen:
     * - If this Google account (email) is already registered -> Instant Login!
     * - If NOT registered -> Shows error: "User not registered! Please enter a valid VIP Key."
     */
    fun directGoogleLogin(email: String) {
        val cleanEmail = email.trim().lowercase(java.util.Locale.ROOT)
        if (cleanEmail.isBlank()) {
            _authErrorMessage.value = "No Google account selected."
            return
        }

        val registered = authRepository.getRegisteredGoogleUser(cleanEmail)
        if (registered != null) {
            authRepository.loginRegisteredGoogleUser(cleanEmail)
            _authErrorMessage.value = null
            _validatedKeyInfo.value = null
            _isAuthenticated.value = true
            showToast("Welcome back, ${registered.username}!")
        } else {
            _authErrorMessage.value = "User not registered! Please enter a valid VIP Key to register first."
            showToast("User not registered! Please enter a VIP Key first.")
        }
    }

    /**
     * Register new Google User with Key & Username:
     * Saves Gmail, chosen username, key & tier.
     * Redirects directly into app!
     */
    fun registerWithGoogle(email: String, username: String, key: String, tier: com.example.data.auth.KeyTier) {
        val cleanEmail = email.trim().lowercase(java.util.Locale.ROOT)
        val cleanUser = username.trim().ifBlank { cleanEmail.substringBefore("@").ifBlank { "Pro Player" } }

        authRepository.registerGoogleUser(
            email = cleanEmail,
            username = cleanUser,
            key = key,
            tier = tier
        )
        _validatedKeyInfo.value = null
        _authErrorMessage.value = null
        _isAuthenticated.value = true
        showToast("Registration Complete! Welcome, $cleanUser!")
    }

    /**
     * Step 2A: Complete login with Google
     */
    fun loginWithGoogle(key: String, userName: String, tier: com.example.data.auth.KeyTier) {
        val cleanName = userName.trim().ifBlank { "Google User" }
        val success = authRepository.activateKey(
            key = key,
            userName = cleanName,
            tier = tier,
            loginMethod = "Google"
        )
        if (success) {
            _validatedKeyInfo.value = null
            _authErrorMessage.value = null
            _isAuthenticated.value = true
            showToast("Logged in with Google: $cleanName")
        } else {
            _authErrorMessage.value = "Failed to activate key with Google account."
        }
    }

    /**
     * Step 2B: Complete login as Guest
     */
    fun loginAsGuest(key: String, guestName: String, tier: com.example.data.auth.KeyTier) {
        val cleanName = guestName.trim().ifBlank { "Guest_${(100..999).random()}" }
        val success = authRepository.activateKey(
            key = key,
            userName = cleanName,
            tier = tier,
            loginMethod = "Guest"
        )
        if (success) {
            _validatedKeyInfo.value = null
            _authErrorMessage.value = null
            _isAuthenticated.value = true
            showToast("Welcome Guest Player: $cleanName")
        } else {
            _authErrorMessage.value = "Failed to activate key as Guest."
        }
    }

    // PIN Setup State (Deprecated - kept for safe legacy compatibility)
    private val _pendingPinSetup = MutableStateFlow<com.example.data.auth.KeyValidationResult.RequirePinSetup?>(null)
    val pendingPinSetup: StateFlow<com.example.data.auth.KeyValidationResult.RequirePinSetup?> = _pendingPinSetup.asStateFlow()

    fun clearPendingPinSetup() {
        _pendingPinSetup.value = null
    }

    fun completePinSetup(pin: String) {
        val pending = _pendingPinSetup.value ?: return
        loginAsGuest(pending.key, pending.userName, pending.tier)
    }

    fun loginWithPin(identifier: String, pin: String) {
        submitKey(identifier)
    }

    fun authenticateWithKey(key: String, userName: String = "") {
        submitKey(key)
    }

    fun enterAsAdmin() {
        authRepository.setLoggedIn(
            com.example.data.auth.AuthRepository.MASTER_KEY,
            "Abhishek (Admin)",
            com.example.data.auth.KeyTier.ADMIN,
            "1111"
        )
        _isAuthenticated.value = true
        _isAdminPanelOpen.value = false
        showToast("Admin access granted — Welcome Abhishek!")
    }

    fun closeAdminPanel() {
        _isAdminPanelOpen.value = false
    }

    fun logout() {
        authRepository.logout()
        _isAuthenticated.value = false
        _authErrorMessage.value = null
        showToast("Logged out successfully.")
    }

    // Navigation Tab (0: Sensitivity, 1: Skills, 2: UID Inspector)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Sensitivity State (Realistic Esports Scale)
    private val _isWithDpiMode = MutableStateFlow(false)
    val isWithDpiMode: StateFlow<Boolean> = _isWithDpiMode.asStateFlow()

    private val _sensitivity = MutableStateFlow(
        SensitivityProfile.calculateForDevice(
            refreshRateHz = deviceInfo.refreshRateHz,
            screenDpi = deviceInfo.screenDpi,
            ramGb = deviceInfo.totalRamGb.toInt(),
            manufacturer = deviceInfo.manufacturer,
            model = deviceInfo.model,
            hardware = deviceInfo.hardware,
            withDpi = false
        )
    )
    val sensitivity: StateFlow<SensitivityProfile> = _sensitivity.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationProgress = MutableStateFlow(0f)
    val generationProgress: StateFlow<Float> = _generationProgress.asStateFlow()

    private val _generationStage = MutableStateFlow("")
    val generationStage: StateFlow<String> = _generationStage.asStateFlow()

    val savedSensitivities: StateFlow<List<SavedSensitivityEntity>> = sensitivityDao.getAllSensitivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Character Skills State
    val allRoleCombinations = CharacterSkillDatabase.combinations
    private val _selectedRole = MutableStateFlow(CharacterSkillDatabase.combinations[0])
    val selectedRole: StateFlow<CharacterCombination> = _selectedRole.asStateFlow()

    fun selectRole(roleId: String) {
        val found = allRoleCombinations.find { it.roleId == roleId }
        if (found != null) {
            _selectedRole.value = found
        }
    }

    // UID Inspector State
    private val _uidInput = MutableStateFlow("")
    val uidInput: StateFlow<String> = _uidInput.asStateFlow()

    private val _selectedRegion = MutableStateFlow("IND")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _inspectionResult = MutableStateFlow<UidInspectionResult>(UidInspectionResult.Idle)
    val inspectionResult: StateFlow<UidInspectionResult> = _inspectionResult.asStateFlow()

    private val _apiEndpoint = MutableStateFlow(uidRepository.configuredEndpoint)
    val apiEndpoint: StateFlow<String> = _apiEndpoint.asStateFlow()

    private val _apiKey = MutableStateFlow(uidRepository.configuredApiKey)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    // Game Launcher State
    private val _installedGameStatus = MutableStateFlow(GameLauncher.getInstalledStatus(context))
    val installedGameStatus: StateFlow<InstalledGameStatus> = _installedGameStatus.asStateFlow()

    private val _showLauncherDialog = MutableStateFlow(false)
    val showLauncherDialog: StateFlow<Boolean> = _showLauncherDialog.asStateFlow()

    private val _showBackendConfigDialog = MutableStateFlow(false)
    val showBackendConfigDialog: StateFlow<Boolean> = _showBackendConfigDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun setUidInput(value: String) {
        _uidInput.value = value.filter { it.isDigit() }.take(15)
    }

    fun setSelectedRegion(region: String) {
        _selectedRegion.value = region
    }

    fun updateSlider(field: SensitivityField, value: Int) {
        val clamped = value.coerceIn(0, 200)
        val current = _sensitivity.value
        _sensitivity.value = when (field) {
            SensitivityField.GENERAL -> current.copy(general = clamped)
            SensitivityField.RED_DOT -> current.copy(redDot = clamped)
            SensitivityField.SCOPE_2X -> current.copy(scope2x = clamped)
            SensitivityField.SCOPE_4X -> current.copy(scope4x = clamped)
            SensitivityField.SNIPER_SCOPE -> current.copy(sniperScope = clamped)
            SensitivityField.FREE_LOOK -> current.copy(freeLook = clamped)
        }
    }

    fun applyPreset(preset: SensitivityProfile) {
        _sensitivity.value = preset
        showToast("Preset '${preset.presetName}' applied!")
    }

    fun setDpiMode(withDpi: Boolean) {
        _isWithDpiMode.value = withDpi
        val calculated = SensitivityProfile.calculateForDevice(
            refreshRateHz = deviceInfo.refreshRateHz,
            screenDpi = deviceInfo.screenDpi,
            ramGb = deviceInfo.totalRamGb.toInt(),
            manufacturer = deviceInfo.manufacturer,
            model = deviceInfo.model,
            hardware = deviceInfo.hardware,
            withDpi = withDpi
        )
        _sensitivity.value = calculated
        val modeText = if (withDpi) "WITH DPI" else "NON-DPI"
        showToast("Switched to $modeText configuration!")
    }

    fun startGeneratingSensitivity(withDpi: Boolean = _isWithDpiMode.value) {
        if (_isGenerating.value) return
        _isWithDpiMode.value = withDpi
        viewModelScope.launch {
            _isGenerating.value = true
            val modeText = if (withDpi) "WITH DPI" else "NON-DPI"
            _generationProgress.value = 0.08f
            _generationStage.value = "🔍 Auto-detecting ${deviceInfo.fullDeviceName} touch digitizer..."
            delay(400)

            _generationProgress.value = 0.32f
            _generationStage.value = if (withDpi)
                "⚡ Calculating boosted DPI & drag curve multipliers..."
            else
                "🛡️ Tuning factory screen density & natural swipe drag..."
            delay(450)

            _generationProgress.value = 0.65f
            _generationStage.value = "📊 Locking fixed sensitivity profile for ${deviceInfo.model.ifBlank { deviceInfo.manufacturer }}..."
            delay(450)

            _generationProgress.value = 0.90f
            _generationStage.value = "🎯 Finalizing Abhishek's Esports Headshot Calibration ($modeText)..."
            delay(400)

            _generationProgress.value = 1.0f
            _generationStage.value = "Optimal $modeText Sensi Calibrated!"

            val calculated = SensitivityProfile.calculateForDevice(
                refreshRateHz = deviceInfo.refreshRateHz,
                screenDpi = deviceInfo.screenDpi,
                ramGb = deviceInfo.totalRamGb.toInt(),
                manufacturer = deviceInfo.manufacturer,
                model = deviceInfo.model,
                hardware = deviceInfo.hardware,
                withDpi = withDpi
            )
            _sensitivity.value = calculated
            delay(350)
            _isGenerating.value = false
            showToast("Fixed $modeText Sensi calibrated for ${deviceInfo.fullDeviceName}!")
        }
    }

    fun copySensitivitySettings() {
        val s = _sensitivity.value
        val text = buildString {
            appendLine("=== T1 ESPORTS SENSITIVITY PROFILE ===")
            appendLine("Device: ${deviceInfo.fullDeviceName} (${deviceInfo.refreshRateHz}Hz)")
            appendLine("General: ${s.general}")
            appendLine("Red Dot: ${s.redDot}")
            appendLine("2x Scope: ${s.scope2x}")
            appendLine("4x Scope: ${s.scope4x}")
            appendLine("Sniper Scope: ${s.sniperScope}")
            appendLine("Free Look: ${s.freeLook}")
            appendLine("Recommended DPI: ${s.recommendedDpi}")
            appendLine("Fire Button Size: ${s.fireButtonSize}%")
            appendLine("Generated by T1 ESPORTS Utility")
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("T1 Sensitivity", text)
        clipboard.setPrimaryClip(clip)
        showToast("Sensitivities copied to clipboard!")
    }

    fun copyPlayerNickname(nickname: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Free Fire Nickname", nickname)
        clipboard.setPrimaryClip(clip)
        showToast("Player nickname '$nickname' copied to clipboard!")
    }

    fun copyPlayerUid(uid: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Free Fire UID", uid)
        clipboard.setPrimaryClip(clip)
        showToast("Player UID '$uid' copied to clipboard!")
    }

    fun openRedxGameWeb() {
        try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.freefiremania.com.br/free-fire-id-check.html")
            ).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            showToast("Could not open browser for freefiremania.com.br")
        }
    }

    fun saveCurrentProfile(title: String) {
        val s = _sensitivity.value
        viewModelScope.launch {
            val entity = SavedSensitivityEntity(
                title = if (title.isBlank()) "Profile ${System.currentTimeMillis() % 1000}" else title,
                general = s.general,
                redDot = s.redDot,
                scope2x = s.scope2x,
                scope4x = s.scope4x,
                sniperScope = s.sniperScope,
                freeLook = s.freeLook,
                dpi = s.recommendedDpi,
                buttonSize = s.fireButtonSize
            )
            sensitivityDao.insertSensitivity(entity)
            showToast("Saved profile '${entity.title}'")
        }
    }

    fun loadSavedProfile(entity: SavedSensitivityEntity) {
        _sensitivity.value = SensitivityProfile(
            general = entity.general,
            redDot = entity.redDot,
            scope2x = entity.scope2x,
            scope4x = entity.scope4x,
            sniperScope = entity.sniperScope,
            freeLook = entity.freeLook,
            recommendedDpi = entity.dpi,
            fireButtonSize = entity.buttonSize,
            presetName = entity.title
        )
        showToast("Loaded '${entity.title}'")
    }

    fun deleteSavedProfile(id: Long) {
        viewModelScope.launch {
            sensitivityDao.deleteSensitivity(id)
            showToast("Deleted profile")
        }
    }

    fun inspectUid() {
        val uid = _uidInput.value.trim()
        if (uid.length < 6) {
            showToast("Please enter a valid player UID (at least 6 digits)")
            return
        }

        viewModelScope.launch {
            _inspectionResult.value = UidInspectionResult.Loading
            val isOnline = networkState.value.isConnected
            val result = uidRepository.inspectUid(
                uid = uid,
                region = _selectedRegion.value,
                isOnline = isOnline
            )
            _inspectionResult.value = result
        }
    }

    fun openBackendConfigDialog(show: Boolean) {
        _showBackendConfigDialog.value = show
    }

    fun saveBackendConfig(endpoint: String, apiKey: String) {
        uidRepository.configuredEndpoint = endpoint
        uidRepository.configuredApiKey = apiKey
        _apiEndpoint.value = endpoint
        _apiKey.value = apiKey
        _showBackendConfigDialog.value = false
        showToast("Backend configuration saved")
    }

    fun openLauncherDialog(show: Boolean) {
        refreshGameStatus()
        _showLauncherDialog.value = show
    }

    fun refreshGameStatus() {
        _installedGameStatus.value = GameLauncher.getInstalledStatus(context)
    }

    fun launchGame(edition: FreeFireEdition) {
        val success = GameLauncher.launchGame(context, edition)
        if (!success) {
            showToast("${edition.title} is not installed on this device.")
            GameLauncher.openPlayStore(context, edition.packageName)
        }
        _showLauncherDialog.value = false
    }

    fun openPlayStore(edition: FreeFireEdition) {
        GameLauncher.openPlayStore(context, edition.packageName)
    }
}
