package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AbhishekAnimatedBanner
import com.example.ui.components.AbhishekSignature
import com.example.ui.theme.CardBorderGradient
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardGradient
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.T1GoldGradient
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.NetworkState

@Composable
fun KeyAuthScreen(
    networkState: NetworkState,
    onKeySubmitted: (key: String, userName: String) -> Unit,
    onPinLoginSubmitted: (identifier: String, pin: String) -> Unit,
    pendingPinSetup: com.example.data.auth.KeyValidationResult.RequirePinSetup?,
    onCompletePinSetup: (pin: String) -> Unit,
    onCancelPinSetup: () -> Unit,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Activate Key, 1: 4-Digit PIN Login
    var nameInput by remember { mutableStateOf("") }
    var keyInput by remember { mutableStateOf("") }
    var pinLoginIdentifier by remember { mutableStateOf("") }
    var pinLoginDigits by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    val infiniteTransition = rememberInfiniteTransition(label = "cyber_effects")
    val logoGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val rotateRings by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_rings"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // 4-Digit PIN Setup Dialog when activating an unused key
    if (pendingPinSetup != null) {
        AlertDialog(
            onDismissRequest = onCancelPinSetup,
            containerColor = Color(0xFF10131E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(T1Yellow.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = T1Yellow, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "GENERATE 4-DIGIT PIN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = T1Yellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "Key: ${pendingPinSetup.key} • Tier: [${pendingPinSetup.tier.name}]",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = when (pendingPinSetup.tier) {
                                com.example.data.auth.KeyTier.BASIC -> CyberCyan
                                com.example.data.auth.KeyTier.PRO -> T1Yellow
                                com.example.data.auth.KeyTier.ADMIN -> NeonPink
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hello ${pendingPinSetup.userName}! Create your unique 4-digit security PIN. You will use this PIN for all future instant logins without needing another key.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                newPinInput = it
                            }
                        },
                        label = { Text("4-DIGIT SECURITY PIN", color = T1Yellow, fontSize = 11.sp) },
                        placeholder = { Text("e.g. 5928", color = TextSecondary.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = T1Yellow) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newPinInput.length == 4) {
                                    onCompletePinSetup(newPinInput)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = T1Yellow,
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
                    onClick = {
                        if (newPinInput.length == 4) {
                            onCompletePinSetup(newPinInput)
                        }
                    },
                    enabled = (newPinInput.length == 4),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = T1Yellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ACTIVATE & LOGIN", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelPinSetup) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .statusBarsPadding()
            .padding(top = 8.dp)
            .navigationBarsPadding()
            .imePadding()
            .testTag("key_auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==================== TOP HERO SECTION ====================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Cyber Animated Shield Logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    // Outer rotating decorative ring
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .rotate(rotateRings)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(T1Yellow, CyberCyan, NeonPink, T1Yellow)
                                ),
                                shape = CircleShape
                            )
                    )

                    // Middle pulsing glow
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .scale(logoGlowScale)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        T1Yellow.copy(alpha = 0.35f * pulseAlpha),
                                        CyberCyan.copy(alpha = 0.15f),
                                        Color.Black
                                    )
                                )
                            )
                            .border(2.dp, T1Yellow, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.t1_logo),
                            contentDescription = "T1 Esports Logo",
                            modifier = Modifier.size(68.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Title with Gold Glow
                Text(
                    text = "T1 ESPORTS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = T1Yellow,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // System Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF131722))
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SINGLE-USE KEYS • 4-DIGIT PIN SECURITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Switcher: [ACTIVATE KEY] vs [PIN LOGIN]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131724))
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) T1Yellow else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = if (selectedTab == 0) Color.Black else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NEW KEY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selectedTab == 0) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) CyberCyan else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (selectedTab == 1) Color.Black else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PIN LOGIN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selectedTab == 1) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==================== CENTER GLASSMORPHIC AUTH CARD ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardGradient)
                    .border(1.5.dp, CardBorderGradient, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedTab == 0) {
                        // MODE 0: ACTIVATE NEW KEY
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = T1Yellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVATE SINGLE-USE KEY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }

                        Text(
                            text = "Each key works only once. After activation, you will set a 4-digit PIN for future logins.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        // 1. Mandatory Name Input Field
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = {
                                Text(
                                    "YOUR PLAYER NAME (COMPULSORY)",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            placeholder = {
                                Text(
                                    "e.g. Abhishek Pro, Toxic, Viper",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CyberCyan
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = CyberCyan,
                                focusedContainerColor = Color(0xFF0F121C),
                                unfocusedContainerColor = Color(0xFF0C0E17)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Key Input Field
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = {
                                Text(
                                    "ENTER ACCESS KEY",
                                    color = T1Yellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            placeholder = {
                                Text(
                                    "e.g. T1-PRO-555",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = T1Yellow
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = clipboard.primaryClip
                                            if (clip != null && clip.itemCount > 0) {
                                                val pasted = clip.getItemAt(0).text?.toString()?.trim()
                                                if (!pasted.isNullOrBlank()) {
                                                    keyInput = pasted
                                                }
                                            }
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste Key",
                                        tint = T1Yellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("key_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    onKeySubmitted(keyInput, nameInput)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = T1Yellow,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = T1Yellow,
                                focusedContainerColor = Color(0xFF0F121C),
                                unfocusedContainerColor = Color(0xFF0C0E17)
                            )
                        )
                    } else {
                        // MODE 1: 4-DIGIT PIN LOGIN
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "4-DIGIT PIN LOGIN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }

                        Text(
                            text = "Login instantly using your registered Player Name or Key and your 4-digit PIN.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        // 1. Registered Name or Key
                        OutlinedTextField(
                            value = pinLoginIdentifier,
                            onValueChange = { pinLoginIdentifier = it },
                            label = { Text("REGISTERED NAME OR KEY", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            placeholder = { Text("e.g. Abhishek or T1-PRO-555", color = TextSecondary.copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = CyberCyan,
                                focusedContainerColor = Color(0xFF0F121C),
                                unfocusedContainerColor = Color(0xFF0C0E17)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. 4-Digit PIN
                        OutlinedTextField(
                            value = pinLoginDigits,
                            onValueChange = {
                                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                    pinLoginDigits = it
                                }
                            },
                            label = { Text("4-DIGIT PIN", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            placeholder = { Text("••••", color = TextSecondary.copy(alpha = 0.5f), fontSize = 16.sp) },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = CyberCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    onPinLoginSubmitted(pinLoginIdentifier, pinLoginDigits)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = CyberCyan,
                                focusedContainerColor = Color(0xFF0F121C),
                                unfocusedContainerColor = Color(0xFF0C0E17)
                            )
                        )
                    }

                    // Error Message banner
                    AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusRed.copy(alpha = 0.15f))
                                    .border(1.dp, StatusRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = StatusRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = StatusRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (selectedTab == 0) {
                                onKeySubmitted(keyInput, nameInput)
                            } else {
                                onPinLoginSubmitted(pinLoginIdentifier, pinLoginDigits)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                if (selectedTab == 0) T1GoldGradient else Brush.horizontalGradient(listOf(CyberCyan, Color(0xFF00BFFF))),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("unlock_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTab == 0) "VERIFY KEY & CREATE PIN" else "LOGIN WITH 4-DIGIT PIN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            // Network Offline Warning
            if (!networkState.isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusRed.copy(alpha = 0.2f))
                        .border(1.dp, StatusRed, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Active internet connection is mandatory to authenticate and run T1 Esports.",
                        style = MaterialTheme.typography.bodySmall.copy(color = StatusRed, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==================== BOTTOM BRANDING & DEVELOPER ====================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                AbhishekAnimatedBanner()
                Spacer(modifier = Modifier.height(10.dp))
                AbhishekSignature()
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Need a Key? Contact Abhishek on Instagram or Telegram",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
