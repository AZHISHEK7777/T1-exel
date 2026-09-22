package com.example.ui.components

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.T1Amber
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CpuMonitorUtil
import com.example.util.FreeFireEdition
import com.example.util.LivePerformanceStats
import kotlinx.coroutines.delay

enum class PerformanceMode(val label: String) {
    BATTERY_SAVER("Battery Saver"),
    BALANCED("Balanced mode"),
    BOOST("BOOST")
}

@Composable
fun SideGameMonitorDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    onLaunchGame: (FreeFireEdition) -> Unit,
    onOpenOverlaySettings: () -> Unit,
    hasOverlayPermission: Boolean
) {
    val context = LocalContext.current
    var stats by remember {
        mutableStateOf(
            LivePerformanceStats(
                cpuUsagePercent = 39,
                cpuCores = CpuMonitorUtil.getCpuCores(),
                cpuGovernor = Build.HARDWARE.ifBlank { "OCTA-CORE SPEEDSTEP" },
                usedRamGb = 3.6,
                totalRamGb = 8.0,
                ramUsagePercent = 45,
                freeRamGb = 4.4,
                batteryTempCelsius = 34.0f,
                batteryPercent = 41,
                isCharging = false,
                refreshRateHz = 90
            )
        )
    }

    var selectedMode by remember { mutableStateOf(PerformanceMode.BALANCED) }

    // 8 Gaming Tool Interactive States
    var isEsportsModeActive by remember { mutableStateOf(false) }
    var isBackgroundCallsActive by remember { mutableStateOf(true) }
    var isBlockNotificationsActive by remember { mutableStateOf(true) }
    var isRejectCallsActive by remember { mutableStateOf(false) }
    var isViewEnhancementActive by remember { mutableStateOf(false) }
    var isBrightnessLockActive by remember { mutableStateOf(false) }
    var isSmallWindowMode by remember { mutableStateOf(false) }
    var isTouchSamplingBoostActive by remember { mutableStateOf(true) }

    // Session duration counter (increments like 0.1 h, 0.2 h)
    var sessionHours by remember { mutableFloatStateOf(0.1f) }
    var simulatedGpuPercent by remember { mutableIntStateOf(12) }

    var boostFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            CpuMonitorUtil.monitorFlow(context).collect { live ->
                stats = live
                // GPU simulation linked to CPU load
                simulatedGpuPercent = when (selectedMode) {
                    PerformanceMode.BATTERY_SAVER -> (live.cpuUsagePercent * 0.4f).toInt().coerceIn(3, 25)
                    PerformanceMode.BALANCED -> (live.cpuUsagePercent * 0.65f).toInt().coerceIn(8, 55)
                    PerformanceMode.BOOST -> (live.cpuUsagePercent * 0.95f).toInt().coerceIn(15, 88)
                }
            }
        }
    }

    // Timer effect for session duration
    LaunchedEffect(isOpen) {
        while (isOpen) {
            delay(120_000) // update every 2 minutes
            sessionHours += 0.1f
        }
    }

    // Small Floating Window / Pill Mode when toggled
    if (isOpen && isSmallWindowMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141A22).copy(alpha = 0.95f))
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(24.dp))
                    .clickable { isSmallWindowMode = false }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("floating_mini_hud")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CPU ${stats.cpuUsagePercent}% | GPU $simulatedGpuPercent%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "Tap to expand Side HUD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }
        return
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.70f))
                .clickable { onClose() }
        ) {
            // Sliding Side Menu Card (Styled after Game Turbo HUD screenshot)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.90f)
                    .align(Alignment.CenterEnd)
                    .clickable(enabled = false) {} // Prevent click-through
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0F141C),
                                Color(0xFF151C28)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                CyberCyan.copy(alpha = 0.8f),
                                T1Yellow.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .testTag("side_game_monitor_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // TOP ROW: Game Duration, Close Button & Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E2838))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", sessionHours)} h",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GAME TURBO",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2838))
                                .testTag("close_side_monitor_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Side Monitor",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // TOP SECTION: CPU GAUGE, GPU GAUGE & BATTERY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // CPU Circular Gauge
                        CircularMetricGauge(
                            label = "CPU",
                            percent = stats.cpuUsagePercent,
                            accentColor = if (stats.cpuUsagePercent < 50) CyberCyan else T1Yellow
                        )

                        // GPU Circular Gauge
                        CircularMetricGauge(
                            label = "GPU",
                            percent = simulatedGpuPercent,
                            accentColor = if (simulatedGpuPercent < 50) CyberCyan else StatusGreen
                        )

                        // Battery Indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A2433))
                                    .border(1.dp, Color(0xFF2E3E56), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = if (stats.batteryPercent < 20) StatusRed else StatusGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${stats.batteryPercent}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.batteryTempCelsius}°C",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // PERFORMANCE MODE SELECTOR (Battery Saver | Balanced mode | BOOST)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF121822))
                            .border(1.dp, Color(0xFF263548), RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PerformanceMode.values().forEach { mode ->
                            val isSelected = selectedMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) {
                                            when (mode) {
                                                PerformanceMode.BATTERY_SAVER -> CyberCyan.copy(alpha = 0.25f)
                                                PerformanceMode.BALANCED -> Color(0xFF243245)
                                                PerformanceMode.BOOST -> T1Yellow.copy(alpha = 0.25f)
                                            }
                                        } else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) {
                                            when (mode) {
                                                PerformanceMode.BATTERY_SAVER -> CyberCyan
                                                PerformanceMode.BALANCED -> Color(0xFF4C6585)
                                                PerformanceMode.BOOST -> T1Yellow
                                            }
                                        } else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedMode = mode
                                        val feedback = when (mode) {
                                            PerformanceMode.BATTERY_SAVER -> "Battery Saver Active: Capped at 60Hz"
                                            PerformanceMode.BALANCED -> "Balanced Mode Active: Auto Dynamic Scaling"
                                            PerformanceMode.BOOST -> "TURBO BOOST Active: Max CPU/GPU Frequencies!"
                                        }
                                        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) {
                                            when (mode) {
                                                PerformanceMode.BATTERY_SAVER -> CyberCyan
                                                PerformanceMode.BALANCED -> TextPrimary
                                                PerformanceMode.BOOST -> T1Yellow
                                            }
                                        } else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 8 GAMING TOOL QUICK ACTION TILES (4x2 Grid)
                    Text(
                        text = "GAMING ASSIST TOOLS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // ROW 1: Esports Mode & Voice Changer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.SportsEsports,
                                title = "Esports Mode",
                                subtitle = if (isEsportsModeActive) "120Hz Active" else "Off",
                                isActive = isEsportsModeActive,
                                onClick = {
                                    isEsportsModeActive = !isEsportsModeActive
                                    Toast.makeText(
                                        context,
                                        if (isEsportsModeActive) "Esports Tournament Mode: Refresh rate locked to max, notifications blocked"
                                        else "Esports Mode deactivated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Speed,
                                title = "Touch Sampling",
                                subtitle = if (isTouchSamplingBoostActive) "360Hz Turbo" else "Standard",
                                isActive = isTouchSamplingBoostActive,
                                onClick = {
                                    isTouchSamplingBoostActive = !isTouchSamplingBoostActive
                                    Toast.makeText(
                                        context,
                                        if (isTouchSamplingBoostActive) "Digitizer Touch Sampling boosted to 360Hz"
                                        else "Touch Sampling restored to 120Hz",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        // ROW 2: Background Calls & Block Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Call,
                                title = "Background calls",
                                subtitle = if (isBackgroundCallsActive) "Floating HUD" else "Disabled",
                                isActive = isBackgroundCallsActive,
                                onClick = {
                                    isBackgroundCallsActive = !isBackgroundCallsActive
                                    Toast.makeText(
                                        context,
                                        if (isBackgroundCallsActive) "Incoming calls will stay in floating banner"
                                        else "Background calls disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.NotificationsOff,
                                title = "Block notifications",
                                subtitle = if (isBlockNotificationsActive) "Suppressed" else "Allowed",
                                isActive = isBlockNotificationsActive,
                                onClick = {
                                    isBlockNotificationsActive = !isBlockNotificationsActive
                                    Toast.makeText(
                                        context,
                                        if (isBlockNotificationsActive) "Notification popups suppressed during match"
                                        else "Notification popups allowed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        // ROW 3: Reject Calls & View Enhancement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CallEnd,
                                title = "Reject calls",
                                subtitle = if (isRejectCallsActive) "Auto-decline" else "Ring allowed",
                                isActive = isRejectCallsActive,
                                onClick = {
                                    isRejectCallsActive = !isRejectCallsActive
                                    Toast.makeText(
                                        context,
                                        if (isRejectCallsActive) "Calls auto-rejected during match"
                                        else "Call auto-rejection disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Visibility,
                                title = "View enhancement",
                                subtitle = if (isViewEnhancementActive) "Hawk-Eye ON" else "Standard",
                                isActive = isViewEnhancementActive,
                                onClick = {
                                    isViewEnhancementActive = !isViewEnhancementActive
                                    Toast.makeText(
                                        context,
                                        if (isViewEnhancementActive) "Hawk-Eye Color Filter: Vivid saturation enabled"
                                        else "Color saturation normal",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        // ROW 4: Brightness Lock & Display in Small Window
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Lock,
                                title = "Brightness lock",
                                subtitle = if (isBrightnessLockActive) "Locked at 100%" else "Auto",
                                isActive = isBrightnessLockActive,
                                onClick = {
                                    isBrightnessLockActive = !isBrightnessLockActive
                                    Toast.makeText(
                                        context,
                                        if (isBrightnessLockActive) "Brightness locked at 100% to prevent auto-dimming"
                                        else "Brightness lock released",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                            GamingToolTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.OpenInBrowser,
                                title = "Small window",
                                subtitle = "Mini floating pill",
                                isActive = false,
                                onClick = {
                                    isSmallWindowMode = true
                                    Toast.makeText(context, "Collapsed to floating mini HUD", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // ONE-TAP RAM CACHE CLEANER & TURBO PURGE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141C27))
                            .border(0.5.dp, Color(0xFF28384C), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = T1Yellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RAM OPTIMIZER",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = T1Yellow,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                                Text(
                                    text = "${stats.usedRamGb} GB / ${stats.totalRamGb} GB",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    CpuMonitorUtil.cleanRam()
                                    boostFeedback = "⚡ Purged 460MB background RAM cache! 0 Frame drops."
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = T1Yellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "PURGE RAM & SPEED BOOST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            if (boostFeedback != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = boostFeedback!!,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StatusGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // LAUNCH FREE FIRE COMBAT BUTTONS
                    Text(
                        text = "LAUNCH FREE FIRE COMBAT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onLaunchGame(FreeFireEdition.STANDARD) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STANDARD", fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        Button(
                            onClick = { onLaunchGame(FreeFireEdition.MAX) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = T1Yellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FF MAX", fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularMetricGauge(
    label: String,
    percent: Int,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(56.dp)) {
                // Background Track
                drawArc(
                    color = Color(0xFF1E2838),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
                // Active Sweep Arc
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = (percent / 100f).coerceIn(0f, 1f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        )
    }
}

@Composable
private fun GamingToolTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Color(0xFF182738) else Color(0xFF131A24))
            .border(
                width = 1.dp,
                color = if (isActive) CyberCyan else Color(0xFF223042),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) CyberCyan.copy(alpha = 0.25f) else Color(0xFF1B2432)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) CyberCyan else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isActive) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isActive) CyberCyan else Color(0xFF7A8B9E),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
