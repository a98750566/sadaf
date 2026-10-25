package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CalculatorKeypad(
    isSecondFunction: Boolean,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Scientific Functions Panel (Compact 3 rows) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark.copy(alpha = 0.85f))
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sci Row 1: Memory & Function toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SciKey(
                    label = if (isSecondFunction) "1st" else "2nd",
                    subLabel = "SHIFT",
                    isHighlighted = isSecondFunction,
                    activeColor = NeonAmber,
                    onClick = { onKeyPress("2nd") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_second_fn"
                )
                SciKey(
                    label = "MC",
                    onClick = { onKeyPress("MC") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_mc"
                )
                SciKey(
                    label = "MR",
                    onClick = { onKeyPress("MR") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_mr"
                )
                SciKey(
                    label = "M+",
                    onClick = { onKeyPress("M+") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_m_plus"
                )
                SciKey(
                    label = "M-",
                    onClick = { onKeyPress("M-") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_m_minus"
                )
                SciKey(
                    label = "MS",
                    onClick = { onKeyPress("MS") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_ms"
                )
            }

            // Sci Row 2: Trig & Powers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SciKey(
                    label = if (isSecondFunction) "sin⁻¹" else "sin",
                    onClick = { onKeyPress(if (isSecondFunction) "sin⁻¹" else "sin") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_sin"
                )
                SciKey(
                    label = if (isSecondFunction) "cos⁻¹" else "cos",
                    onClick = { onKeyPress(if (isSecondFunction) "cos⁻¹" else "cos") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_cos"
                )
                SciKey(
                    label = if (isSecondFunction) "tan⁻¹" else "tan",
                    onClick = { onKeyPress(if (isSecondFunction) "tan⁻¹" else "tan") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_tan"
                )
                SciKey(
                    label = "π",
                    onClick = { onKeyPress("π") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_pi"
                )
                SciKey(
                    label = "e",
                    onClick = { onKeyPress("e") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_e"
                )
                SciKey(
                    label = if (isSecondFunction) "x²" else "√",
                    onClick = { onKeyPress(if (isSecondFunction) "x²" else "√") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_sqrt"
                )
            }

            // Sci Row 3: Logs & Parentheses & Powers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SciKey(
                    label = if (isSecondFunction) "eˣ" else "ln",
                    onClick = { onKeyPress(if (isSecondFunction) "eˣ" else "ln") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_ln"
                )
                SciKey(
                    label = if (isSecondFunction) "10ˣ" else "log",
                    onClick = { onKeyPress(if (isSecondFunction) "10ˣ" else "log") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_log"
                )
                SciKey(
                    label = "^",
                    subLabel = "xʸ",
                    onClick = { onKeyPress("xʸ") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_pow"
                )
                SciKey(
                    label = "(",
                    onClick = { onKeyPress("(") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_open_paren"
                )
                SciKey(
                    label = ")",
                    onClick = { onKeyPress(")") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_close_paren"
                )
                SciKey(
                    label = "1/x",
                    onClick = { onKeyPress("1/x") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_reciprocal"
                )
            }
        }

        // --- Main Keypad: 4 Columns Grid ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, C, DEL, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainKey(
                    label = "AC",
                    textColor = NeonOrange,
                    bgColor = KeypadFnBg,
                    borderColor = NeonOrange.copy(alpha = 0.5f),
                    onClick = { onKeyPress("AC") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_ac"
                )
                MainKey(
                    label = "C",
                    textColor = NeonOrange,
                    bgColor = KeypadFnBg,
                    onClick = { onKeyPress("C") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_c"
                )
                MainKey(
                    label = "DEL",
                    textColor = TextWhite,
                    bgColor = KeypadFnBg,
                    onClick = { onKeyPress("DEL") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_del"
                )
                MainKey(
                    label = "÷",
                    textColor = NeonCyan,
                    bgColor = KeypadOpBg,
                    borderColor = NeonCyan.copy(alpha = 0.4f),
                    fontSize = 24.sp,
                    onClick = { onKeyPress("÷") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_divide"
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainKey(
                    label = "7",
                    onClick = { onKeyPress("7") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_7"
                )
                MainKey(
                    label = "8",
                    onClick = { onKeyPress("8") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_8"
                )
                MainKey(
                    label = "9",
                    onClick = { onKeyPress("9") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_9"
                )
                MainKey(
                    label = "×",
                    textColor = NeonCyan,
                    bgColor = KeypadOpBg,
                    borderColor = NeonCyan.copy(alpha = 0.4f),
                    fontSize = 24.sp,
                    onClick = { onKeyPress("×") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_multiply"
                )
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainKey(
                    label = "4",
                    onClick = { onKeyPress("4") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_4"
                )
                MainKey(
                    label = "5",
                    onClick = { onKeyPress("5") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_5"
                )
                MainKey(
                    label = "6",
                    onClick = { onKeyPress("6") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_6"
                )
                MainKey(
                    label = "−",
                    textColor = NeonCyan,
                    bgColor = KeypadOpBg,
                    borderColor = NeonCyan.copy(alpha = 0.4f),
                    fontSize = 24.sp,
                    onClick = { onKeyPress("-") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_subtract"
                )
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainKey(
                    label = "1",
                    onClick = { onKeyPress("1") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_1"
                )
                MainKey(
                    label = "2",
                    onClick = { onKeyPress("2") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_2"
                )
                MainKey(
                    label = "3",
                    onClick = { onKeyPress("3") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_3"
                )
                MainKey(
                    label = "+",
                    textColor = NeonCyan,
                    bgColor = KeypadOpBg,
                    borderColor = NeonCyan.copy(alpha = 0.4f),
                    fontSize = 24.sp,
                    onClick = { onKeyPress("+") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_add"
                )
            }

            // Row 5: +/-, 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainKey(
                    label = "+/-",
                    textColor = TextMuted,
                    onClick = { onKeyPress("+/-") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_plus_minus"
                )
                MainKey(
                    label = "0",
                    onClick = { onKeyPress("0") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_0"
                )
                MainKey(
                    label = ".",
                    fontSize = 22.sp,
                    onClick = { onKeyPress(".") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_dot"
                )
                // Hero equals key with glowing Neon Rose gradient
                MainKey(
                    label = "=",
                    textColor = TextWhite,
                    bgColor = NeonRose,
                    isHero = true,
                    fontSize = 26.sp,
                    onClick = { onKeyPress("=") },
                    modifier = Modifier.weight(1f),
                    testTag = "key_equals"
                )
            }
        }
    }
}

@Composable
private fun SciKey(
    label: String,
    subLabel: String? = null,
    isHighlighted: Boolean = false,
    activeColor: Color = NeonCyan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val bg = if (isHighlighted) activeColor.copy(alpha = 0.25f) else KeypadFnBg
    val border = if (isHighlighted) activeColor else BorderSubtle

    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = activeColor),
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = if (isHighlighted) activeColor else TextWhite
                ),
                textAlign = TextAlign.Center
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        color = activeColor
                    )
                )
            }
        }
    }
}

@Composable
private fun MainKey(
    label: String,
    textColor: Color = TextWhite,
    bgColor: Color = KeypadNumBg,
    borderColor: Color = BorderSubtle,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    isHero: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val finalBg = if (isHero) {
        Brush.linearGradient(colors = listOf(NeonRose, Color(0xFFFF5288)))
    } else {
        Brush.linearGradient(colors = listOf(bgColor, bgColor))
    }

    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(if (isHero) 6.dp else 2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(finalBg)
            .border(1.dp, if (isHero) NeonRose else borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isHero) Color.White else NeonCyan),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )
    }
}
