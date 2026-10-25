package com.example.math

import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object EngineeringFormatter {

    private val SI_PREFIXES = mapOf(
        -15 to "f",
        -12 to "p",
        -9 to "n",
        -6 to "µ",
        -3 to "m",
        0 to "",
        3 to "k",
        6 to "M",
        9 to "G",
        12 to "T",
        15 to "P"
    )

    fun format(value: Double, mode: NotationMode = NotationMode.STD): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        return when (mode) {
            NotationMode.STD -> formatStandard(value)
            NotationMode.ENG -> formatEngineering(value)
            NotationMode.SCI -> formatScientific(value)
            NotationMode.FRAC -> formatFraction(value)
        }
    }

    fun formatStandard(value: Double): String {
        if (abs(value) < 1e-12 && value != 0.0) {
            return formatScientific(value)
        }
        if (abs(value) >= 1e12) {
            return formatScientific(value)
        }

        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.##########", symbols)
        return df.format(value)
    }

    fun formatScientific(value: Double): String {
        if (value == 0.0) return "0"
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.######E0", symbols)
        val raw = df.format(value)
        val parts = raw.split("E")
        if (parts.size == 2) {
            val mantissa = parts[0]
            val exp = parts[1].toIntOrNull() ?: 0
            return "$mantissa × 10^($exp)"
        }
        return raw
    }

    fun formatEngineering(value: Double): String {
        if (value == 0.0) return "0"
        val absVal = abs(value)
        val sign = if (value < 0) "-" else ""

        var exp = floor(log10(absVal)).toInt()
        val engExp = if (exp >= 0) {
            (exp / 3) * 3
        } else {
            ((exp - 2) / 3) * 3
        }

        var e3 = engExp
        var mantissa = absVal / 10.0.pow(e3.toDouble())
        // اگر مانتیس بعد از گرد کردن به 1000 برسد، یک پله بالا می‌رویم
        if (round(mantissa * 10000.0) / 10000.0 >= 1000.0) {
            e3 += 3
            mantissa = absVal / 10.0.pow(e3.toDouble())
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.####", symbols)
        val mantissaStr = df.format(mantissa)

        val prefix = SI_PREFIXES[e3]
        return if (prefix != null && prefix.isNotEmpty()) {
            "$sign$mantissaStr $prefix"
        } else if (e3 == 0) {
            "$sign$mantissaStr"
        } else {
            "$sign$mantissaStr × 10^($e3)"
        }
    }

    /**
     * عدد را به شکل ساده و بدون کاما/پیشوند برمی‌گرداند تا بشود داخل عبارت
     * گذاشت یا کپی کرد (مثل 1234.5 یا 1.0E-10).
     */
    fun formatPlain(value: Double): String {
        if (value.isNaN() || value.isInfinite() || value == 0.0) return "0"
        val a = abs(value)
        if (a >= 1e15 || a < 1e-9) return value.toString()
        return BigDecimal(value).round(MathContext(15)).stripTrailingZeros().toPlainString()
    }

    fun formatFraction(value: Double, maxDenominator: Long = 1000): String {
        if (value == 0.0) return "0"
        val sign = if (value < 0) "-" else ""
        val absVal = abs(value)

        val intPart = floor(absVal).toLong()
        val fracPart = absVal - intPart

        if (fracPart < 1e-9) {
            return "$sign$intPart"
        }

        // Continued fraction approximation
        var p0 = 0L
        var q0 = 1L
        var p1 = 1L
        var q1 = 0L
        var x = fracPart

        while (true) {
            val a = floor(x).toLong()
            val p2 = a * p1 + p0
            val q2 = a * q1 + q0

            if (q2 > maxDenominator) break

            p0 = p1
            q0 = q1
            p1 = p2
            q1 = q2

            val diff = x - a
            if (abs(diff) < 1e-9) break
            x = 1.0 / diff
        }

        val totalNum = intPart * q1 + p1
        val approx = totalNum.toDouble() / q1.toDouble()
        if (abs(approx - absVal) > 1e-3) {
            // Not a clean fraction, fall back to standard
            return formatStandard(value)
        }

        return if (intPart > 0) {
            "$sign$totalNum/$q1 ($intPart ${p1}/$q1)"
        } else {
            "$sign$p1/$q1"
        }
    }
}
