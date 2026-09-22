package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FreeFireEdition
import com.example.util.InstalledGameStatus

@Composable
fun LauncherDialog(
    status: InstalledGameStatus,
    onLaunch: (FreeFireEdition) -> Unit,
    onInstall: (FreeFireEdition) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, T1Yellow, RoundedCornerShape(20.dp)),
            color = CyberBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GAME LAUNCHER",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = T1Yellow,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = "Official Client Direct Launch",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
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

                Spacer(modifier = Modifier.height(16.dp))

                // Free Fire Standard Card
                GameEditionCard(
                    title = "Free Fire",
                    packageName = FreeFireEdition.STANDARD.packageName,
                    isInstalled = status.standardInstalled,
                    onLaunch = { onLaunch(FreeFireEdition.STANDARD) },
                    onInstall = { onInstall(FreeFireEdition.STANDARD) },
                    testTag = "launch_standard_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Free Fire MAX Card
                GameEditionCard(
                    title = "Free Fire MAX",
                    packageName = FreeFireEdition.MAX.packageName,
                    isInstalled = status.maxInstalled,
                    onLaunch = { onLaunch(FreeFireEdition.MAX) },
                    onInstall = { onInstall(FreeFireEdition.MAX) },
                    testTag = "launch_max_button"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Strict Safety Guarantee Statement
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security verified",
                            tint = StatusGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "100% Policy Compliant & Anti-Ban Safe",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This utility only opens the officially installed application package via standard Android intents. It does not modify game files, memory, or run cheats/automation.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameEditionCard(
    title: String,
    packageName: String,
    isInstalled: Boolean,
    onLaunch: () -> Unit,
    onInstall: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(
                1.dp,
                if (isInstalled) T1Yellow.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isInstalled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Installed",
                            tint = StatusGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = if (isInstalled) "Installed and ready to launch" else "Not installed on this device",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isInstalled) StatusGreen else TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            if (isInstalled) {
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = T1Yellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag(testTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LAUNCH",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
            } else {
                Button(
                    onClick = onInstall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = T1Yellow
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, T1Yellow, RoundedCornerShape(8.dp))
                        .testTag("${testTag}_install")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GET ON PLAY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
