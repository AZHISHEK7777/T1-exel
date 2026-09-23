package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.SideGameMonitorDrawer
import com.example.ui.components.T1BottomNav
import com.example.ui.components.T1TopBar
import com.example.data.call.LocalCallStatus
import com.example.data.call.SquadCallNotificationHelper
import com.example.ui.dialogs.AdminPanelDialog
import com.example.ui.dialogs.BackendConfigDialog
import com.example.ui.dialogs.CallPermissionsDialog
import com.example.ui.dialogs.GroupCallRoomDialog
import com.example.ui.dialogs.IncomingCallNotificationBanner
import com.example.ui.dialogs.LauncherDialog
import com.example.ui.dialogs.OverlayPermissionDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.screens.CharacterSkillsScreen
import com.example.ui.screens.GlobalChatScreen
import com.example.ui.screens.KeyAuthScreen
import com.example.ui.screens.SensitivityScreen
import com.example.ui.screens.UidInspectorScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var pendingNotificationAction: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SquadCallNotificationHelper.createNotificationChannels(this)
        pendingNotificationAction = intent?.action

        setContent {
            MyApplicationTheme {
                T1EsportsApp(
                    initialAction = pendingNotificationAction,
                    onClearAction = { pendingNotificationAction = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.action?.let { action ->
            pendingNotificationAction = action
        }
    }
}

@Composable
fun T1EsportsApp(
    viewModel: MainViewModel = viewModel(),
    initialAction: String? = null,
    onClearAction: () -> Unit = {}
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isAdminPanelOpen by viewModel.isAdminPanelOpen.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()

    val currentTab by viewModel.currentTab.collectAsState()
    val deviceInfo = viewModel.deviceInfo
    val networkState by viewModel.networkState.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val isWithDpiMode by viewModel.isWithDpiMode.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationProgress by viewModel.generationProgress.collectAsState()
    val generationStage by viewModel.generationStage.collectAsState()
    val savedSensitivities by viewModel.savedSensitivities.collectAsState()

    val uidInput by viewModel.uidInput.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val inspectionResult by viewModel.inspectionResult.collectAsState()
    val apiEndpoint by viewModel.apiEndpoint.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()

    val installedGameStatus by viewModel.installedGameStatus.collectAsState()
    val showLauncherDialog by viewModel.showLauncherDialog.collectAsState()
    val showBackendConfigDialog by viewModel.showBackendConfigDialog.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    val callRoom by viewModel.callRoom.collectAsState()
    val callStatus by viewModel.callStatus.collectAsState()
    val isCallMicMuted by viewModel.isCallMicMuted.collectAsState()
    val isCallSpeakerOn by viewModel.isCallSpeakerOn.collectAsState()
    val agoraStatus by viewModel.agoraStatus.collectAsState()
    val agoraStatusMessage by viewModel.agoraStatusMessage.collectAsState()

    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAgoraConfigDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var isSideCpuMonitorOpen by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val recordAudioGranted = results[Manifest.permission.RECORD_AUDIO] == true
        if (recordAudioGranted) {
            viewModel.showToast("Voice Microphone Access Granted!")
        }
    }

    val checkAndRequestPermissions = {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Auto prompt permissions on launch if needed
    LaunchedEffect(Unit) {
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasMic) {
            showPermissionsDialog = true
        }
    }

    // Handle initial / incoming notification action
    LaunchedEffect(initialAction) {
        when (initialAction) {
            SquadCallNotificationHelper.ACTION_ACCEPT_CALL -> {
                viewModel.joinGroupCall()
                onClearAction()
            }
            SquadCallNotificationHelper.ACTION_DECLINE_CALL -> {
                viewModel.declineIncomingCall()
                onClearAction()
            }
            SquadCallNotificationHelper.ACTION_LEAVE_CALL -> {
                viewModel.leaveGroupCall()
                onClearAction()
            }
        }
    }

    val handleStartCallWithPermission = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            showPermissionsDialog = true
        } else {
            viewModel.startGroupCall()
        }
    }

    val handleJoinCallWithPermission = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            showPermissionsDialog = true
        } else {
            viewModel.joinGroupCall()
        }
    }

    val handleLaunchGameTrigger = {
        if (Settings.canDrawOverlays(context)) {
            isSideCpuMonitorOpen = true
        } else {
            showOverlayPermissionDialog = true
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // Admin Panel Dialog (Opened with 111 or Settings)
    if (isAdminPanelOpen) {
        AdminPanelDialog(
            authRepository = viewModel.authRepository,
            adminViewModel = viewModel.adminViewModel,
            onEnterAsAdmin = { viewModel.enterAsAdmin() },
            onDismiss = { viewModel.closeAdminPanel() }
        )
    }

    // Settings & Security Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            onOpenAdminPanel = {
                showSettingsDialog = false
                viewModel.enterAsAdmin() // Grants access and opens panel
                // Alternatively open panel directly:
            },
            onOpenBackendConfig = {
                showSettingsDialog = false
                viewModel.openBackendConfigDialog(true)
            },
            onOpenAgoraConfig = {
                showSettingsDialog = false
                showAgoraConfigDialog = true
            },
            onLogout = {
                showSettingsDialog = false
                viewModel.logout()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Agora Voice Engine Configuration Dialog
    if (showAgoraConfigDialog) {
        com.example.ui.dialogs.AgoraConfigDialog(
            savedAppId = viewModel.getAgoraAppId(),
            savedToken = viewModel.getAgoraToken(),
            savedChannel = viewModel.getAgoraChannel(),
            currentStatus = agoraStatusMessage,
            onSave = { appId, token, channel ->
                showAgoraConfigDialog = false
                viewModel.saveAgoraConfig(appId, token, channel)
            },
            onDismiss = { showAgoraConfigDialog = false }
        )
    }

    // Free Fire Launcher Dialog
    if (showLauncherDialog) {
        LauncherDialog(
            status = installedGameStatus,
            onLaunch = { edition ->
                viewModel.launchGame(edition)
                isSideCpuMonitorOpen = true
            },
            onInstall = { edition -> viewModel.openPlayStore(edition) },
            onDismiss = { viewModel.openLauncherDialog(false) }
        )
    }

    // Backend Config Dialog
    if (showBackendConfigDialog) {
        BackendConfigDialog(
            initialEndpoint = apiEndpoint,
            initialApiKey = apiKey,
            onSave = { endpoint, key ->
                viewModel.saveBackendConfig(endpoint, key)
            },
            onDismiss = { viewModel.openBackendConfigDialog(false) }
        )
    }

    // Overlay Permission Dialog
    if (showOverlayPermissionDialog) {
        OverlayPermissionDialog(
            onRequestOverlayPermission = {
                showOverlayPermissionDialog = false
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    context.startActivity(intent)
                }
                isSideCpuMonitorOpen = true
            },
            onOpenSideMonitorDirectly = {
                showOverlayPermissionDialog = false
                isSideCpuMonitorOpen = true
            },
            onDismiss = {
                showOverlayPermissionDialog = false
                isSideCpuMonitorOpen = true
            }
        )
    }

    // Call Permissions Dialog (Microphone & Ring Notifications)
    if (showPermissionsDialog) {
        CallPermissionsDialog(
            onRequestPermissions = {
                showPermissionsDialog = false
                checkAndRequestPermissions()
            },
            onDismiss = { showPermissionsDialog = false }
        )
    }

    // Active Group Call Room Screen / Dialog
    if (callStatus == LocalCallStatus.CONNECTED) {
        GroupCallRoomDialog(
            room = callRoom,
            currentUserName = viewModel.getUserName(),
            isMicMuted = isCallMicMuted,
            isSpeakerOn = isCallSpeakerOn,
            onToggleMic = { viewModel.toggleCallMic() },
            onToggleSpeaker = { viewModel.toggleCallSpeaker() },
            onLeaveCall = { viewModel.leaveGroupCall() },
            onDialPhone = { number -> viewModel.dialPhoneNumber(number) },
            onDismiss = { viewModel.leaveGroupCall() },
            agoraStatus = agoraStatus,
            agoraStatusMessage = agoraStatusMessage,
            savedAgoraAppId = viewModel.getAgoraAppId(),
            savedAgoraToken = viewModel.getAgoraToken(),
            savedAgoraChannel = viewModel.getAgoraChannel(),
            onSaveAgoraConfig = { appId, token, channel ->
                viewModel.saveAgoraConfig(appId, token, channel)
            }
        )
    }

    // Floating Incoming Group Call Banner (Instagram style)
    if (callStatus == LocalCallStatus.INCOMING) {
        IncomingCallNotificationBanner(
            callerName = callRoom?.callerName ?: "Squad Mate",
            onAccept = handleJoinCallWithPermission,
            onDecline = { viewModel.declineIncomingCall() }
        )
    }

    val pendingPinSetup by viewModel.pendingPinSetup.collectAsState()

    // 1. If not authenticated, display Key Authentication Screen (Opening / Splash)
    if (!isAuthenticated) {
        KeyAuthScreen(
            networkState = networkState,
            errorMessage = authErrorMessage,
            pendingPinSetup = pendingPinSetup,
            onKeySubmitted = { key, userName ->
                viewModel.authenticateWithKey(key, userName)
            },
            onPinLoginSubmitted = { identifier, pin ->
                viewModel.loginWithPin(identifier, pin)
            },
            onCompletePinSetup = { pin ->
                viewModel.completePinSetup(pin)
            },
            onCancelPinSetup = {
                viewModel.clearPendingPinSetup()
            }
        )
        return
    }

    // 2. Main Authenticated Interface
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack),
        topBar = {
            T1TopBar(
                networkState = networkState,
                onLaunchGameClick = handleLaunchGameTrigger,
                onSettingsClick = { showSettingsDialog = true }
            )
        },
        bottomBar = {
            T1BottomNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = CyberBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBlack)
        ) {
            Crossfade(
                targetState = currentTab,
                label = "tab_crossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> SensitivityScreen(
                        deviceInfo = deviceInfo,
                        networkState = networkState,
                        sensitivity = sensitivity,
                        isGenerating = isGenerating,
                        generationProgress = generationProgress,
                        generationStage = generationStage,
                        savedSensitivities = savedSensitivities,
                        isWithDpiMode = isWithDpiMode,
                        onGenerateClick = { viewModel.startGeneratingSensitivity() },
                        onGenerateWithDpiChoice = { withDpi -> viewModel.startGeneratingSensitivity(withDpi) },
                        onDpiModeChange = { withDpi -> viewModel.setDpiMode(withDpi) },
                        onApplyPreset = { preset -> viewModel.applyPreset(preset) },
                        onCopySettings = { viewModel.copySensitivitySettings() },
                        onSaveProfile = { title -> viewModel.saveCurrentProfile(title) },
                        onLoadProfile = { profile -> viewModel.loadSavedProfile(profile) },
                        onDeleteProfile = { id -> viewModel.deleteSavedProfile(id) }
                    )

                    1 -> CharacterSkillsScreen()

                    2 -> UidInspectorScreen(
                        uidInput = uidInput,
                        selectedRegion = selectedRegion,
                        networkState = networkState,
                        inspectionResult = inspectionResult,
                        apiEndpoint = apiEndpoint,
                        onUidChange = { viewModel.setUidInput(it) },
                        onRegionChange = { viewModel.setSelectedRegion(it) },
                        onInspectClick = { viewModel.inspectUid() },
                        onOpenConfigClick = { viewModel.openBackendConfigDialog(true) },
                        onCopyNickname = { viewModel.copyPlayerNickname(it) },
                        onCopyUid = { viewModel.copyPlayerUid(it) },
                        onOpenWeb = { viewModel.openRedxGameWeb() }
                    )

                    3 -> GlobalChatScreen(
                        userName = viewModel.getUserName(),
                        messages = chatMessages,
                        callRoom = callRoom,
                        onStartCall = handleStartCallWithPermission,
                        onJoinCall = handleJoinCallWithPermission,
                        onSendMessage = { text, uri, type ->
                            viewModel.sendChatMessage(text, uri, type)
                        }
                    )
                }
            }
        }
    }

    // Game Turbo Side Monitor Drawer (Slides in from side with live CPU, RAM, Battery, Hz monitoring)
    SideGameMonitorDrawer(
        isOpen = isSideCpuMonitorOpen,
        onClose = { isSideCpuMonitorOpen = false },
        onLaunchGame = { edition ->
            isSideCpuMonitorOpen = false
            viewModel.launchGame(edition)
        },
        onOpenOverlaySettings = {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                context.startActivity(intent)
            }
        },
        hasOverlayPermission = Settings.canDrawOverlays(context)
    )
}
