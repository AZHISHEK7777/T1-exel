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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
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
import com.example.data.call.SquadCallRoom
import kotlinx.coroutines.delay

// Authentic WhatsApp Calling Colors
private val WhatsAppDarkBg = Color(0xFF0B141B)
private val WhatsAppDarkSecondary = Color(0xFF111B21)
private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppTeal = Color(0xFF00A884)
private val WhatsAppRed = Color(0xFFEA0038)
private val WhatsAppCardBg = Color(0xFF1F2C34)
private val WhatsAppBorder = Color(0xFF2A3942)
private val WhatsAppText = Color(0xFFE9EDEF)
private val WhatsAppSubtext = Color(0xFF8696A0)

/**
 * WhatsApp-Style Incoming Call Banner (Pops up at top of screen with ring vibration)
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
            .padding(14.dp)
            .border(1.dp, WhatsAppBorder, RoundedCornerShape(18.dp))
            .testTag("incoming_call_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsAppCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
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
                        .size(48.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(WhatsAppGreen.copy(alpha = 0.2f))
                        .border(2.dp, WhatsAppGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Calling",
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WHATSAPP AUDIO CALL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WhatsAppGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Text(
                        text = callerName.ifBlank { "Squad Mate" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = WhatsAppText,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Ringing...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WhatsAppSubtext,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // WhatsApp Red Decline button
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WhatsAppRed)
                        .testTag("decline_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline Call",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // WhatsApp Green Accept button
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                        .testTag("accept_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Accept Call",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * WhatsApp Minimized Sticky Top Banner (When user minimizes active call)
 */
@Composable
fun WhatsAppMinimizedCallBanner(
    durationText: String,
    onExpand: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WhatsAppTeal,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpand() }
            .testTag("whatsapp_minimized_call_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WhatsApp Voice Call",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Tap to return to call • $durationText",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WhatsAppRed)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Authentic WhatsApp Voice Call Screen
 * - Zero configuration: users never paste App ID, Token, or Channel
 * - Direct tap to call & connects immediately
 * - Authentic WhatsApp voice call interface
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
    onSaveAgoraConfig: (String, String, String) -> Unit = { _, _, _ -> },
    onMinimize: () -> Unit = {}
) {
    var callDurationSeconds by remember { mutableIntStateOf(0) }
    var showDirectDialDialog by remember { mutableStateOf(false) }

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
    val isMultiUser = participants.size > 1
    val isWaitingForOthers = participants.size <= 1

    val infiniteTransition = rememberInfiniteTransition(label = "whatsapp_call_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(WhatsAppDarkBg, WhatsAppDarkSecondary, WhatsAppDarkBg)
                    )
                )
                .testTag("whatsapp_call_screen"),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. WhatsApp Top Bar: Minimize Button + Encryption Tag + Add Participant
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize chevron
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WhatsAppCardBg.copy(alpha = 0.6f))
                            .testTag("minimize_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Encryption Lock Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(WhatsAppCardBg.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = WhatsAppSubtext,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "End-to-end encrypted",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WhatsAppSubtext,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    // Add Squad Mate Button
                    IconButton(
                        onClick = { showDirectDialDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WhatsAppCardBg.copy(alpha = 0.6f))
                            .testTag("add_participant_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Participant",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2. Center Content: WhatsApp Avatar or WhatsApp Group Grid
                if (!isMultiUser) {
                    // Single User / Direct Calling View (Just like WhatsApp Voice Call)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Outer glowing waves
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(if (isWaitingForOthers) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(WhatsAppTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(116.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppTeal.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Main Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppBorder)
                                        .border(2.dp, WhatsAppTeal, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = WhatsAppText,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Contact Name
                        Text(
                            text = room?.callerName?.ifBlank { "Squad Voice Call" } ?: "Squad Voice Call",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = WhatsAppText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status: "Ringing..." or Call Duration
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isWaitingForOthers) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ringing...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = WhatsAppSubtext,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = WhatsAppSubtext,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // WhatsApp Group Call Mode: 2-column participant tiles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Squad Voice Call • $durationText",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = WhatsAppText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(participants) { participant ->
                                val isMe = participant == currentUserName
                                WhatsAppParticipantTile(
                                    name = participant,
                                    isMe = isMe,
                                    isMuted = if (isMe) isMicMuted else false
                                )
                            }
                        }
                    }
                }

                // 3. Audio Activity indicator pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(WhatsAppCardBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (!isMicMuted) Icons.Default.GraphicEq else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = if (!isMicMuted) WhatsAppGreen else WhatsAppRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!isMicMuted) "Microphone On • Two-way audio live" else "Microphone Muted",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (!isMicMuted) WhatsAppGreen else WhatsAppRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. WhatsApp Bottom Action Bar (Floating Rounded Dock)
                Surface(
                    color = WhatsAppCardBg,
                    shape = RoundedCornerShape(36.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speaker Toggle Button
                        IconButton(
                            onClick = onToggleSpeaker,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) WhatsAppGreen else Color(0xFF2A3942))
                                .testTag("whatsapp_speaker_btn")
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Microphone Mute Button
                        IconButton(
                            onClick = onToggleMic,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isMicMuted) WhatsAppRed else Color(0xFF2A3942))
                                .testTag("whatsapp_mic_btn")
                        ) {
                            Icon(
                                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Direct GSM Phone Dialer (Emergency SIM Call fallback)
                        IconButton(
                            onClick = { showDirectDialDialog = true },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A3942))
                                .testTag("whatsapp_gsm_dialer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Direct Dial",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // WhatsApp Iconic Red Circular End Call Button
                        IconButton(
                            onClick = onLeaveCall,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(WhatsAppRed)
                                .testTag("whatsapp_end_call_btn")
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
}

/**
 * WhatsApp-style participant tile in multi-user group voice call
 */
@Composable
private fun WhatsAppParticipantTile(
    name: String,
    isMe: Boolean,
    isMuted: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsAppCardBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!isMuted) WhatsAppTeal.copy(alpha = 0.6f) else WhatsAppBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(WhatsAppBorder)
                    .border(1.5.dp, if (!isMuted) WhatsAppGreen else WhatsAppBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = WhatsAppText,
                    modifier = Modifier.size(34.dp)
                )

                if (isMuted) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(WhatsAppRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Muted",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isMe) "$name (You)" else name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = WhatsAppText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                maxLines = 1
            )

            Text(
                text = if (!isMuted) "Speaking" else "Muted",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (!isMuted) WhatsAppGreen else WhatsAppRed,
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
                Icon(Icons.Default.Phone, contentDescription = null, tint = WhatsAppGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Direct SIM Call",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = WhatsAppText,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter player mobile number to open your phone's direct dialer:",
                    style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("e.g. +91 98765 43210", color = WhatsAppSubtext) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppGreen,
                        unfocusedBorderColor = WhatsAppBorder,
                        focusedTextColor = WhatsAppText,
                        unfocusedTextColor = WhatsAppText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDial(phoneNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dial Now", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppCardBg)
            ) {
                Text("Cancel", color = WhatsAppSubtext)
            }
        },
        containerColor = WhatsAppDarkSecondary,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Agora Voice Engine Configuration Dialog (Optional Advanced Setup)
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
                Icon(Icons.Default.Call, contentDescription = null, tint = WhatsAppGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice Calling Server Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = WhatsAppText,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "App is already pre-configured to connect automatically just like WhatsApp. Custom settings are optional.",
                    style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Agora App ID:",
                    style = MaterialTheme.typography.labelSmall.copy(color = WhatsAppText, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = appIdInput,
                    onValueChange = { appIdInput = it },
                    placeholder = { Text("Default pre-configured", color = WhatsAppSubtext) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppGreen,
                        unfocusedBorderColor = WhatsAppBorder,
                        focusedTextColor = WhatsAppText,
                        unfocusedTextColor = WhatsAppText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Voice Channel Name:",
                    style = MaterialTheme.typography.labelSmall.copy(color = WhatsAppText, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = channelInput,
                    onValueChange = { channelInput = it },
                    placeholder = { Text("t1_squad_voice_channel", color = WhatsAppSubtext) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppGreen,
                        unfocusedBorderColor = WhatsAppBorder,
                        focusedTextColor = WhatsAppText,
                        unfocusedTextColor = WhatsAppText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(appIdInput, tokenInput, channelInput) },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppCardBg)
            ) {
                Text("Cancel", color = WhatsAppSubtext)
            }
        },
        containerColor = WhatsAppDarkSecondary,
        shape = RoundedCornerShape(16.dp)
    )
}
