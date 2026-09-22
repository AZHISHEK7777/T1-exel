package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedSensitivityEntity
import com.example.data.model.DeviceInfo
import com.example.data.model.SensitivityProfile
import com.example.ui.components.AbhishekAnimatedBanner
import com.example.ui.components.AbhishekSignature
import com.example.ui.components.CalculationAnimationDialog
import com.example.ui.dialogs.SaveProfileDialog
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
import com.example.util.NetworkState

@Composable
fun SensitivityScreen(
    deviceInfo: DeviceInfo,
    networkState: NetworkState,
    sensitivity: SensitivityProfile,
    isGenerating: Boolean,
    generationProgress: Float,
    generationStage: String,
    savedSensitivities: List<SavedSensitivityEntity>,
    isWithDpiMode: Boolean = sensitivity.isWithDpi,
    onGenerateClick: () -> Unit = {},
    onGenerateWithDpiChoice: (Boolean) -> Unit = { onGenerateClick() },
    onDpiModeChange: (Boolean) -> Unit = {},
    onApplyPreset: (SensitivityProfile) -> Unit,
    onCopySettings: () -> Unit,
    onSaveProfile: (String) -> Unit,
    onLoadProfile: (SavedSensitivityEntity) -> Unit,
    onDeleteProfile: (Long) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDpiChoiceDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "congrats_glow")
    val buttonGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_glow"
    )

    if (isGenerating) {
        CalculationAnimationDialog(
            progress = generationProgress,
            stageText = generationStage,
            deviceName = deviceInfo.fullDeviceName
        )
    }

    if (showSaveDialog) {
        SaveProfileDialog(
            initialName = "${deviceInfo.fullDeviceName} Sensi",
            onSave = {
                onSaveProfile(it)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (showDpiChoiceDialog) {
        DpiChoiceDialog(
            deviceName = deviceInfo.fullDeviceName,
            stockDpi = deviceInfo.screenDpi,
            onModeSelected = { withDpi ->
                showDpiChoiceDialog = false
                onGenerateWithDpiChoice(withDpi)
            },
            onDismiss = { showDpiChoiceDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HEADER: AUTO-DETECTED DEVICE CONGRATULATIONS CARD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF241A04),
                                Color(0xFF161205),
                                DarkSurface
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                T1Yellow,
                                CyberCyan.copy(alpha = 0.7f),
                                T1Yellow
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
                    .testTag("device_congratulations_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Celebration badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(T1Yellow.copy(alpha = 0.18f))
                            .border(1.dp, T1Yellow.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🎉", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CONGRATULATIONS!",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = T1Yellow,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto-detected device name
                    Text(
                        text = "${deviceInfo.fullDeviceName.uppercase()} DETECTED",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.testTag("detected_device_title")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Hardware auto-identified. Digitizer & touch kernel ready.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hardware Specs Pills (Hz, RAM, DPI, Android Version)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpecPill(
                            label = "REFRESH",
                            value = "${deviceInfo.refreshRateHz}Hz",
                            icon = Icons.Default.Speed,
                            color = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        SpecPill(
                            label = "RAM",
                            value = "${deviceInfo.totalRamGb} GB",
                            icon = Icons.Default.Memory,
                            color = T1Yellow,
                            modifier = Modifier.weight(1f)
                        )
                        SpecPill(
                            label = "DENSITY",
                            value = "${deviceInfo.screenDpi} DPI",
                            icon = Icons.Default.PhoneAndroid,
                            color = StatusGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. GENERATE SENSI BUTTON (Placed directly below congratulations as requested)
        item {
            Button(
                onClick = { showDpiChoiceDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .scale(buttonGlowScale)
                    .background(com.example.ui.theme.T1GoldGradient, RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFFFF155), RoundedCornerShape(14.dp))
                    .testTag("generate_sensi_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE SENSI FOR ${deviceInfo.model.ifBlank { deviceInfo.manufacturer }.uppercase()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                )
            }
        }

        // 3. LIVE INTERNET STATUS BAR
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .border(
                        0.5.dp,
                        if (networkState.isConnected) StatusGreen.copy(alpha = 0.4f) else StatusRed.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (networkState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (networkState.isConnected) StatusGreen else StatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (networkState.isConnected) "ONLINE CALIBRATION ACTIVE" else "OFFLINE — RECONNECT INTERNET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (networkState.isConnected) StatusGreen else StatusRed,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(T1Yellow.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LOCKED HEADSHOT CONFIG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = T1Yellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }

        // 4. FIXED SENSITIVITY CARD (Exact number representation with line meters)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, T1Yellow.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
                    .testTag("fixed_sensitivity_card")
            ) {
                Column {
                    // Header of the card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${deviceInfo.fullDeviceName} SENSI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked Calibration",
                                    tint = T1Yellow,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = sensitivity.headshotRating,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, T1Yellow, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (sensitivity.isWithDpi) "WITH DPI MODE" else "NON-DPI MODE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (sensitivity.isWithDpi) CyberCyan else T1Yellow,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // DPI Mode Selector Segmented Switch (With DPI vs Non-DPI)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberBlack)
                            .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // NON-DPI (Default Phone)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!sensitivity.isWithDpi) T1Yellow else Color.Transparent)
                                .clickable { onDpiModeChange(false) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛡️ WITH NON-DPI",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (!sensitivity.isWithDpi) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // WITH DPI (Developer Options)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sensitivity.isWithDpi) CyberCyan else Color.Transparent)
                                .clickable { onDpiModeChange(true) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡ WITH DPI",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (sensitivity.isWithDpi) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Fixed Sensitivity Representation Lines (Realistic Esports Scale)
                    CompactFixedSensitivityRow(
                        name = "GENERAL (CAMERA)",
                        value = sensitivity.general,
                        color = T1Yellow
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CompactFixedSensitivityRow(
                        name = "RED DOT",
                        value = sensitivity.redDot,
                        color = Color(0xFFFF5252)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CompactFixedSensitivityRow(
                        name = "2X SCOPE",
                        value = sensitivity.scope2x,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CompactFixedSensitivityRow(
                        name = "4X SCOPE",
                        value = sensitivity.scope4x,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CompactFixedSensitivityRow(
                        name = "SNIPER SCOPE",
                        value = sensitivity.sniperScope,
                        color = Color(0xFFFFD740)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CompactFixedSensitivityRow(
                        name = "FREE LOOK",
                        value = sensitivity.freeLook,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Specs: Recommended DPI & Fire Button Size Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Recommended DPI
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (sensitivity.isWithDpi) "DEV OPTIONS DPI" else "FACTORY PHONE DPI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (sensitivity.isWithDpi) CyberCyan else TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${sensitivity.recommendedDpi} DPI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (sensitivity.isWithDpi) CyberCyan else T1Yellow,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }

                        // Fire Button Size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "FIRE BUTTON SIZE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${sensitivity.fireButtonSize}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Drag Technique
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = T1Yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "RECOMMENDED DRAG TECHNIQUE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = sensitivity.dragTechnique,
                                    style = MaterialTheme.typography.bodySmall.copy(
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

        // 5. ACTIONS: COPY SETTINGS & SAVE PROFILE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCopySettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = T1Yellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COPY SETTINGS",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { showSaveDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = T1Yellow),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, T1Yellow, RoundedCornerShape(10.dp))
                        .testTag("save_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE PROFILE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 6. SAVED PROFILES LIST
        if (savedSensitivities.isNotEmpty()) {
            item {
                Text(
                    text = "SAVED DEVICE PROFILES (${savedSensitivities.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            items(savedSensitivities) { profile ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Gen: ${profile.general} • RedDot: ${profile.redDot} • 2x: ${profile.scope2x} • DPI: ${profile.dpi} • Button: ${profile.buttonSize}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onLoadProfile(profile) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Load",
                                    tint = StatusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteProfile(profile.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = StatusRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. ANIMATED "MADE WITH LOVE BY ABHISHEK" AT BOTTOM
        item {
            AbhishekAnimatedBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * Compact fixed sensitivity line representation with numeric badge.
 * Matches: "kam jyada nahin kar sakte fix rahega aur itna jyada bada Nahin rahega ek hi jagah per rahega aur number likha hua rahega aur ek line se represent hokar"
 */
@Composable
private fun CompactFixedSensitivityRow(
    name: String,
    value: Int,
    color: Color
) {
    val progress = (value / 200f).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextPrimary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberBlack)
                    .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress line representing value out of 200
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CyberBlack,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun SpecPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
            .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 8.sp
                )
            )
        }
    }
}

/**
 * Modal dialog prompted when user clicks "GENERATE SENSI FOR [DEVICE]".
 * Offers the explicit choice between "WITH DPI" (developer options pro drag)
 * and "WITH NON-DPI" (factory phone default screen).
 */
@Composable
private fun DpiChoiceDialog(
    deviceName: String,
    stockDpi: Int,
    onModeSelected: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, T1Yellow),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = T1Yellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELECT SENSI MODE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = T1Yellow,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Text(
                            text = "Calibrating for $deviceName",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OPTION 1: WITH NON-DPI (Default Phone Screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberBlack)
                        .border(1.dp, T1Yellow.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable { onModeSelected(false) }
                        .padding(14.dp)
                        .testTag("option_non_dpi")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(T1Yellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WITH NON-DPI",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = T1Yellow,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(T1Yellow)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "DEFAULT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Factory Screen DPI ($stockDpi DPI)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "No Developer Options needed. 100% safe, natural finger drag headshots.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OPTION 2: WITH DPI (Boosted Developer Options)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberBlack)
                        .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable { onModeSelected(true) }
                        .padding(14.dp)
                        .testTag("option_with_dpi")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WITH DPI",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCyan)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "PRO DRAG",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Boosted Developer Options DPI (${stockDpi + 50}+ DPI)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Silky quick 360° swipe speed, smaller crosshair, instant flick headshots.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

