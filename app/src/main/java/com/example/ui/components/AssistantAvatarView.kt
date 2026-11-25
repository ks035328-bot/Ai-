package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCard
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.AvatarMood

@Composable
fun AssistantAvatarView(
    personaName: String,
    personaTitle: String,
    mood: AvatarMood,
    speechBubbleText: String,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "haloRotation"
    )

    val avatarScale = remember { Animatable(1f) }

    LaunchedEffect(mood) {
        if (mood == AvatarMood.HAPPY) {
            avatarScale.animateTo(1.1f, animationSpec = tween(150))
            avatarScale.animateTo(1.0f, animationSpec = tween(150))
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("assistant_avatar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Frame with Halo Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(if (mood == AvatarMood.LISTENING || mood == AvatarMood.SPEAKING) pulseScale else 1f)
                        .scale(avatarScale.value)
                        .clickable { onAvatarClick() }
                ) {
                    // Outer Holographic Aura
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        CyberCyan,
                                        NeonPurple,
                                        HotPink,
                                        CyberCyan
                                    )
                                )
                            )
                    )

                    // Inner Avatar Image
                    Image(
                        painter = painterResource(
                            id = if (mood == AvatarMood.SPEAKING || mood == AvatarMood.HAPPY) {
                                R.drawable.img_assistant_avatar_speaking_1785254707706
                            } else {
                                R.drawable.img_assistant_avatar_idle_1785254692109
                            }
                        ),
                        contentDescription = "AI Assistant Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    )

                    // Mood Tag Indicator Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(
                                when (mood) {
                                    AvatarMood.LISTENING -> HotPink
                                    AvatarMood.SPEAKING -> CyberCyan
                                    AvatarMood.THINKING -> NeonPurple
                                    AvatarMood.HAPPY -> Color(0xFF00E676)
                                    AvatarMood.NEUTRAL -> DarkCard
                                },
                                CircleShape
                            )
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (mood) {
                                AvatarMood.LISTENING -> Icons.Default.RecordVoiceOver
                                AvatarMood.SPEAKING -> Icons.Default.VolumeUp
                                else -> Icons.Default.AutoAwesome
                            },
                            contentDescription = "Status Icon",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name and Mood Status Text
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = personaName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00E676)
                        )
                    }

                    Text(
                        text = personaTitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (mood) {
                                    AvatarMood.LISTENING -> HotPink.copy(alpha = 0.25f)
                                    AvatarMood.THINKING -> NeonPurple.copy(alpha = 0.25f)
                                    AvatarMood.SPEAKING -> CyberCyan.copy(alpha = 0.25f)
                                    AvatarMood.HAPPY -> Color(0xFF00E676).copy(alpha = 0.25f)
                                    AvatarMood.NEUTRAL -> Color.White.copy(alpha = 0.10f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (mood) {
                                AvatarMood.LISTENING -> " Listening to voice..."
                                AvatarMood.THINKING -> " Parsing desktop command..."
                                AvatarMood.SPEAKING -> " Speaking response..."
                                AvatarMood.HAPPY -> " Action completed!"
                                AvatarMood.NEUTRAL -> " Ready for command"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (mood) {
                                AvatarMood.LISTENING -> HotPink
                                AvatarMood.THINKING -> NeonPurple
                                AvatarMood.SPEAKING -> CyberCyan
                                AvatarMood.HAPPY -> Color(0xFF00E676)
                                AvatarMood.NEUTRAL -> Color.White
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speech Bubble Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161C2A))
                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = speechBubbleText,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
