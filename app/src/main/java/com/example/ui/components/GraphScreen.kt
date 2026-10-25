package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleMode
import com.example.math.MathEvaluator
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GraphScreen(
    functionExpr: String,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    angleMode: AngleMode,
    onFunctionChange: (String) -> Unit,
    onZoom: (Boolean) -> Unit,
    onReset: () -> Unit,
    onPan: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var textInput by remember(functionExpr) { mutableStateOf(functionExpr) }
    var inspectedPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var inspectedCanvasOffset by remember { mutableStateOf<Offset?>(null) }

    val presetFunctions = listOf(
        "sin(x)",
        "cos(x)",
        "x^2 - 4",
        "sin(x) * x",
        "2^x",
        "abs(x)",
        "1/x",
        "sqrt(abs(x))"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Function input & Evaluate button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "f(x) =",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = {
                            textInput = it
                            onFunctionChange(it)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = BorderSubtle,
                            cursorColor = NeonEmerald
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("graph_function_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Preset chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetFunctions.forEach { fn ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (functionExpr == fn) NeonEmerald.copy(alpha = 0.2f) else KeypadFnBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (functionExpr == fn) NeonEmerald else BorderSubtle
                            ),
                            modifier = Modifier.clickable {
                                textInput = fn
                                onFunctionChange(fn)
                            }
                        ) {
                            Text(
                                text = fn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (functionExpr == fn) NeonEmerald else TextWhite
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Graph Plane Canvas Box with Overlay Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                .testTag("graph_canvas_box")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(functionExpr, xMin, xMax, yMin, yMax) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val canvasWidth = size.width.toFloat()
                                val canvasHeight = size.height.toFloat()
                                val touchX = change.position.x.coerceIn(0f, canvasWidth)

                                val mathX = xMin + (touchX / canvasWidth) * (xMax - xMin)
                                try {
                                    val mathY = MathEvaluator.evaluate(functionExpr, angleMode, mathX)
                                    if (!mathY.isNaN() && !mathY.isInfinite()) {
                                        val normY = (mathY - yMin) / (yMax - yMin)
                                        val canvasY = (canvasHeight * (1.0 - normY)).toFloat()
                                        inspectedPoint = Pair(mathX, mathY)
                                        inspectedCanvasOffset = Offset(touchX, canvasY.coerceIn(0f, canvasHeight))
                                    }
                                } catch (_: Exception) {}
                            },
                            onDragEnd = {
                                // keep last point
                            }
                        )
                    }
                    .pointerInput(functionExpr, xMin, xMax, yMin, yMax) {
                        detectTapGestures { offset ->
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            val touchX = offset.x.coerceIn(0f, canvasWidth)

                            val mathX = xMin + (touchX / canvasWidth) * (xMax - xMin)
                            try {
                                val mathY = MathEvaluator.evaluate(functionExpr, angleMode, mathX)
                                if (!mathY.isNaN() && !mathY.isInfinite()) {
                                    val normY = (mathY - yMin) / (yMax - yMin)
                                    val canvasY = (canvasHeight * (1.0 - normY)).toFloat()
                                    inspectedPoint = Pair(mathX, mathY)
                                    inspectedCanvasOffset = Offset(touchX, canvasY.coerceIn(0f, canvasHeight))
                                }
                            } catch (_: Exception) {}
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Calculate axes pixel coordinates
                val originX = ((0.0 - xMin) / (xMax - xMin) * w).toFloat()
                val originY = ((1.0 - (0.0 - yMin) / (yMax - yMin)) * h).toFloat()

                // 1. Draw Grid Lines
                val xStep = (xMax - xMin) / 8.0
                var currX = xMin
                while (currX <= xMax) {
                    val px = ((currX - xMin) / (xMax - xMin) * w).toFloat()
                    drawLine(
                        color = Color(0x1A00E5FF),
                        start = Offset(px, 0f),
                        end = Offset(px, h),
                        strokeWidth = 1f
                    )
                    currX += xStep
                }

                val yStep = (yMax - yMin) / 8.0
                var currY = yMin
                while (currY <= yMax) {
                    val py = ((1.0 - (currY - yMin) / (yMax - yMin)) * h).toFloat()
                    drawLine(
                        color = Color(0x1A00E5FF),
                        start = Offset(0f, py),
                        end = Offset(w, py),
                        strokeWidth = 1f
                    )
                    currY += yStep
                }

                // 2. Draw Main Axes (X and Y)
                if (originX in 0f..w) {
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.6f),
                        start = Offset(originX, 0f),
                        end = Offset(originX, h),
                        strokeWidth = 2f
                    )
                }
                if (originY in 0f..h) {
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.6f),
                        start = Offset(0f, originY),
                        end = Offset(w, originY),
                        strokeWidth = 2f
                    )
                }

                // 3. Plot Function Curve
                val path = Path()
                var isStarted = false
                var prevPy = 0f
                val steps = (w / 2).toInt().coerceAtLeast(100)
                val dx = (xMax - xMin) / steps

                for (i in 0..steps) {
                    val xVal = xMin + i * dx
                    try {
                        val yVal = MathEvaluator.evaluate(functionExpr, angleMode, xVal)
                        if (!yVal.isNaN() && !yVal.isInfinite()) {
                            val px = (i.toFloat() / steps.toFloat()) * w
                            val normY = (yVal - yMin) / (yMax - yMin)
                            val py = (h * (1.0 - normY)).toFloat()

                            if (py in -h..(h * 2)) {
                                // اگر خط ناگهان بیشتر از ارتفاع بوم بپرد (مجانب، مثل tan)، خط را قطع می‌کنیم
                                if (!isStarted || abs(py - prevPy) > h) {
                                    path.moveTo(px, py)
                                    isStarted = true
                                } else {
                                    path.lineTo(px, py)
                                }
                                prevPy = py
                            } else {
                                isStarted = false
                            }
                        } else {
                            isStarted = false
                        }
                    } catch (_: Exception) {
                        isStarted = false
                    }
                }

                // Draw curve with vibrant glow
                drawPath(
                    path = path,
                    color = NeonEmerald,
                    style = Stroke(width = 3.5f)
                )

                // 4. Draw Inspected Point & Crosshair
                inspectedCanvasOffset?.let { offset ->
                    drawLine(
                        color = NeonRose.copy(alpha = 0.7f),
                        start = Offset(offset.x, 0f),
                        end = Offset(offset.x, h),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = NeonRose.copy(alpha = 0.7f),
                        start = Offset(0f, offset.y),
                        end = Offset(w, offset.y),
                        strokeWidth = 1.5f
                    )
                    drawCircle(
                        color = NeonRose,
                        radius = 6f,
                        center = offset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = offset
                    )
                }
            }

            // Top-left coordinate readout
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ObsidianBg.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    if (inspectedPoint != null) {
                        val (x, y) = inspectedPoint!!
                        Text(
                            text = "x: ${"%.2f".format(x)} | y: ${"%.2f".format(y)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonRose,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Text(
                            text = "X: [${"%.0f".format(xMin)}, ${"%.0f".format(xMax)}] | Y: [${"%.0f".format(yMin)}, ${"%.0f".format(yMax)}]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Pan Controls on Bottom-Left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple("\u25C0", -0.25 to 0.0, "graph_pan_left"),
                    Triple("\u25B6", 0.25 to 0.0, "graph_pan_right"),
                    Triple("\u25B2", 0.0 to 0.25, "graph_pan_up"),
                    Triple("\u25BC", 0.0 to -0.25, "graph_pan_down")
                ).forEach { (label, delta, tag) ->
                    IconButton(
                        onClick = { onPan(delta.first, delta.second) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(KeypadFnBg)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag(tag)
                    ) {
                        Text(text = label, color = NeonCyan, fontSize = 12.sp)
                    }
                }
            }

            // Zoom & Reset Controls on Bottom-Right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onZoom(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KeypadFnBg)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .testTag("graph_zoom_in")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "بزرگ‌نمایی", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = { onZoom(false) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KeypadFnBg)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .testTag("graph_zoom_out")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "کوچک‌نمایی", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KeypadFnBg)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .testTag("graph_reset")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "بازنشانی نما", tint = NeonEmerald, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
