package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorUiState

@Composable
fun HeaderBar(
    state: CalculatorUiState,
    onTabSelected: (Int) -> Unit,
    onToggleAngle: () -> Unit,
    onToggleNotation: () -> Unit,
    onToggleHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Title Row with Beautiful Calligraphy "sadaf 💔"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("app_branding_row")
            ) {
                // Glowing circular icon badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonRose, NeonCyan)
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(ObsidianBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💔",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "sadaf",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = DancingScriptFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(NeonRose, Color(0xFFFF7597), NeonCyan)
                                )
                            ),
                            modifier = Modifier.testTag("sadaf_title")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "💔",
                            fontSize = 20.sp,
                            modifier = Modifier.testTag("broken_heart_sticker")
                        )
                    }
                    Text(
                        text = "ماشین حساب مهندسی فوق حرفه‌ای",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Quick mode switches & settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Angle Mode Badge (DEG / RAD / GRAD)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KeypadFnBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onToggleAngle() }
                        .testTag("toggle_angle_button")
                ) {
                    Text(
                        text = state.angleMode.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Notation Badge (STD / ENG / SCI / FRAC)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KeypadOpBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onToggleNotation() }
                        .testTag("toggle_notation_button")
                ) {
                    Text(
                        text = state.notationMode.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Haptic feedback toggle
                IconButton(
                    onClick = onToggleHaptic,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("toggle_haptic_button")
                ) {
                    Icon(
                        imageVector = if (state.hapticEnabled) Icons.Default.Vibration else Icons.Default.PhoneAndroid,
                        contentDescription = "تنظیمات ویبره کلیدها",
                        tint = if (state.hapticEnabled) NeonAmber else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Tabs (Scientific, Graph 2D, History & Memory)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianBg)
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton(
                title = "مهندسی 📐",
                selected = state.activeTab == 0,
                activeColor = NeonCyan,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "نمودار ۲D 📈",
                selected = state.activeTab == 1,
                activeColor = NeonEmerald,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "حافظه و آرشیو 💾",
                selected = state.activeTab == 2,
                activeColor = NeonRose,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    selected: Boolean,
    activeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) activeColor.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) activeColor else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) activeColor else TextMuted,
                fontSize = 12.sp
            )
        )
    }
}
