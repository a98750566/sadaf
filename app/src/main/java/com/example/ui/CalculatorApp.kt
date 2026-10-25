package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.math.AngleMode
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.DisplayScreen
import com.example.ui.components.GraphScreen
import com.example.ui.components.HeaderBar
import com.example.ui.components.HistoryAndMemorySheet
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay

@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.historyList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Auto-dismiss in-app toast message
    LaunchedEffect(state.toastMessage) {
        if (state.toastMessage != null) {
            delay(2200)
            viewModel.dismissToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("calculator_main_container")
            ) {
                HeaderBar(
                    state = state,
                    onTabSelected = { viewModel.setActiveTab(it) },
                    onToggleAngle = { viewModel.toggleAngleMode() },
                    onToggleNotation = { viewModel.toggleNotationMode() },
                    onToggleHaptic = { viewModel.toggleHaptic() }
                )

                AnimatedContent(
                    targetState = state.activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    modifier = Modifier.weight(1f),
                    label = "tab_content_animation"
                ) { tab ->
                    when (tab) {
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                DisplayScreen(
                                    state = state,
                                    onCopyResult = { viewModel.copyResult(it) },
                                    onCopyExpression = { viewModel.copyExpression(it) },
                                    onPaste = { viewModel.pasteFromClipboard(it) },
                                    onRecallMemory = { viewModel.memoryRecall() },
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )

                                CalculatorKeypad(
                                    isSecondFunction = state.isSecondFunction,
                                    onKeyPress = { viewModel.onKeyPress(it) },
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        1 -> {
                            // نمودار همیشه با رادیان حساب می‌شود (مستقل از DEG/GRAD ماشین‌حساب)
                            GraphScreen(
                                functionExpr = state.graphFunction,
                                xMin = state.graphXMin,
                                xMax = state.graphXMax,
                                yMin = state.graphYMin,
                                yMax = state.graphYMax,
                                angleMode = AngleMode.RAD,
                                onFunctionChange = { viewModel.setGraphFunction(it) },
                                onZoom = { viewModel.zoomGraph(it) },
                                onReset = { viewModel.resetGraph() },
                                onPan = { dx, dy -> viewModel.panGraph(dx, dy) }
                            )
                        }

                        2 -> {
                            HistoryAndMemorySheet(
                                historyList = history,
                                memoryValue = state.memoryValue,
                                memorySlots = state.memorySlots,
                                currentResult = state.numericResult,
                                onReuseHistory = { item, useResult ->
                                    viewModel.reuseHistory(item, useResult)
                                    viewModel.setActiveTab(0)
                                },
                                onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                                onClearAllHistory = { viewModel.clearHistory() },
                                onMemoryClear = { viewModel.memoryClear() },
                                onMemoryRecall = {
                                    viewModel.memoryRecall()
                                    viewModel.setActiveTab(0)
                                },
                                onMemoryAdd = { viewModel.memoryAdd() },
                                onMemorySubtract = { viewModel.memorySubtract() },
                                onMemoryStore = { viewModel.memoryStore() },
                                onSetMemorySlot = { slot, num -> viewModel.setMemorySlot(slot, num) },
                                onCopyText = { text ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Sadaf", text))
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.toastMessage != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("floating_toast")
                ) {
                    Text(
                        text = state.toastMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeonCyan,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
