package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorUiState

@Composable
fun DisplayScreen(
    state: CalculatorUiState,
    onCopyResult: (Context) -> Unit,
    onCopyExpression: (Context) -> Unit,
    onPaste: (Context) -> Unit,
    onRecallMemory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exprScrollState = rememberScrollState()

    // Auto scroll to end of expression when updated
    LaunchedEffect(state.expression) {
        if (state.expression.isNotEmpty()) {
            exprScrollState.animateScrollTo(exprScrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceDark, SurfaceVariantDark)
                )
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("calculator_display_screen")
    ) {
        // Status Indicators (Unclosed parentheses, Memory indicator)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.memoryValue != 0.0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber),
                        modifier = Modifier
                            .clickable { onRecallMemory() }
                            .testTag("memory_status_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmark,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "M: ${state.memoryValue}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                if (state.openParenthesesCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "( ${state.openParenthesesCount} )",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Copy and Paste buttons on top-right of display
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionChip(
                    icon = Icons.Default.ContentPaste,
                    label = "پیست",
                    onClick = { onPaste(context) },
                    testTag = "paste_button"
                )
                ActionChip(
                    icon = Icons.Default.ContentCopy,
                    label = "کپی نتیجه",
                    onClick = { onCopyResult(context) },
                    testTag = "copy_result_button"
                )
                if (state.expression.isNotEmpty()) {
                    ActionChip(
                        icon = Icons.Outlined.EditNote,
                        label = "کپی فرمول",
                        onClick = { onCopyExpression(context) },
                        testTag = "copy_expression_button"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Expression View (Scrollable horizontally)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(exprScrollState),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (state.expression.isEmpty()) "0" else state.expression,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 24.sp,
                    color = if (state.expression.isEmpty()) TextDim else TextWhite,
                    textAlign = TextAlign.End
                ),
                maxLines = 1,
                modifier = Modifier.testTag("expression_text")
            )
        }

        // Live Preview Line
        AnimatedVisibility(
            visible = state.livePreview.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = state.livePreview,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = NeonEmerald,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .testTag("live_preview_text")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Result Output
        Text(
            text = state.result,
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (state.result.length > 12) 26.sp else 36.sp,
                color = if (state.isError) NeonOrange else NeonCyan,
                textAlign = TextAlign.End
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_text")
        )
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = KeypadFnBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
