package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationItem
import com.example.math.EngineeringFormatter
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryAndMemorySheet(
    historyList: List<CalculationItem>,
    memoryValue: Double,
    memorySlots: Map<String, Double>,
    currentResult: Double,
    onReuseHistory: (CalculationItem, Boolean) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    onSetMemorySlot: (String, Double) -> Unit,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: History, 1: Memory

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-Tab Switcher (History vs Memory)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubTab == 0) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { selectedSubTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "تاریخچه محاسبات (${historyList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (selectedSubTab == 0) NeonCyan else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSubTab == 1) NeonAmber.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { selectedSubTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ثبات‌های حافظه (Memory)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (selectedSubTab == 1) NeonAmber else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (selectedSubTab == 0) {
            // --- HISTORY LIST ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آرشیو محاسبات انجام شده",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
                if (historyList.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAllHistory,
                        modifier = Modifier.testTag("clear_all_history_button")
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "پاکسازی همه",
                            tint = NeonOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "پاکسازی همه",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonOrange,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.HistoryEdu,
                            contentDescription = null,
                            tint = TextDim,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هنوز هیچ محاسبه‌ای ذخیره نشده است",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextDim)
                        )
                        Text(
                            text = "با زدن دکمه = محاسبات اینجا ثبت می‌شوند",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextDim, fontSize = 11.sp)
                        )
                    }
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyList, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            dateString = dateFormat.format(Date(item.timestamp)),
                            onReuseExpr = { onReuseHistory(item, false) },
                            onReuseResult = { onReuseHistory(item, true) },
                            onDelete = { onDeleteHistoryItem(item.id) },
                            onCopy = onCopyText
                        )
                    }
                }
            }
        } else {
            // --- MEMORY INSPECTOR ---
            MemoryInspectorPanel(
                memoryValue = memoryValue,
                memorySlots = memorySlots,
                currentResult = currentResult,
                onMemoryClear = onMemoryClear,
                onMemoryRecall = onMemoryRecall,
                onMemoryAdd = onMemoryAdd,
                onMemorySubtract = onMemorySubtract,
                onMemoryStore = onMemoryStore,
                onSetMemorySlot = onSetMemorySlot,
                onCopy = onCopyText
            )
        }
    }
}

@Composable
private fun HistoryCard(
    item: CalculationItem,
    dateString: String,
    onReuseExpr: () -> Unit,
    onReuseResult: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.angleMode} • $dateString",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextDim, fontSize = 10.sp)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "حذف مورد",
                        tint = TextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expression
            Text(
                text = item.expression,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = RobotoMonoFontFamily,
                    color = TextWhite,
                    fontSize = 16.sp
                ),
                modifier = Modifier.clickable { onReuseExpr() }
            )

            // Result
            Text(
                text = "= ${item.result}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 18.sp
                ),
                modifier = Modifier.clickable { onReuseResult() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onCopy(item.expression) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("کپی فرمول", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontSize = 10.sp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = { onCopy(item.result) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("کپی نتیجه", style = MaterialTheme.typography.labelSmall.copy(color = NeonEmerald, fontSize = 10.sp))
                }
            }
        }
    }
}

@Composable
private fun MemoryInspectorPanel(
    memoryValue: Double,
    memorySlots: Map<String, Double>,
    currentResult: Double,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    onSetMemorySlot: (String, Double) -> Unit,
    onCopy: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Memory Register Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ثبات اصلی حافظه (M)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(
                        onClick = { onCopy(memoryValue.toString()) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی مقدار حافظه", tint = NeonAmber, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = EngineeringFormatter.formatStandard(memoryValue),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = RobotoMonoFontFamily,
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Standard Memory Buttons: MC, MR, M+, M-, MS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MemoryActionButton(label = "MC", onClick = onMemoryClear, modifier = Modifier.weight(1f))
                    MemoryActionButton(label = "MR", onClick = onMemoryRecall, modifier = Modifier.weight(1f))
                    MemoryActionButton(label = "M+", onClick = onMemoryAdd, modifier = Modifier.weight(1f))
                    MemoryActionButton(label = "M-", onClick = onMemorySubtract, modifier = Modifier.weight(1f))
                    MemoryActionButton(label = "MS", onClick = onMemoryStore, modifier = Modifier.weight(1f), isHighlighted = true)
                }
            }
        }

        // Secondary Slots (M1, M2, M3)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "خانه‌های حافظه اضافی (Slots)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                memorySlots.forEach { (slot, valNum) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = slot,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = EngineeringFormatter.formatStandard(valNum),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = RobotoMonoFontFamily,
                                    color = TextWhite
                                )
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onSetMemorySlot(slot, currentResult) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("ذخیره نتیجه", fontSize = 10.sp)
                            }
                            IconButton(
                                onClick = { onCopy(valNum.toString()) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "کپی", tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun MemoryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlighted) NeonAmber.copy(alpha = 0.25f) else KeypadFnBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlighted) NeonAmber else BorderSubtle
        ),
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isHighlighted) NeonAmber else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}
