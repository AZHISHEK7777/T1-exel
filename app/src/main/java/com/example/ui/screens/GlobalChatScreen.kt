package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GlobalChatScreen(
    userName: String,
    messages: List<ChatMessageEntity>,
    onSendMessage: (text: String, attachmentUri: String?, attachmentType: String) -> Unit,
    callRoom: com.example.data.call.SquadCallRoom? = null,
    onStartCall: () -> Unit = {},
    onJoinCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showTacticalPresets by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Photo picker launcher (standard zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Scroll to latest message on change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val handleSend = {
        if (inputText.isNotBlank() || selectedImageUri != null) {
            val uriString = selectedImageUri?.toString()
            val type = if (selectedImageUri != null) "IMAGE" else "NONE"
            onSendMessage(inputText.ifBlank { "Sent an image attachment" }, uriString, type)
            inputText = ""
            selectedImageUri = null
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .imePadding()
            .testTag("global_chat_screen")
    ) {
        // 1. TOP HEADER: Community Status & Nickname
        Surface(
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.dp, CyberCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GLOBAL CHAT",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(StatusGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = StatusGreen,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Logged in as $userName • 1,428 Players",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isCallActive = callRoom != null && (callRoom.status == "ACTIVE" || callRoom.status == "RINGING")
                    if (isCallActive) {
                        // Pulsing / Glowing Active Call Join Button
                        Button(
                            onClick = onJoinCall,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("join_live_call_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CALL (${callRoom.participants.size})",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    } else {
                        // Start WhatsApp Voice Call Button (Direct Calling, Zero Setup)
                        Button(
                            onClick = onStartCall,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("start_group_call_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice Call",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CALL",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Quick preset toggle button
                    IconButton(
                        onClick = { showTacticalPresets = !showTacticalPresets },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showTacticalPresets) T1Yellow else DarkSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Tactical Quick Share",
                            tint = if (showTacticalPresets) Color.Black else T1Yellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. QUICK TACTICAL PRESETS BAR (Optional Dropdown)
        AnimatedVisibility(visible = showTacticalPresets) {
            Surface(
                color = Color(0xFF141922),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = DarkBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "QUICK ESPORTS CALLOUTS (TAP TO SEND):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = T1Yellow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        val tacticalChips = listOf(
                            "🎯 Locked 98 General Sensi! Insane Red Numbers",
                            "🏆 Ready for CS Ranked! Need 2 Rusher Teammates",
                            "🎮 Custom Room Created! Code: 88219 (Pass: 123)",
                            "⚡ Tatsuya + Maro loadout is unbeatable!",
                            "🛡️ Gloo Wall fast placed, covering revival"
                        )
                        items(tacticalChips) { chipText ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        onSendMessage(chipText, null, "TACTICAL")
                                        showTacticalPresets = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = chipText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. MESSAGES LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    isCurrentUser = msg.isFromMe || msg.senderName.equals(userName, ignoreCase = true),
                    onJoinCall = onJoinCall
                )
            }
        }

        // 4. ATTACHMENT PREVIEW (If photo selected)
        AnimatedVisibility(visible = selectedImageUri != null) {
            selectedImageUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141922))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Attachment preview",
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Photo Selected",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Ready to send to Global Squad",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = TextSecondary)
                        }
                    }
                }
            }
        }

        // 5. BOTTOM INPUT BAR
        Surface(
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach Photo Button
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .testTag("chat_attach_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach photo",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Message community as $userName...",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { handleSend() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyberCyan
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .then(
                            if (inputText.isNotBlank() || selectedImageUri != null) {
                                Modifier.background(com.example.ui.theme.T1GoldGradient)
                            } else {
                                Modifier.background(Color(0xFF1E2330))
                            }
                        )
                        .clickable(enabled = inputText.isNotBlank() || selectedImageUri != null) {
                            handleSend()
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() || selectedImageUri != null) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TierBadge(role: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_anim")

    when (role) {
        "ADMIN" -> {
            // Rainbow blinking shimmer effect
            val rainbowOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rainbow_anim"
            )
            val blinkAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "admin_blink"
            )

            val rainbowColors = listOf(
                Color(0xFFFF0055).copy(alpha = blinkAlpha),
                Color(0xFFFF9900).copy(alpha = blinkAlpha),
                Color(0xFFFFEE00).copy(alpha = blinkAlpha),
                Color(0xFF00FF66).copy(alpha = blinkAlpha),
                Color(0xFF00EEFF).copy(alpha = blinkAlpha),
                Color(0xFF9900FF).copy(alpha = blinkAlpha),
                Color(0xFFFF0055).copy(alpha = blinkAlpha)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = rainbowColors,
                            startX = rainbowOffset * 200f,
                            endX = (rainbowOffset + 1f) * 200f
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = blinkAlpha),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "ADMIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
        "PRO" -> {
            // Golden pulse animation
            val goldGlow by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "gold_glow"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = 0.35f * goldGlow),
                                Color(0xFFFF9500).copy(alpha = 0.35f * goldGlow)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color(0xFFFFD700).copy(alpha = goldGlow),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "PRO VIP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFFD700),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }
        else -> {
            // BASIC Tier: Clean Static Badge (No flashy animation)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberCyan.copy(alpha = 0.15f))
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "BASIC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberCyan,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    isCurrentUser: Boolean,
    onJoinCall: () -> Unit = {}
) {
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            // Other Gamer Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (message.senderRole) {
                            "ADMIN" -> T1Yellow
                            "PRO" -> Color(0xFFFF9500)
                            else -> CyberCyan
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Sender Name & Role Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = if (isCurrentUser) "You (${message.senderName})" else message.senderName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isCurrentUser) T1Yellow else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                TierBadge(role = message.senderRole)
            }

            // Message Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isCurrentUser) 14.dp else 2.dp,
                            bottomEnd = if (isCurrentUser) 2.dp else 14.dp
                        )
                    )
                    .background(
                        if (isCurrentUser) {
                            Brush.horizontalGradient(listOf(Color(0xFF2C2508), Color(0xFF1E1905)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFF181C28), Color(0xFF121520)))
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when (message.senderRole) {
                            "ADMIN" -> com.example.ui.theme.NeonPink.copy(alpha = 0.5f)
                            "PRO" -> T1Yellow.copy(alpha = 0.5f)
                            else -> if (isCurrentUser) T1Yellow.copy(alpha = 0.45f) else DarkBorder
                        },
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isCurrentUser) 14.dp else 2.dp,
                            bottomEnd = if (isCurrentUser) 2.dp else 14.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    // Attachment if any
                    if (message.attachmentUri != null) {
                        val imageModel = remember(message.attachmentUri) {
                            val u = message.attachmentUri
                            if (u.startsWith("/")) java.io.File(u) else u
                        }
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Shared image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(0.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Message text
                    Text(
                        text = message.messageText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    // Call Invitation Interactive Card
                    if (message.attachmentType == "CALL_INVITE") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1D16)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(StatusGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "SQUAD AUDIO ROOM",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = StatusGreen,
                                                fontWeight = FontWeight.Black
                                            )
                                        )
                                        Text(
                                            text = "Tap button below to connect",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onJoinCall,
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "🟢 JOIN SQUAD CALL",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Timestamp
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
