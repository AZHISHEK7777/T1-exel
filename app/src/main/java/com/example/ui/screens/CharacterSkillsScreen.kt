package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ScenarioSkillConfig
import com.example.data.model.ScenarioSkillDatabase
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CharacterSkillsScreen(
    scenarios: List<ScenarioSkillConfig> = ScenarioSkillDatabase.scenarios
) {
    val context = LocalContext.current
    var selectedScenario by remember { mutableStateOf(scenarios.first()) }

    fun copyComboText(scenario: ScenarioSkillConfig) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = "🎮 T1 ESPORTS FF LOADOUT [${scenario.title}]:\n" +
                "⚡ Active: ${scenario.activeCharacter} (${scenario.activeSkillName})\n" +
                "🛡️ Passive 1: ${scenario.passive1Char} (${scenario.passive1Name})\n" +
                "🎯 Passive 2: ${scenario.passive2Char} (${scenario.passive2Name})\n" +
                "💨 Passive 3: ${scenario.passive3Char} (${scenario.passive3Name})\n" +
                "🐾 Pet: ${scenario.petName} (${scenario.petSkill})\n" +
                "🎒 Loadout: ${scenario.loadoutName}"
        clipboard.setPrimaryClip(ClipData.newPlainText("FF Loadout", text))
        Toast.makeText(context, "Skill Loadout copied! Ready to paste in Global Chat or Game.", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 14.dp)
            .testTag("character_skills_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP PRESET SELECTOR (Easy 1-Tap Category Filter)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(T1Yellow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SELECT PRESET LOADOUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = T1Yellow,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Text(
                        text = "FREE FIRE META 2026",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(scenarios) { scenario ->
                        val isSelected = scenario.id == selectedScenario.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) T1Yellow else DarkSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) T1Yellow else DarkBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedScenario = scenario }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = scenario.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = scenario.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. HERO CHARACTER PREVIEW BANNER (Free Fire Style)
        item {
            AnimatedContent(
                targetState = selectedScenario,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "hero_banner"
            ) { target ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF1E1705),
                                    Color(0xFF0F151F),
                                    CyberBlack
                                )
                            )
                        )
                        .border(1.5.dp, T1Yellow.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Character Portrait & Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(2.dp, T1Yellow, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.t1_logo),
                                    contentDescription = target.activeCharacter,
                                    modifier = Modifier.size(54.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(T1Yellow)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = target.badge.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = target.activeCharacter.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        fontSize = 20.sp
                                    )
                                )

                                Text(
                                    text = "Main Ability: ${target.activeSkillName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Right: Quick Copy Button
                        IconButton(
                            onClick = { copyComboText(target) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Combo",
                                tint = T1Yellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. FREE FIRE 4-SLOT CHARACTER SKILL GRID
        item {
            Text(
                text = "EQUIPPED CHARACTER SKILLS (SLOTS 1-4)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // Slot 1: Active Skill
        item {
            FreeFireSkillSlotCard(
                slotLabel = "SLOT 1 • ACTIVE SKILL",
                characterName = selectedScenario.activeCharacter,
                skillName = selectedScenario.activeSkillName,
                perkText = selectedScenario.activeRolePerk,
                badgeColor = T1Yellow,
                icon = Icons.Default.Bolt,
                isHeroSlot = true
            )
        }

        // Slot 2: Passive 1
        item {
            FreeFireSkillSlotCard(
                slotLabel = "SLOT 2 • PASSIVE SKILL",
                characterName = selectedScenario.passive1Char,
                skillName = selectedScenario.passive1Name,
                perkText = selectedScenario.passive1Perk,
                badgeColor = CyberCyan,
                icon = Icons.Default.Shield,
                isHeroSlot = false
            )
        }

        // Slot 3: Passive 2
        item {
            FreeFireSkillSlotCard(
                slotLabel = "SLOT 3 • PASSIVE SKILL",
                characterName = selectedScenario.passive2Char,
                skillName = selectedScenario.passive2Name,
                perkText = selectedScenario.passive2Perk,
                badgeColor = Color(0xFFC084FC),
                icon = Icons.Default.Tune,
                isHeroSlot = false
            )
        }

        // Slot 4: Passive 3
        item {
            FreeFireSkillSlotCard(
                slotLabel = "SLOT 4 • PASSIVE SKILL",
                characterName = selectedScenario.passive3Char,
                skillName = selectedScenario.passive3Name,
                perkText = selectedScenario.passive3Perk,
                badgeColor = StatusGreen,
                icon = Icons.Default.SportsEsports,
                isHeroSlot = false
            )
        }

        // 4. PET & TACTICAL LOADOUT SLOTS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Companion Pet Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(T1Yellow.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = T1Yellow, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PET SKILL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = T1Yellow,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.sp
                                    )
                                )
                                Text(
                                    text = selectedScenario.petName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedScenario.petSkill,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        )
                    }
                }

                // Battle Loadout Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Work, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "BATTLE LOADOUT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.sp
                                    )
                                )
                                Text(
                                    text = selectedScenario.loadoutName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedScenario.loadoutTactical,
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

        // 5. BOTTOM SUMMARY: EXACT CHARACTERS EQUIPPED (Free Fire Bottom Roster)
        // User explicitly demanded: "aur sabse niche character skill ka naam sabki कौन-कौन Sa character Laga Hai"
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF131A24))
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EQUIPPED CHARACTER ROSTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            )
                        )

                        Text(
                            text = "4 SLOTS FULL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clean list of all 4 characters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CharacterNameBadge(
                            number = "1",
                            name = selectedScenario.activeCharacter,
                            isLeader = true
                        )
                        CharacterNameBadge(
                            number = "2",
                            name = selectedScenario.passive1Char,
                            isLeader = false
                        )
                        CharacterNameBadge(
                            number = "3",
                            name = selectedScenario.passive2Char,
                            isLeader = false
                        )
                        CharacterNameBadge(
                            number = "4",
                            name = selectedScenario.passive3Char,
                            isLeader = false
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { copyComboText(selectedScenario) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = T1Yellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COPY LOADOUT FOR SQUAD & CHAT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeFireSkillSlotCard(
    slotLabel: String,
    characterName: String,
    skillName: String,
    perkText: String,
    badgeColor: Color,
    icon: ImageVector,
    isHeroSlot: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(
                width = if (isHeroSlot) 1.dp else 0.5.dp,
                color = if (isHeroSlot) badgeColor else DarkBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character Photo / Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.5.dp, badgeColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = characterName.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "CHAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center details: Character Name + Skill Name + Perk
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = characterName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = skillName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                )

                Text(
                    text = perkText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    ),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Skill Icon Photo / Emblem
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = skillName,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun CharacterNameBadge(
    number: String,
    name: String,
    isLeader: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLeader) T1Yellow.copy(alpha = 0.15f) else DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isLeader) T1Yellow else DarkBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLeader) "ACTIVE" else "SLOT $number",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isLeader) T1Yellow else TextSecondary,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isLeader) T1Yellow else TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            )
        }
    }
}
