package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.call.LocalCallStatus
import com.example.data.call.SquadCallRoom
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Instagram-Style Floating Incoming Call Banner (pops up at top of screen)
 */
@Composable
fun IncomingCallNotificationBanner(
    callerName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.5.dp, CyberCyan, RoundedCornerShape(18.dp))
            .testTag("incoming_call_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(StatusGreen.copy(alpha = 0.25f))
                        .border(2.dp, StatusGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Calling",
                        tint = StatusGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(StatusGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "INCOMING SQUAD CALL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StatusGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Text(
                        text = callerName.ifBlank { "Squad Mate" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Group Voice Room • Tap to join",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Decline button
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StatusRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Accept button
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StatusGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Accept",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Fullscreen / Dialog Active Instagram/Gaming Group Call Room
 */
@Composable
fun GroupCallRoomDialog(
    room: SquadCallRoom?,
    currentUserName: String,
    isMicMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onLeaveCall: () -> Unit,
    onDialPhone: (String) -> Unit,
    onDismiss: () -> Unit,
    agoraStatus: com.example.data.call.AgoraStatus = com.example.data.call.AgoraStatus.IDLE,
    agoraStatusMessage: String = "",
    savedAgoraAppId: String = "",
    savedAgoraToken: String = "",
    savedAgoraChannel: String = "",
    onSaveAgoraConfig: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var callDurationSeconds by remember { mutableIntStateOf(0) }
    var showDirectDialDialog by remember { mutableStateOf(false) }
    var showAgoraConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    val participants = room?.participants?.ifEmpty { listOf(currentUserName) } ?: listOf(currentUserName)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBlack),
            color = CyberBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top Bar: Header, Call Timer & Agora Engine Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE SQUAD CALL • $durationText",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Agora Voice Engine Status Pill (Clickable to configure)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (agoraStatus == com.example.data.call.AgoraStatus.CONNECTED) StatusGreen.copy(alpha = 0.15f)
                                else DarkSurface
                            )
                            .border(
                                1.dp,
                                if (agoraStatus == com.example.data.call.AgoraStatus.CONNECTED) StatusGreen
                                else CyberCyan.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { showAgoraConfigDialog = true }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("agora_engine_status_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Agora Voice Engine",
                            tint = if (agoraStatus == com.example.data.call.AgoraStatus.CONNECTED) StatusGreen else CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (agoraStatus == com.example.data.call.AgoraStatus.CONNECTED) "AGORA VOICE: LIVE"
                            else if (savedAgoraAppId.isNotBlank()) "AGORA: READY / CONNECTING"
                            else "SETUP AGORA VOICE (TAP HERE)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (agoraStatus == com.example.data.call.AgoraStatus.CONNECTED) StatusGreen else CyberCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure Agora",
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "T1 ESPORTS VOICE ROOM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "${participants.size} Player${if (participants.size > 1) "s" else ""} in Call",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                // 2. Middle Grid: Participant Avatars with Voice Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (participants.size > 2) 2 else 1),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(participants) { participant ->
                            val isMe = participant == currentUserName
                            ParticipantCard(
                                name = participant,
                                isMe = isMe,
                                isMuted = if (isMe) isMicMuted else false
                            )
                        }
                    }
                }

                // 3. Audio Activity Waveform indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Activity",
                        tint = if (!isMicMuted) StatusGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isMicMuted) "Microphone Active (Speaking)" else "Microphone Muted",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (!isMicMuted) StatusGreen else TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Bottom Control Bar (Instagram-style pill)
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(32.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Button
                        IconButton(
                            onClick = onToggleMic,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMicMuted) DarkSurface else CyberCyan)
                                .border(1.dp, if (isMicMuted) StatusRed else CyberCyan, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = if (isMicMuted) StatusRed else Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Speakerphone Button
                        IconButton(
                            onClick = onToggleSpeaker,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) T1Yellow else DarkSurface)
                                .border(1.dp, if (isSpeakerOn) T1Yellow else DarkBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color.Black else TextSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Direct Phone Call Dialer Option (Easiest Direct GSM Call)
                        IconButton(
                            onClick = { showDirectDialDialog = true },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(1.dp, CyberCyan, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Direct Dial Phone",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // End Call Button (Big Red)
                        IconButton(
                            onClick = onLeaveCall,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(StatusRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDirectDialDialog) {
        DirectPhoneDialDialog(
            onDial = { number ->
                showDirectDialDialog = false
                onDialPhone(number)
            },
            onDismiss = { showDirectDialDialog = false }
        )
    }

    if (showAgoraConfigDialog) {
        AgoraConfigDialog(
            savedAppId = savedAgoraAppId,
            savedToken = savedAgoraToken,
            savedChannel = savedAgoraChannel,
            currentStatus = agoraStatusMessage,
            onSave = { appId, token, channel ->
                showAgoraConfigDialog = false
                onSaveAgoraConfig(appId, token, channel)
            },
            onDismiss = { showAgoraConfigDialog = false }
        )
    }
}

@Composable
private fun ParticipantCard(
    name: String,
    isMe: Boolean,
    isMuted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_wave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (!isMuted) StatusGreen.copy(alpha = 0.8f) else DarkBorder
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (!isMuted) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.2f))
                    .border(2.dp, if (!isMuted) StatusGreen else CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (!isMuted) StatusGreen else CyberCyan,
                    modifier = Modifier.size(46.dp)
                )

                if (isMuted) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(StatusRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Muted",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isMe) "$name (You)" else name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Text(
                text = if (!isMuted) "Speaking" else "Muted",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (!isMuted) StatusGreen else StatusRed,
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Direct Cell Phone Dialer Dialog (for quick real SIM/GSM calls)
 */
@Composable
fun DirectPhoneDialDialog(
    onDial: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Direct Phone Call",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter squad leader or player mobile number to open your phone's direct dialer:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("e.g. +91 98765 43210", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDial(phoneNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dial Now", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Agora Voice Engine Configuration Dialog
 */
@Composable
fun AgoraConfigDialog(
    savedAppId: String,
    savedToken: String = "",
    savedChannel: String = "",
    currentStatus: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var appIdInput by remember { mutableStateOf(savedAppId) }
    var tokenInput by remember { mutableStateOf(savedToken) }
    var channelInput by remember { mutableStateOf(savedChannel.ifBlank { com.example.data.call.AgoraVoiceEngine.DEFAULT_CHANNEL }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = StatusGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agora Voice Engine Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "High-definition, ultra-low latency gaming voice chat powered by Agora SDK.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Status: $currentStatus",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (currentStatus.contains("Live", ignoreCase = true) || currentStatus.contains("Ready", ignoreCase = true)) StatusGreen else CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Agora App ID:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = appIdInput,
                    onValueChange = { appIdInput = it },
                    placeholder = { Text("App ID", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusGreen,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Agora Temp Token (Optional):",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "If App Certificate is ON in Agora console, paste Temp Token. If in Testing Mode, leave blank.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    placeholder = { Text("Leave blank if Testing Mode", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusGreen,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Voice Channel Name:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = channelInput,
                    onValueChange = { channelInput = it },
                    placeholder = { Text("t1_squad_voice_channel", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusGreen,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(appIdInput, tokenInput, channelInput) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text("Save & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

