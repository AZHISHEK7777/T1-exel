package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.PlayerProfileData
import com.example.data.network.UidInspectionResult
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
import com.example.util.NetworkState

val AvailableRegions = listOf(
    "IND" to "India (IND)",
    "BR" to "Brazil (BR)",
    "ID" to "Indonesia (ID)",
    "SG" to "Singapore (SG)",
    "US" to "North America (US)",
    "ME" to "Middle East (ME)",
    "EU" to "Europe (EU)",
    "TH" to "Thailand (TH)",
    "VN" to "Vietnam (VN)",
    "BD" to "Bangladesh (BD)",
    "PK" to "Pakistan (PK)"
)

@Composable
fun UidInspectorScreen(
    uidInput: String,
    selectedRegion: String,
    networkState: NetworkState,
    inspectionResult: UidInspectionResult,
    apiEndpoint: String,
    onUidChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onInspectClick: () -> Unit,
    onOpenConfigClick: () -> Unit,
    onCopyNickname: (String) -> Unit = {},
    onCopyUid: (String) -> Unit = {},
    onOpenWeb: () -> Unit = {}
) {
    var regionDropdownExpanded by remember { mutableStateOf(false) }
    var showRawJson by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Column {
                Text(
                    text = "UID INSPECTOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = T1Yellow,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Verified Free Fire nickname & region lookup via redxgame.com ID checker",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Internet Status & Backend Source Badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(0.5.dp, DarkBorder, RoundedCornerShape(12.dp))
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
                                imageVector = if (networkState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (networkState.isConnected) StatusGreen else StatusRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (networkState.isConnected) "Internet: ${networkState.networkType}" else "Internet: Disconnected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (networkState.isConnected) StatusGreen else StatusRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Backend Source Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberBlack)
                                .clickable { onOpenConfigClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (apiEndpoint.isNotBlank()) CyberCyan else StatusGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (apiEndpoint.isNotBlank()) "Custom API" else "REDX Game Live",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (apiEndpoint.isNotBlank()) CyberCyan else StatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Input Card: UID & Region
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // UID Input Field
                    OutlinedTextField(
                        value = uidInput,
                        onValueChange = onUidChange,
                        label = { Text("Player UID (Numeric)") },
                        placeholder = { Text("e.g. 1048294821") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = T1Yellow)
                        },
                        trailingIcon = {
                            if (uidInput.isNotEmpty()) {
                                IconButton(onClick = { onUidChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear UID", tint = TextSecondary)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                onInspectClick()
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("uid_text_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = T1Yellow,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = T1Yellow,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = T1Yellow
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Region Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = AvailableRegions.find { it.first == selectedRegion }?.second ?: selectedRegion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Server Region") },
                            leadingIcon = {
                                Icon(Icons.Default.Public, contentDescription = null, tint = CyberCyan)
                            },
                            trailingIcon = {
                                IconButton(onClick = { regionDropdownExpanded = !regionDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Region", tint = TextSecondary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { regionDropdownExpanded = true }
                                .testTag("region_select_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = T1Yellow,
                                unfocusedBorderColor = DarkBorder,
                                focusedLabelColor = T1Yellow,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        DropdownMenu(
                            expanded = regionDropdownExpanded,
                            onDismissRequest = { regionDropdownExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            AvailableRegions.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, color = TextPrimary) },
                                    onClick = {
                                        onRegionChange(code)
                                        regionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Inspect Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onInspectClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(com.example.ui.theme.T1GoldGradient, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFFF155), RoundedCornerShape(12.dp))
                            .testTag("inspect_uid_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INSPECT PLAYER UID",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }

        // Result Content Area
        item {
            when (inspectionResult) {
                is UidInspectionResult.Idle -> {
                    IdleGuidanceCard()
                }

                is UidInspectionResult.Loading -> {
                    LoadingCard()
                }

                is UidInspectionResult.NoInternet -> {
                    NoInternetCard(inspectionResult.message)
                }

                is UidInspectionResult.NotConfigured -> {
                    NotConfiguredCard(
                        title = inspectionResult.title,
                        details = inspectionResult.details,
                        onOpenConfig = onOpenConfigClick
                    )
                }

                is UidInspectionResult.Unavailable -> {
                    UnavailableCard(
                        title = inspectionResult.title,
                        details = inspectionResult.details
                    )
                }

                is UidInspectionResult.Success -> {
                    PlayerProfileCard(
                        profile = inspectionResult.profile,
                        showRawJson = showRawJson,
                        onToggleRawJson = { showRawJson = !showRawJson },
                        onCopyNickname = onCopyNickname,
                        onCopyUid = onCopyUid,
                        onOpenWeb = onOpenWeb
                    )
                }
            }
        }

        // Integrity & Policy Disclaimer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Authentic Data Guarantee: T1 ESPORTS displays only genuine API responses received from authorized game data endpoints. When a data source is absent or unresponsive, no fictitious records or mock stats are fabricated.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    )
                }
            }
        }

        // Abhishek Signature
        item {
            com.example.ui.components.AbhishekSignature(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun IdleGuidanceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(0.5.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = T1Yellow,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Enter Player UID Above",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select the corresponding server region and tap 'Inspect Player UID' to retrieve official player records.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, T1Yellow.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                color = T1Yellow,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Connecting to REDX Game Gateway...",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Querying redxgame.com/tools/free-fire-id-checker for verified player ID...",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }
    }
}

@Composable
private fun NoInternetCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, StatusRed.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = StatusRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Internet Connection Offline",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = StatusRed,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun NotConfiguredCard(
    title: String,
    details: String,
    onOpenConfig: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, T1Amber, RoundedCornerShape(14.dp))
            .padding(18.dp)
            .testTag("result_not_configured_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = T1Amber,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title, // "Real data source not configured."
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = T1Amber,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = details, // "Player data unavailable..."
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("configure_data_source_button"),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(T1Yellow)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = T1Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONFIGURE BACKEND DATA SOURCE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = T1Yellow,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun UnavailableCard(
    title: String,
    details: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, StatusRed.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .padding(18.dp)
            .testTag("result_unavailable_card")
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = StatusRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title, // "Player data unavailable."
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = StatusRed,
                        fontWeight = FontWeight.Black
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun PlayerProfileCard(
    profile: PlayerProfileData,
    showRawJson: Boolean,
    onToggleRawJson: () -> Unit,
    onCopyNickname: (String) -> Unit = {},
    onCopyUid: (String) -> Unit = {},
    onOpenWeb: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.5.dp, T1Yellow, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("result_player_profile_card")
    ) {
        Column {
            // 1. Account Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusGreen.copy(alpha = 0.15f))
                    .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = profile.statusMessage,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StatusGreen,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFICIAL RECORD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Profile Header: In-Game Nickname with Copy Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberBlack)
                    .border(0.5.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, T1Yellow, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = T1Yellow,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "IN-GAME NICKNAME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = profile.nickname,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { onCopyNickname(profile.nickname) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .size(38.dp)
                            .testTag("copy_player_nickname_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Nickname",
                            tint = T1Yellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Two columns: Player UID & Server Region
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // UID Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberBlack)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PLAYER UID", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(profile.accountId, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }
                        IconButton(
                            onClick = { onCopyUid(profile.accountId) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy UID", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Server Region Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberBlack)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("SERVER REGION", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(profile.region, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Stats Badges (if present)
            if (profile.likes != null || profile.rankScore != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.likes != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = T1Yellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("LIKES", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                    Text("${profile.likes}", style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    if (profile.rankScore != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("SCORE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                    Text("${profile.rankScore}", style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            if (profile.guildName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBlack)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = T1Amber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("GUILD", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                            Text(profile.guildName, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Free Fire Mania Extra Details: Account Age & Creation Date
            if (profile.accountCreated != null || profile.accountAge != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.accountAge != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("ACCOUNT AGE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                Text(profile.accountAge, style = MaterialTheme.typography.labelMedium.copy(color = CyberCyan, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    if (profile.accountCreated != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("CREATED ON", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                Text(profile.accountCreated, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Free Fire Mania Extra Details: Booyah Pass & Prime Level
            if (profile.booyahPass != null || profile.primeLevel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.booyahPass != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("BOOYAH PASS", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                Text(profile.booyahPass, style = MaterialTheme.typography.labelMedium.copy(color = T1Yellow, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    if (profile.primeLevel != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("PRIME LEVEL", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                Text(profile.primeLevel, style = MaterialTheme.typography.labelMedium.copy(color = StatusGreen, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Free Fire Mania Extra Details: Last Login & Game Client
            if (profile.lastLogin != null || profile.gameVersion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.lastLogin != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("LAST LOGIN", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                Text(profile.lastLogin, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    if (profile.gameVersion != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBlack)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("GAME CLIENT", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                                Text(profile.gameVersion, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            if (profile.bio != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Signature: \"${profile.bio}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Data Source info & Direct Web Link
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = profile.dataSource,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onOpenWeb() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Open Web Tool",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = T1Yellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = T1Yellow,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Raw Server JSON View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleRawJson() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showRawJson) "Hide Raw Server JSON" else "View Authentic Response JSON",
                    style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan, fontWeight = FontWeight.Bold)
                )
            }

            if (showRawJson) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberBlack)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = profile.rawJson,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
