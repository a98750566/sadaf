package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalculationItem
import com.example.math.AngleMode
import com.example.math.CalcException
import com.example.math.EngineeringFormatter
import com.example.math.MathEvaluator
import com.example.math.NotationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "0",
    val livePreview: String = "",
    // مقدار فعلیِ عبارت (با هر تایپ به‌روز می‌شود)؛ برای حافظه استفاده می‌شود
    val numericResult: Double = 0.0,
    // مقدار نتیجه‌ای که در بخش result نمایش داده شده
    val lastResult: Double = 0.0,
    // بعد از زدن «=» true می‌شود تا ورودی بعدی محاسبه تازه شروع کند
    val justEvaluated: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val notationMode: NotationMode = NotationMode.STD,
    val isSecondFunction: Boolean = false,
    val memoryValue: Double = 0.0,
    val memorySlots: Map<String, Double> = mapOf("M1" to 0.0, "M2" to 0.0, "M3" to 0.0),
    val activeTab: Int = 0, // 0: Scientific, 1: Graph 2D, 2: History & Memory
    val graphFunction: String = "sin(x)",
    val graphXMin: Double = -10.0,
    val graphXMax: Double = 10.0,
    val graphYMin: Double = -5.0,
    val graphYMax: Double = 5.0,
    val hapticEnabled: Boolean = true,
    val toastMessage: String? = null,
    val isError: Boolean = false,
    val openParenthesesCount: Int = 0
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.calculationDao()
    private val prefs = application.getSharedPreferences("sadaf_calculator_prefs", Context.MODE_PRIVATE)

    val historyList: StateFlow<List<CalculationItem>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // ---------- ذخیره و بازیابی تنظیمات و حافظه ----------

    private fun loadInitialState(): CalculatorUiState {
        fun readDouble(key: String): Double = prefs.getString(key, null)?.toDoubleOrNull() ?: 0.0
        val angle = try {
            AngleMode.valueOf(prefs.getString("angle", "DEG") ?: "DEG")
        } catch (_: Exception) {
            AngleMode.DEG
        }
        val notation = try {
            NotationMode.valueOf(prefs.getString("notation", "STD") ?: "STD")
        } catch (_: Exception) {
            NotationMode.STD
        }
        return CalculatorUiState(
            angleMode = angle,
            notationMode = notation,
            memoryValue = readDouble("mem"),
            memorySlots = mapOf(
                "M1" to readDouble("M1"),
                "M2" to readDouble("M2"),
                "M3" to readDouble("M3")
            ),
            hapticEnabled = prefs.getBoolean("haptic", true)
        )
    }

    private fun persist() {
        val s = _uiState.value
        prefs.edit()
            .putString("angle", s.angleMode.name)
            .putString("notation", s.notationMode.name)
            .putString("mem", s.memoryValue.toString())
            .putString("M1", (s.memorySlots["M1"] ?: 0.0).toString())
            .putString("M2", (s.memorySlots["M2"] ?: 0.0).toString())
            .putString("M3", (s.memorySlots["M3"] ?: 0.0).toString())
            .putBoolean("haptic", s.hapticEnabled)
            .apply()
    }

    private fun triggerHaptic() {
        if (!_uiState.value.hapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun onKeyPress(key: String) {
        triggerHaptic()
        when (key) {
            "AC" -> clearAll()
            "C" -> clearEntry()
            "DEL" -> deleteLast()
            "=" -> calculate(saveToHistory = true)
            "+/-" -> toggleSign()
            "2nd" -> toggleSecondFunction()
            "DEG/RAD" -> toggleAngleMode()
            "NOTATION" -> toggleNotationMode()
            "MC" -> memoryClear()
            "MR" -> memoryRecall()
            "M+" -> memoryAdd()
            "M-" -> memorySubtract()
            "MS" -> memoryStore()
            "(" -> append("(")
            ")" -> append(")")
            "π" -> append("π")
            "e" -> append("e")
            "%" -> append("%")
            "!" -> append("!")
            "1/x" -> appendReciprocal()
            "x²" -> append("^2")
            "x³" -> append("^3")
            "xʸ" -> append("^")
            "√" -> append("√(")
            "∛" -> append("∛(")
            "10ˣ" -> append("10^(")
            "eˣ" -> append("exp(")
            "sin" -> append("sin(")
            "cos" -> append("cos(")
            "tan" -> append("tan(")
            "sin⁻¹" -> append("sin⁻¹(")
            "cos⁻¹" -> append("cos⁻¹(")
            "tan⁻¹" -> append("tan⁻¹(")
            "sinh" -> append("sinh(")
            "cosh" -> append("cosh(")
            "tanh" -> append("tanh(")
            "ln" -> append("ln(")
            "log" -> append("log(")
            "log2" -> append("log2(")
            "|x|" -> append("abs(")
            else -> append(key)
        }
    }

    // ---------- ویرایش عبارت ----------

    private companion object {
        // + − × ÷
        const val BIN_OPS = "+\u2212\u00D7\u00F7"

        // اگر بعد از «=» یکی از این‌ها زده شود، محاسبه با نتیجه ادامه پیدا می‌کند
        const val CONTINUATION_CHARS = "+\u2212-\u00D7\u00F7*/^%!"
    }

    private fun autoClose(expr: String): String {
        return expr + ")".repeat(countUnbalancedOpenParens(expr))
    }

    fun append(text: String) {
        val st = _uiState.value
        var base = st.expression

        if (st.justEvaluated) {
            // بعد از «=»: عملگر → ادامه با نتیجه، هر چیز دیگر → محاسبه تازه
            base = if (text.isNotEmpty() && text[0] in CONTINUATION_CHARS) {
                val plain = EngineeringFormatter.formatPlain(st.lastResult)
                if (plain.startsWith("-")) "($plain)" else plain
            } else {
                ""
            }
        }

        // جایگزینی عملگر تکراری (مثلاً 5+ و بعد ×  →  5×)
        if (text.length == 1 && text[0] in BIN_OPS && base.isNotEmpty()) {
            val last = base.last()
            if (last in BIN_OPS && (text != "\u2212" || last == '+' || last == '\u2212')) {
                base = base.dropLast(1)
            }
        }

        val updated = base + text
        val openCount = countUnbalancedOpenParens(updated)
        _uiState.update {
            it.copy(
                expression = updated,
                openParenthesesCount = openCount,
                isError = false,
                justEvaluated = false
            )
        }
        updateLivePreview(updated)
    }

    private fun countUnbalancedOpenParens(expr: String): Int {
        var open = 0
        for (c in expr) {
            if (c == '(') open++
            else if (c == ')') {
                if (open > 0) open--
            }
        }
        return open
    }

    private fun deleteLast() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            // توابع چندحرفی را یک‌جا پاک می‌کنیم
            val candidates = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sinh(", "cosh(", "tanh(",
                "sin(", "cos(", "tan(", "log2(", "log(", "ln(",
                "exp(", "abs(", "10^(", "√(", "∛("
            )
            var newExpr: String? = null
            for (fn in candidates) {
                if (current.endsWith(fn)) {
                    newExpr = current.removeSuffix(fn)
                    break
                }
            }
            val finalExpr = newExpr ?: current.dropLast(1)
            val openCount = countUnbalancedOpenParens(finalExpr)
            _uiState.update {
                it.copy(
                    expression = finalExpr,
                    openParenthesesCount = openCount,
                    isError = false,
                    justEvaluated = false
                )
            }
            updateLivePreview(finalExpr)
        }
    }

    private fun clearAll() {
        _uiState.update {
            it.copy(
                expression = "",
                result = "0",
                livePreview = "",
                numericResult = 0.0,
                lastResult = 0.0,
                openParenthesesCount = 0,
                isError = false,
                justEvaluated = false
            )
        }
    }

    private fun clearEntry() {
        _uiState.update {
            it.copy(
                expression = "",
                openParenthesesCount = 0,
                livePreview = "",
                isError = false,
                justEvaluated = false
            )
        }
    }

    // کل عبارت را منفی/مثبت می‌کند (نه فقط جمله اول)
    private fun toggleSign() {
        val current = _uiState.value.expression
        if (current.isEmpty()) {
            append("-")
            return
        }
        val updated = negateExpression(current)
        _uiState.update {
            it.copy(
                expression = updated,
                openParenthesesCount = countUnbalancedOpenParens(updated),
                isError = false,
                justEvaluated = false
            )
        }
        updateLivePreview(updated)
    }

    private fun negateExpression(e: String): String {
        // عدد ساده: فقط علامت را برگردان
        if (Regex("^-?[0-9.]+$").matches(e)) {
            return if (e.startsWith("-")) e.substring(1) else "-$e"
        }
        // -(...) که پرانتزش کل عبارت را می‌پوشاند: پرانتز را بردار
        if (e.startsWith("-(") && e.endsWith(")")) {
            val inner = e.substring(2, e.length - 1)
            var depth = 0
            var wraps = true
            for (c in inner) {
                if (c == '(') depth++
                else if (c == ')') {
                    depth--
                    if (depth < 0) {
                        wraps = false
                        break
                    }
                }
            }
            if (wraps && depth == 0) return inner
        }
        return "-($e)"
    }

    private fun appendReciprocal() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            val updated = "1/(" + autoClose(current) + ")"
            _uiState.update {
                it.copy(
                    expression = updated,
                    openParenthesesCount = 0,
                    isError = false,
                    justEvaluated = false
                )
            }
            updateLivePreview(updated)
        } else {
            append("1/(")
        }
    }

    private fun updateLivePreview(expr: String) {
        if (expr.isBlank()) {
            _uiState.update { it.copy(livePreview = "") }
            return
        }
        try {
            val num = MathEvaluator.evaluate(autoClose(expr), _uiState.value.angleMode)
            val formatted = EngineeringFormatter.format(num, _uiState.value.notationMode)
            _uiState.update { it.copy(livePreview = "= $formatted", numericResult = num) }
        } catch (_: Exception) {
            _uiState.update { it.copy(livePreview = "") }
        }
    }

    fun calculate(saveToHistory: Boolean = true) {
        val rawExpr = _uiState.value.expression
        if (rawExpr.isBlank()) return

        val expr = autoClose(rawExpr)
        val angle = _uiState.value.angleMode

        try {
            val num = MathEvaluator.evaluate(expr, angle)
            val formatted = EngineeringFormatter.format(num, _uiState.value.notationMode)

            _uiState.update {
                it.copy(
                    expression = expr,
                    result = formatted,
                    numericResult = num,
                    lastResult = num,
                    livePreview = "",
                    openParenthesesCount = 0,
                    isError = false,
                    justEvaluated = true
                )
            }

            if (saveToHistory) {
                // اگر دقیقاً همین محاسبه آخرین مورد تاریخچه است، دوباره ذخیره نمی‌کنیم
                val last = historyList.value.firstOrNull()
                val duplicate = last != null && last.expression == expr && last.angleMode == angle.name
                if (!duplicate) {
                    viewModelScope.launch {
                        dao.insert(
                            CalculationItem(
                                expression = expr,
                                result = formatted,
                                angleMode = angle.name
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            val errorMsg = if (e is CalcException) (e.message ?: "خطا در محاسبه") else "عبارت نامعتبر است"
            _uiState.update {
                it.copy(
                    result = errorMsg,
                    isError = true,
                    livePreview = "",
                    justEvaluated = false
                )
            }
        }
    }

    fun toggleAngleMode() {
        triggerHaptic()
        val next = when (_uiState.value.angleMode) {
            AngleMode.DEG -> AngleMode.RAD
            AngleMode.RAD -> AngleMode.GRAD
            AngleMode.GRAD -> AngleMode.DEG
        }
        _uiState.update { it.copy(angleMode = next) }
        persist()
        updateLivePreview(_uiState.value.expression)
        showToast("حالت زاویه: ${next.name}")
    }

    fun toggleNotationMode() {
        triggerHaptic()
        val next = when (_uiState.value.notationMode) {
            NotationMode.STD -> NotationMode.ENG
            NotationMode.ENG -> NotationMode.SCI
            NotationMode.SCI -> NotationMode.FRAC
            NotationMode.FRAC -> NotationMode.STD
        }
        _uiState.update {
            val formatted = if (!it.isError) {
                EngineeringFormatter.format(it.lastResult, next)
            } else {
                it.result
            }
            it.copy(notationMode = next, result = formatted)
        }
        persist()
        updateLivePreview(_uiState.value.expression)
        val modeFa = when (next) {
            NotationMode.STD -> "استاندارد (Standard)"
            NotationMode.ENG -> "مهندسی (Engineering)"
            NotationMode.SCI -> "علمی (Scientific)"
            NotationMode.FRAC -> "کسری (Fraction)"
        }
        showToast("فرمت نمایش: $modeFa")
    }

    fun toggleSecondFunction() {
        triggerHaptic()
        _uiState.update { it.copy(isSecondFunction = !it.isSecondFunction) }
    }

    // ---------- حافظه (M+, M-, MR, MC, MS) ----------

    // مقدار فعلی عبارت؛ اگر عبارت ناقص/نامعتبر باشد null
    private fun currentValue(): Double? {
        val st = _uiState.value
        if (st.justEvaluated || st.expression.isBlank()) return st.lastResult
        return try {
            MathEvaluator.evaluate(autoClose(st.expression), st.angleMode)
        } catch (_: Exception) {
            null
        }
    }

    fun memoryAdd() {
        triggerHaptic()
        val current = currentValue()
        if (current == null) {
            showToast("ابتدا یک عبارت معتبر وارد کنید")
            return
        }
        val newMem = _uiState.value.memoryValue + current
        _uiState.update { it.copy(memoryValue = newMem) }
        persist()
        showToast("M+ : ${EngineeringFormatter.format(newMem)}")
    }

    fun memorySubtract() {
        triggerHaptic()
        val current = currentValue()
        if (current == null) {
            showToast("ابتدا یک عبارت معتبر وارد کنید")
            return
        }
        val newMem = _uiState.value.memoryValue - current
        _uiState.update { it.copy(memoryValue = newMem) }
        persist()
        showToast("M- : ${EngineeringFormatter.format(newMem)}")
    }

    // یک عدد را (بدون کاما و پیشوند) به عبارت اضافه می‌کند
    private fun appendValue(v: Double) {
        val st = _uiState.value
        val plain = EngineeringFormatter.formatPlain(v)
        var text = if (plain.startsWith("-")) "($plain)" else plain
        val last = st.expression.lastOrNull()
        // جلوگیری از چسبیدن دو عدد (مثلاً 5 و 12 → 512)
        if (!st.justEvaluated && last != null && (last.isDigit() || last == '.')) {
            text = "\u00D7$text"
        }
        append(text)
    }

    fun memoryRecall() {
        triggerHaptic()
        val mem = _uiState.value.memoryValue
        appendValue(mem)
        showToast("MR فراخوانی شد: ${EngineeringFormatter.formatStandard(mem)}")
    }

    fun memoryClear() {
        triggerHaptic()
        _uiState.update { it.copy(memoryValue = 0.0) }
        persist()
        showToast("حافظه پاک شد (MC)")
    }

    fun memoryStore() {
        triggerHaptic()
        val num = currentValue()
        if (num == null) {
            showToast("ابتدا یک عبارت معتبر وارد کنید")
            return
        }
        _uiState.update { it.copy(memoryValue = num) }
        persist()
        showToast("ذخیره در حافظه (MS): ${EngineeringFormatter.format(num)}")
    }

    fun setMemorySlot(slotKey: String, value: Double) {
        val updated = _uiState.value.memorySlots.toMutableMap()
        updated[slotKey] = value
        _uiState.update { it.copy(memorySlots = updated) }
        persist()
        showToast("$slotKey ذخیره شد: ${EngineeringFormatter.format(value)}")
    }

    // ---------- کپی و پیست ----------

    fun copyResult(context: Context) {
        triggerHaptic()
        val st = _uiState.value
        // در حالت استاندارد عدد ساده (بدون کاما) کپی می‌شود تا جای دیگر قابل استفاده باشد
        val text = if (!st.isError && st.notationMode == NotationMode.STD) {
            EngineeringFormatter.formatPlain(st.lastResult)
        } else {
            st.result
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Sadaf Result", text)
        clipboard.setPrimaryClip(clip)
        showToast("نتیجه کپی شد: $text")
    }

    fun copyExpression(context: Context) {
        triggerHaptic()
        val expr = _uiState.value.expression
        if (expr.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Sadaf Equation", expr)
        clipboard.setPrimaryClip(clip)
        showToast("فرمول کپی شد")
    }

    fun pasteFromClipboard(context: Context) {
        triggerHaptic()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip?.getItemAt(0)
        val text = item?.text?.toString()?.trim()

        if (!text.isNullOrEmpty()) {
            val sanitized = text.replace("=", "").trim()
            if (sanitized.length > 300) {
                showToast("متن کلیپ‌بورد خیلی طولانی است")
                return
            }
            append(sanitized)
            showToast("پیست شد: $sanitized")
        } else {
            showToast("کلیپ‌بورد خالی است")
        }
    }

    // ---------- تاریخچه ----------

    fun reuseHistory(item: CalculationItem, useResultOnly: Boolean = false) {
        triggerHaptic()
        val angle = try {
            AngleMode.valueOf(item.angleMode)
        } catch (_: Exception) {
            _uiState.value.angleMode
        }
        // نتیجه را دوباره حساب می‌کنیم تا عدد دقیق داشته باشیم (نه متن فرمت‌شده)
        val value: Double? = try {
            MathEvaluator.evaluate(item.expression, angle)
        } catch (_: Exception) {
            null
        }

        if (useResultOnly) {
            if (value != null) {
                appendValue(value)
            } else {
                append(item.result.replace(",", ""))
            }
        } else {
            val notation = _uiState.value.notationMode
            _uiState.update {
                it.copy(
                    expression = item.expression,
                    result = if (value != null) EngineeringFormatter.format(value, notation) else item.result,
                    numericResult = value ?: it.numericResult,
                    lastResult = value ?: it.lastResult,
                    livePreview = "",
                    openParenthesesCount = countUnbalancedOpenParens(item.expression),
                    isError = false,
                    justEvaluated = false
                )
            }
        }
        showToast("از تاریخچه بارگذاری شد")
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
            showToast("مورد از تاریخچه حذف شد")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAll()
            showToast("تمام تاریخچه پاک شد")
        }
    }

    // ---------- نمودار ----------

    fun setGraphFunction(fn: String) {
        _uiState.update { it.copy(graphFunction = fn) }
    }

    // زوم حول مرکز نما (نه حول مبدأ)
    fun zoomGraph(zoomIn: Boolean) {
        triggerHaptic()
        val factor = if (zoomIn) 0.75 else 1.33
        _uiState.update {
            val cx = (it.graphXMin + it.graphXMax) / 2.0
            val cy = (it.graphYMin + it.graphYMax) / 2.0
            val hw = (it.graphXMax - it.graphXMin) / 2.0 * factor
            val hh = (it.graphYMax - it.graphYMin) / 2.0 * factor
            if (hw < 1e-4 || hh < 1e-4 || hw > 1e6 || hh > 1e6) {
                it
            } else {
                it.copy(
                    graphXMin = cx - hw,
                    graphXMax = cx + hw,
                    graphYMin = cy - hh,
                    graphYMax = cy + hh
                )
            }
        }
    }

    // جابه‌جایی نما؛ مقدارها کسری از عرض/ارتفاع فعلی هستند
    fun panGraph(dxFraction: Double, dyFraction: Double) {
        triggerHaptic()
        _uiState.update {
            val w = it.graphXMax - it.graphXMin
            val h = it.graphYMax - it.graphYMin
            it.copy(
                graphXMin = it.graphXMin + dxFraction * w,
                graphXMax = it.graphXMax + dxFraction * w,
                graphYMin = it.graphYMin + dyFraction * h,
                graphYMax = it.graphYMax + dyFraction * h
            )
        }
    }

    fun resetGraph() {
        triggerHaptic()
        _uiState.update {
            it.copy(
                graphXMin = -10.0,
                graphXMax = 10.0,
                graphYMin = -5.0,
                graphYMax = 5.0
            )
        }
    }

    fun setActiveTab(tab: Int) {
        triggerHaptic()
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
        persist()
        triggerHaptic()
        showToast(if (_uiState.value.hapticEnabled) "ویبره کلیدها فعال شد" else "ویبره کلیدها غیرفعال شد")
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
