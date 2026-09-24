package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.StatusRed
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Animated signature badge displaying:
 * "MADE WITH ♥
 *        - ABHISHEK"
 * With pulsing heartbeat animation, glowing cyber borders, and neon pulse.
 */
@Composable
fun AbhishekSignature(
    modifier: Modifier = Modifier,
    isProminent: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_pulse"
    )

    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("abhishek_signature_container"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isProminent) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                CyberBlack,
                                Color(0xFF221804),
                                Color(0xFF2A1E05),
                                Color(0xFF221804),
                                CyberBlack
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0F0F12),
                                Color(0xFF1A1810),
                                Color(0xFF0F0F12)
                            )
                        )
                    }
                )
                .border(
                    width = if (isProminent) 1.5.dp else 1.dp,
                    color = if (isProminent) T1Yellow.copy(alpha = borderGlowAlpha) else DarkBorder.copy(alpha = borderGlowAlpha),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MADE WITH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = if (isProminent) 12.sp else 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Love",
                        tint = StatusRed,
                        modifier = Modifier
                            .size(if (isProminent) 18.dp else 15.dp)
                            .graphicsLayer {
                                scaleX = heartScale
                                scaleY = heartScale
                            }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "BY ABHISHEK",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = T1Yellow,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.5.sp,
                        fontSize = if (isProminent) 14.sp else 12.sp
                    )
                )
            }
        }
    }
}

/**
 * High-impact Animated Esports Hero Banner for "MADE WITH ♥ BY ABHISHEK"
 * Features expanding sonar rings, glowing heart, neon golden borders, and cyber flair.
 * Displayed on Key Auth screen and during Sensitivity Calculation.
 */
@Composable
fun AbhishekAnimatedBanner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")

    // Heartbeat pulse
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_heart_pulse"
    )

    // Echo ring expansion
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_ring_scale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_ring_alpha"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("abhishek_animated_hero_banner"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF201604),
                            Color(0xFF141005),
                            Color(0xFF0A0A0E)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            T1Yellow.copy(alpha = glowAlpha),
                            CyberCyan.copy(alpha = glowAlpha * 0.7f),
                            StatusRed.copy(alpha = glowAlpha * 0.8f),
                            T1Yellow.copy(alpha = glowAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Heart with expanding sonar wave rings
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = StatusRed.copy(alpha = ringAlpha),
                            radius = (size.minDimension / 2) * ringScale,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love",
                            tint = StatusRed,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = heartScale
                                    scaleY = heartScale
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = T1Yellow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MADE WITH LOVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.5.sp,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = T1Yellow,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "BY ABHISHEK",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = T1Yellow,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "T1 ESPORTS EXCLUSIVE HEADSHOT ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberCyan.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}
