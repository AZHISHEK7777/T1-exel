package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.AccessKeyRecord
import com.example.data.auth.AuthRepository
import com.example.ui.components.AbhishekSignature
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AdminPanelDialog(
    authRepository: AuthRepository,
    adminViewModel: com.example.ui.AdminViewModel? = null,
    onClearChat: (() -> Unit)? = null,
    onEnterAsAdmin: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTier by remember { mutableStateOf(com.example.data.auth.KeyTier.BASIC) }
    var keyLabelInput by remember { mutableStateOf("") }
    var lastGeneratedKey by remember { mutableStateOf<AccessKeyRecord?>(null) }
    var keyToValidateInput by remember { mutableStateOf("") }
    var validationResultText by remember { mutableStateOf<String?>(null) }
    var allKeys by remember { mutableStateOf(authRepository.getAllKeys()) }
    var adminNotification by remember { mutableStateOf(authRepository.getLastAdminNotification()) }

    var showClearChatDialog by remember { mutableStateOf(false) }
    var isClearingChat by remember { mutableStateOf(false) }
    var clearChatStatusMessage by remember { mutableStateOf<String?>(null) }

    // Observe AdminViewModel role validation state if available
    val roleValidationState by (adminViewModel?.roleValidationState ?: kotlinx.coroutines.flow.MutableStateFlow(com.example.ui.RoleValidationState.Idle)).collectAsState()

    LaunchedEffect(roleValidationState) {
        when (val state = roleValidationState) {
            is com.example.ui.RoleValidationState.Valid -> {
                validationResultText = "✅ ${state.message}"
            }
            is com.example.ui.RoleValidationState.Invalid -> {
                validationResultText = "❌ ${state.message}"
            }
            is com.example.ui.RoleValidationState.Loading -> {
                validationResultText = "⏳ Validating key..."
            }
            is com.example.ui.RoleValidationState.Idle -> {}
        }
    }

    fun refreshKeys() {
        allKeys = authRepository.getAllKeys()
        adminNotification = authRepository.getLastAdminNotification()
    }

    fun copyToClipboard(text: String, message: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("T1 VIP Key", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(16.dp))
                .background(CyberBlack)
                .border(1.5.dp, T1Yellow, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("admin_panel_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(T1Yellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ADMIN PANEL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = T1Yellow,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Key Generator & Validator (Abhishek)",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEnterAsAdmin != null) {
                            Button(
                                onClick = onEnterAsAdmin,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("ENTER APP", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Quick Action: Enter App as Admin
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "👑 LOGGED IN AS ADMIN ABHISHEK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = T1Yellow,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Access granted via passcode 111. You are inside Admin Panel.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (onEnterAsAdmin != null) {
                                    Button(
                                        onClick = onEnterAsAdmin,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberCyan,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ENTER APP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    // Admin Notification for Key Activations
                    if (adminNotification != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2A1C08))
                                    .border(1.5.dp, T1Yellow, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "KEY USAGE NOTIFICATION",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = T1Yellow,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = adminNotification!!,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            authRepository.clearAdminNotification()
                                            adminNotification = null
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    // Quick Action: Enter App Directly as Admin
                    item {
                        Button(
                            onClick = onEnterAsAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = T1Yellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("enter_as_admin_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENTER T1 ESPORTS (ADMIN ACCESS)",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // ADMIN SPECIAL ACTION: CLEAR GLOBAL COMMUNITY CHAT
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF241014))
                                .border(1.5.dp, StatusRed.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                                .testTag("admin_clear_chat_card")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StatusRed.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = StatusRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "COMMUNITY CHAT MANAGEMENT",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = StatusRed,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Text(
                                            text = "Clear messages across all devices",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Yahan se aap squad/global chat ke saare messages aur photos ek click me wipe kar sakte hain.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )

                                clearChatStatusMessage?.let { status ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (status.startsWith("✅")) StatusGreen else StatusRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { showClearChatDialog = true },
                                    enabled = !isClearingChat,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("admin_clear_chat_button")
                                ) {
                                    if (isClearingChat) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("CLEARING CHAT...", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CLEAR ALL CHAT (SQUAD & GLOBAL)",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1. GENERATE NEW KEY SECTION
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "GENERATE VIP ACCESS KEY",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = CyberCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Generate a unique access key for any user/client. Share it so they can unlock the app.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "SELECT KEY TIER (BADGE & ANIMATION):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = T1Yellow,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    com.example.data.auth.KeyTier.entries.forEach { tier ->
                                        val isSelected = (selectedTier == tier)
                                        val tierColor = when (tier) {
                                            com.example.data.auth.KeyTier.BASIC -> CyberCyan
                                            com.example.data.auth.KeyTier.PRO -> T1Yellow
                                            com.example.data.auth.KeyTier.ADMIN -> com.example.ui.theme.NeonPink
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) tierColor.copy(alpha = 0.25f) else DarkSurfaceElevated
                                                )
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                                    color = if (isSelected) tierColor else DarkBorder,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedTier = tier }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = tier.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSelected) tierColor else TextSecondary,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                                Text(
                                                    text = when (tier) {
                                                        com.example.data.auth.KeyTier.BASIC -> "Normal"
                                                        com.example.data.auth.KeyTier.PRO -> "Gold FX"
                                                        com.example.data.auth.KeyTier.ADMIN -> "Rainbow FX"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = TextSecondary.copy(alpha = 0.7f),
                                                        fontSize = 8.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = keyLabelInput,
                                    onValueChange = { keyLabelInput = it },
                                    placeholder = { Text("User/Client Name (e.g. Rahul VIP)", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = T1Yellow,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (adminViewModel != null) {
                                            val generated = adminViewModel.generateKey(
                                                type = selectedTier.name.lowercase(),
                                                label = keyLabelInput
                                            )
                                            lastGeneratedKey = AccessKeyRecord(
                                                key = generated.value,
                                                label = generated.label,
                                                tier = selectedTier,
                                                isUsed = false,
                                                createdAt = generated.createdAt
                                            )
                                        } else {
                                            val newRecord = authRepository.generateNewKey(
                                                label = keyLabelInput,
                                                tier = selectedTier
                                            )
                                            lastGeneratedKey = newRecord
                                        }
                                        keyLabelInput = ""
                                        refreshKeys()
                                        lastGeneratedKey?.let {
                                            copyToClipboard(it.key, "[${it.tier}] Key '${it.key}' generated & copied!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (selectedTier) {
                                            com.example.data.auth.KeyTier.BASIC -> CyberCyan
                                            com.example.data.auth.KeyTier.PRO -> T1Yellow
                                            com.example.data.auth.KeyTier.ADMIN -> com.example.ui.theme.NeonPink
                                        },
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("generate_key_button")
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GENERATE [${selectedTier.name}] KEY", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }

                                lastGeneratedKey?.let { generated ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CyberBlack)
                                            .border(1.dp, StatusGreen, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "GENERATED KEY (READY TO SHARE):",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = StatusGreen, fontSize = 9.sp)
                                                )
                                                Text(
                                                    text = generated.key,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 1.sp
                                                    )
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    copyToClipboard(generated.key, "Key copied to clipboard!")
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = T1Yellow)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. VALIDATE KEY SECTION
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = T1Yellow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "VALIDATE EXISTING KEY",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = T1Yellow,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = keyToValidateInput,
                                        onValueChange = { keyToValidateInput = it },
                                        placeholder = { Text("Paste Key (e.g. T1-XXXX-XXXX)", color = TextSecondary, fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = T1Yellow,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (adminViewModel != null) {
                                                adminViewModel.validateUserRole(keyToValidateInput)
                                            } else {
                                                val found = allKeys.find { it.key.equals(keyToValidateInput.trim(), ignoreCase = true) }
                                                validationResultText = when {
                                                    found == null -> "❌ Key not found in database!"
                                                    found.isRevoked -> "🚫 Key is REVOKED (Access blocked)"
                                                    else -> "✅ Key is ACTIVE & VALID for ${found.label} (${found.createdAt})"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DarkSurfaceElevated,
                                            contentColor = T1Yellow
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(52.dp)
                                    ) {
                                        Text("CHECK", fontWeight = FontWeight.Bold)
                                    }
                                }

                                validationResultText?.let { result ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (result.startsWith("✅")) StatusGreen else StatusRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 3. ALL ACTIVE KEYS LIST
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE STORED KEYS (${allKeys.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    items(allKeys) { itemKey ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBlack)
                                .border(
                                    0.5.dp,
                                    if (itemKey.isRevoked) StatusRed.copy(alpha = 0.5f) else DarkBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = itemKey.key,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (itemKey.isRevoked) StatusRed else TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (itemKey.tier) {
                                                        com.example.data.auth.KeyTier.BASIC -> CyberCyan.copy(alpha = 0.2f)
                                                        com.example.data.auth.KeyTier.PRO -> T1Yellow.copy(alpha = 0.2f)
                                                        com.example.data.auth.KeyTier.ADMIN -> com.example.ui.theme.NeonPink.copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = itemKey.tier.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = when (itemKey.tier) {
                                                        com.example.data.auth.KeyTier.BASIC -> CyberCyan
                                                        com.example.data.auth.KeyTier.PRO -> T1Yellow
                                                        com.example.data.auth.KeyTier.ADMIN -> com.example.ui.theme.NeonPink
                                                    },
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (itemKey.isRevoked) StatusRed else if (itemKey.isUsed) CyberCyan.copy(alpha = 0.2f) else StatusGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (itemKey.isRevoked) "REVOKED" else if (itemKey.isUsed) "USED (1x)" else "UNUSED",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (itemKey.isRevoked) Color.White else if (itemKey.isUsed) CyberCyan else StatusGreen,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    if (itemKey.isUsed && itemKey.usedBy.isNotBlank()) {
                                        Text(
                                            text = "👤 Activated by: ${itemKey.usedBy} • Method: ${if (itemKey.pin.isNotBlank()) itemKey.pin else "Verified"}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Text(
                                        text = "${itemKey.label} • Created: ${itemKey.createdAt}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(itemKey.key, "Copied key: ${itemKey.key}")
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = T1Yellow, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            authRepository.deleteKey(itemKey.key)
                                            refreshKeys()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        AbhishekSignature(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    if (showClearChatDialog) {
        Dialog(
            onDismissRequest = { showClearChatDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberBlack)
                    .border(2.dp, StatusRed, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(StatusRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = StatusRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CLEAR ALL CHAT MESSAGES?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = StatusRed,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kya aap sach me poori community chat clear karna chahte hain?\n\nSabhi players ki messages aur photos hamesha ke liye delete ho jayengi. Ye action reverse nahi ho sakta.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showClearChatDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceElevated,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showClearChatDialog = false
                                isClearingChat = true
                                clearChatStatusMessage = "⏳ Clearing chat..."
                                if (onClearChat != null) {
                                    onClearChat()
                                    isClearingChat = false
                                    clearChatStatusMessage = "✅ All community chat messages cleared successfully!"
                                    Toast.makeText(context, "Chat cleared successfully!", Toast.LENGTH_SHORT).show()
                                } else if (adminViewModel != null) {
                                    adminViewModel.clearGlobalChat { success ->
                                        isClearingChat = false
                                        clearChatStatusMessage = if (success) "✅ All community chat messages cleared successfully!" else "⚠️ Chat cleared locally."
                                        Toast.makeText(context, "Chat cleared successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val repo = com.example.data.chat.GlobalChatRepository(context)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val success = repo.clearAllChat()
                                        isClearingChat = false
                                        clearChatStatusMessage = if (success) "✅ All community chat messages cleared successfully!" else "⚠️ Chat cleared locally."
                                        Toast.makeText(context, "Chat cleared successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("confirm_clear_chat_button")
                        ) {
                            Text("YES, CLEAR CHAT", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
