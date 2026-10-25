package com.example.math

import kotlin.math.*

enum class AngleMode {
    DEG, RAD, GRAD
}

enum class NotationMode {
    STD, ENG, SCI, FRAC
}

/** خطایی که پیام فارسی آن مستقیماً به کاربر نمایش داده می‌شود. */
class CalcException(message: String) : IllegalArgumentException(message)

object MathEvaluator {

    val PI = Math.PI
    val E = Math.E
    val PHI = 1.618033988749895
    val SPEED_OF_LIGHT = 299792458.0 // m/s
    val PLANCK = 6.62607015e-34

    private val FUNCTIONS = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "sinh", "cosh", "tanh",
        "sqrt", "cbrt", "ln", "log", "log10", "log2",
        "abs", "exp", "floor", "ceil", "round"
    )

    // نشانه‌هایی که بعد از آن‌ها «-» یا «+» یکانی (علامت عدد) است
    private val UNARY_PRECEDERS = setOf("+", "-", "*", "/", "^", "(", "neg")

    // مقدار روی پشته؛ pct یعنی این مقدار از عملگر % آمده (برای 50+10% = 55)
    private class Num(val v: Double, val pct: Boolean = false)

    /**
     * عبارت ریاضی را محاسبه می‌کند.
     * توابع، عملگرها، ثابت‌ها، پرانتز و متغیر x پشتیبانی می‌شوند.
     */
    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.DEG,
        xVal: Double = 0.0
    ): Double {
        val tokens = tokenize(sanitize(expression))
        val rpn = shuntingYard(tokens)
        return evalRpn(rpn, angleMode, xVal)
    }

    private fun sanitize(expr: String): String {
        val mapped = expr
            .replace("\u00D7", "*")           // ×
            .replace("\u00F7", "/")           // ÷
            .replace("\u2212", "-")           // −
            .replace("\u2013", "-")           // –
            .replace("\u221A", "sqrt")        // √
            .replace("\u221B", "cbrt")        // ∛
            .replace("sin\u207B\u00B9", "asin") // sin⁻¹
            .replace("cos\u207B\u00B9", "acos")
            .replace("tan\u207B\u00B9", "atan")
            .replace("\u00B2", "^2")          // ²
            .replace("\u00B3", "^3")          // ³
            .replace("\u066A", "%")           // ٪
        val sb = StringBuilder(mapped.length)
        for (ch in mapped) {
            when (ch) {
                in '\u06F0'..'\u06F9' -> sb.append('0' + (ch - '\u06F0')) // ارقام فارسی
                in '\u0660'..'\u0669' -> sb.append('0' + (ch - '\u0660')) // ارقام عربی
                '\u066B' -> sb.append('.')                                // ممیز فارسی
                ',', '\u066C', '\u060C' -> { }                            // جداکننده هزارگان
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun isOperandToken(t: String): Boolean {
        return t == ")" || t == "x" || t == "!" || t == "%" || t.toDoubleOrNull() != null
    }

    private fun insertImplicitMultiplyIfNeeded(tokens: MutableList<String>) {
        val last = tokens.lastOrNull() ?: return
        if (isOperandToken(last)) {
            tokens.add("*")
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c == '\u03C0') { // π
                insertImplicitMultiplyIfNeeded(tokens)
                tokens.add(PI.toString())
                i++
                continue
            }

            if (c == '\u03C6') { // φ
                insertImplicitMultiplyIfNeeded(tokens)
                tokens.add(PHI.toString())
                i++
                continue
            }

            if (c in "+-*/^()!%") {
                if (c == '(') {
                    insertImplicitMultiplyIfNeeded(tokens)
                }
                if (c == '-' || c == '+') {
                    val prev = tokens.lastOrNull()
                    val isUnary = prev == null || prev in UNARY_PRECEDERS || prev in FUNCTIONS
                    if (isUnary) {
                        if (c == '-') tokens.add("neg")
                        i++
                        continue
                    }
                }
                tokens.add(c.toString())
                i++
                continue
            }

            // اعداد
            if (c.isDigit() || (c == '.' && i + 1 < len && expr[i + 1].isDigit())) {
                val start = i
                var hasDot = false
                while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                    if (expr[i] == '.') {
                        if (hasDot) throw CalcException("عدد نامعتبر است (دو ممیز)")
                        hasDot = true
                    }
                    i++
                }
                // نماد علمی فقط با E بزرگ (مثل 1.2E-5)؛ e کوچک همان عدد نپر است (2e+1 = 2·e+1)
                if (i < len && expr[i] == 'E') {
                    var j = i + 1
                    if (j < len && (expr[j] == '+' || expr[j] == '-')) j++
                    if (j < len && expr[j].isDigit()) {
                        while (j < len && expr[j].isDigit()) j++
                        i = j
                    }
                }
                insertImplicitMultiplyIfNeeded(tokens)
                tokens.add(expr.substring(start, i))
                continue
            }

            // کلمات: توابع و ثابت‌ها و متغیر x
            if (c.isLetter()) {
                val start = i
                while (i < len && expr[i].isLetter()) i++
                var word = expr.substring(start, i).lowercase()
                if (word == "log") {
                    if (expr.startsWith("10", i)) {
                        word = "log10"
                        i += 2
                    } else if (expr.startsWith("2", i)) {
                        word = "log2"
                        i += 1
                    }
                }
                when {
                    word == "e" -> {
                        insertImplicitMultiplyIfNeeded(tokens)
                        tokens.add(E.toString())
                    }
                    word == "x" -> {
                        insertImplicitMultiplyIfNeeded(tokens)
                        tokens.add("x")
                    }
                    word == "pi" -> {
                        insertImplicitMultiplyIfNeeded(tokens)
                        tokens.add(PI.toString())
                    }
                    word in FUNCTIONS -> {
                        insertImplicitMultiplyIfNeeded(tokens)
                        tokens.add(word)
                    }
                    word.length > 1 && word.endsWith("x") && word.dropLast(1) in FUNCTIONS -> {
                        // مثل sinx
                        insertImplicitMultiplyIfNeeded(tokens)
                        tokens.add(word.dropLast(1))
                        tokens.add("x")
                    }
                    else -> throw CalcException("نماد ناشناخته: $word")
                }
                continue
            }

            throw CalcException("نویسه نامعتبر: $c")
        }

        return tokens
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "neg" -> 3
            "^" -> 4
            else -> if (op in FUNCTIONS) 5 else 0
        }
    }

    private fun isRightAssociative(op: String): Boolean {
        return op == "^"
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val opStack = mutableListOf<String>()

        for (token in tokens) {
            when {
                token == "x" || token.toDoubleOrNull() != null -> output.add(token)
                token in FUNCTIONS -> opStack.add(token)
                token == "(" -> opStack.add(token)
                token == ")" -> {
                    while (opStack.isNotEmpty() && opStack.last() != "(") {
                        output.add(opStack.removeAt(opStack.size - 1))
                    }
                    if (opStack.isEmpty()) {
                        throw CalcException("پرانتز بسته اضافه است")
                    }
                    opStack.removeAt(opStack.size - 1) // حذف "("
                    if (opStack.isNotEmpty() && opStack.last() in FUNCTIONS) {
                        output.add(opStack.removeAt(opStack.size - 1))
                    }
                }
                token == "!" || token == "%" -> {
                    // عملگر پسوندی
                    output.add(token)
                }
                else -> { // عملگر دوتایی یا منفی یکانی
                    // منفی یکانی پیشوندی است و نباید چیزی از پشته بیرون بکشد
                    if (token != "neg") {
                        while (opStack.isNotEmpty() && opStack.last() != "(") {
                            val top = opStack.last()
                            val precTop = precedence(top)
                            val precToken = precedence(token)
                            if (precTop > precToken || (precTop == precToken && !isRightAssociative(token))) {
                                output.add(opStack.removeAt(opStack.size - 1))
                            } else {
                                break
                            }
                        }
                    }
                    opStack.add(token)
                }
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.removeAt(opStack.size - 1)
            if (top != "(") output.add(top)
        }

        return output
    }

    private fun evalRpn(rpn: List<String>, angleMode: AngleMode, xVal: Double): Double {
        val st = mutableListOf<Num>()

        fun pop(): Num {
            if (st.isEmpty()) throw CalcException("عبارت ناقص است")
            return st.removeAt(st.size - 1)
        }

        fun push(v: Double) {
            if (v.isNaN() || v.isInfinite()) {
                throw CalcException("نتیجه تعریف‌نشده یا بسیار بزرگ است")
            }
            st.add(Num(v))
        }

        for (token in rpn) {
            if (token == "x") {
                push(xVal)
                continue
            }
            val num = token.toDoubleOrNull()
            if (num != null) {
                push(num)
                continue
            }

            when (token) {
                "neg" -> {
                    val a = pop()
                    st.add(Num(-a.v, a.pct))
                }
                "!" -> push(factorial(pop().v))
                "%" -> st.add(Num(pop().v / 100.0, true))
                "+" -> {
                    val b = pop()
                    val a = pop()
                    push(if (b.pct) a.v + a.v * b.v else a.v + b.v)
                }
                "-" -> {
                    val b = pop()
                    val a = pop()
                    push(if (b.pct) a.v - a.v * b.v else a.v - b.v)
                }
                "*" -> {
                    val b = pop()
                    val a = pop()
                    push(a.v * b.v)
                }
                "/" -> {
                    val b = pop()
                    val a = pop()
                    if (b.v == 0.0) throw CalcException("تقسیم بر صفر ممکن نیست")
                    push(a.v / b.v)
                }
                "^" -> {
                    val b = pop()
                    val a = pop()
                    push(a.v.pow(b.v))
                }
                "sin" -> push(snap(sin(toRadians(pop().v, angleMode))))
                "cos" -> push(snap(cos(toRadians(pop().v, angleMode))))
                "tan" -> {
                    val rad = toRadians(pop().v, angleMode)
                    if (abs(cos(rad)) < 1e-15) throw CalcException("تانژانت در این زاویه تعریف‌نشده است")
                    push(tan(rad))
                }
                "asin" -> {
                    val a = pop().v
                    if (a < -1.0 || a > 1.0) throw CalcException("ورودی asin باید بین ۱- و ۱ باشد")
                    push(fromRadians(asin(a), angleMode))
                }
                "acos" -> {
                    val a = pop().v
                    if (a < -1.0 || a > 1.0) throw CalcException("ورودی acos باید بین ۱- و ۱ باشد")
                    push(fromRadians(acos(a), angleMode))
                }
                "atan" -> push(fromRadians(atan(pop().v), angleMode))
                "sinh" -> push(sinh(pop().v))
                "cosh" -> push(cosh(pop().v))
                "tanh" -> push(tanh(pop().v))
                "sqrt" -> {
                    val a = pop().v
                    if (a < 0) throw CalcException("ریشه دوم عدد منفی تعریف‌نشده است")
                    push(sqrt(a))
                }
                "cbrt" -> push(cbrt(pop().v))
                "ln" -> {
                    val a = pop().v
                    if (a <= 0) throw CalcException("ln فقط برای عدد مثبت تعریف شده است")
                    push(ln(a))
                }
                "log", "log10" -> {
                    val a = pop().v
                    if (a <= 0) throw CalcException("log فقط برای عدد مثبت تعریف شده است")
                    push(log10(a))
                }
                "log2" -> {
                    val a = pop().v
                    if (a <= 0) throw CalcException("log2 فقط برای عدد مثبت تعریف شده است")
                    push(log2(a))
                }
                "abs" -> push(abs(pop().v))
                "exp" -> push(exp(pop().v))
                "floor" -> push(floor(pop().v))
                "ceil" -> push(ceil(pop().v))
                "round" -> push(floor(pop().v + 0.5))
                else -> throw CalcException("عملگر ناشناخته: $token")
            }
        }

        if (st.size != 1) throw CalcException("عبارت نامعتبر است")
        val res = st[0].v
        return if (abs(res) < 1e-14) 0.0 else res
    }

    // خطای گرد کردن در sin/cos (مثل sin(180°) = 1.2e-16) را صفر می‌کند
    private fun snap(v: Double): Double = if (abs(v) < 1e-15) 0.0 else v

    private fun toRadians(angle: Double, mode: AngleMode): Double {
        return when (mode) {
            AngleMode.RAD -> angle
            AngleMode.DEG -> Math.toRadians(angle)
            AngleMode.GRAD -> angle * (Math.PI / 200.0)
        }
    }

    private fun fromRadians(rad: Double, mode: AngleMode): Double {
        return when (mode) {
            AngleMode.RAD -> rad
            AngleMode.DEG -> Math.toDegrees(rad)
            AngleMode.GRAD -> rad * (200.0 / Math.PI)
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) {
            throw CalcException("فاکتوریل فقط برای عدد صحیح ۰ تا ۱۷۰ تعریف شده است")
        }
        var result = 1.0
        val intN = n.toInt()
        for (i in 2..intN) {
            result *= i
        }
        return result
    }
}
