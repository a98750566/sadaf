package com.example

import com.example.math.AngleMode
import com.example.math.CalcException
import com.example.math.EngineeringFormatter
import com.example.math.MathEvaluator
import com.example.math.NotationMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun basicArithmetic_isCorrect() {
        val res = MathEvaluator.evaluate("2 + 3 * 4")
        assertEquals(14.0, res, 1e-6)
    }

    @Test
    fun trigonometry_degrees_isCorrect() {
        val sin30 = MathEvaluator.evaluate("sin(30)", AngleMode.DEG)
        assertEquals(0.5, sin30, 1e-6)

        val cos60 = MathEvaluator.evaluate("cos(60)", AngleMode.DEG)
        assertEquals(0.5, cos60, 1e-6)
    }

    @Test
    fun implicitMultiplication_isCorrect() {
        val res1 = MathEvaluator.evaluate("2(3 + 4)")
        assertEquals(14.0, res1, 1e-6)

        val res2 = MathEvaluator.evaluate("2π")
        assertEquals(2 * Math.PI, res2, 1e-6)
    }

    @Test
    fun powerAndRoots_isCorrect() {
        val pow = MathEvaluator.evaluate("2^3")
        assertEquals(8.0, pow, 1e-6)

        val sqrt = MathEvaluator.evaluate("√(16)")
        assertEquals(4.0, sqrt, 1e-6)
    }

    @Test
    fun engineeringFormatter_isCorrect() {
        val eng1 = EngineeringFormatter.format(1500.0, NotationMode.ENG)
        assertEquals("1.5 k", eng1)

        val frac = EngineeringFormatter.format(0.75, NotationMode.FRAC)
        assertTrue(frac.contains("3/4"))
    }

    // ---- تست باگ‌هایی که اصلاح شدند ----

    @Test
    fun exp_isNotBrokenByVariableReplacement() {
        assertEquals(Math.E, MathEvaluator.evaluate("exp(1)"), 1e-9)
        assertEquals(Math.E, MathEvaluator.evaluate("exp(x)", AngleMode.DEG, 1.0), 1e-9)
    }

    @Test
    fun variableX_works() {
        assertEquals(9.0, MathEvaluator.evaluate("x^2", AngleMode.DEG, -3.0), 1e-9)
        assertEquals(6.0, MathEvaluator.evaluate("2x", AngleMode.DEG, 3.0), 1e-9)
    }

    @Test
    fun percent_behavesLikePhoneCalculator() {
        assertEquals(55.0, MathEvaluator.evaluate("50+10%"), 1e-9)
        assertEquals(45.0, MathEvaluator.evaluate("50-10%"), 1e-9)
        assertEquals(20.0, MathEvaluator.evaluate("200×10%"), 1e-9)
        assertEquals(0.1, MathEvaluator.evaluate("10%"), 1e-9)
    }

    @Test
    fun unaryMinus_precedence() {
        assertEquals(-4.0, MathEvaluator.evaluate("-2^2"), 1e-9)
        assertEquals(4.0, MathEvaluator.evaluate("(-2)^2"), 1e-9)
        assertEquals(0.125, MathEvaluator.evaluate("2^-3"), 1e-9)
        assertEquals(-6.0, MathEvaluator.evaluate("2*-3"), 1e-9)
        assertEquals(5.0, MathEvaluator.evaluate("--5"), 1e-9)
    }

    @Test
    fun constantE_afterNumber_isNotScientificNotation() {
        assertEquals(2 * Math.E + 1, MathEvaluator.evaluate("2e+1"), 1e-9)
        assertEquals(2000.0, MathEvaluator.evaluate("2E3"), 1e-9)
        assertEquals(0.0015, MathEvaluator.evaluate("1.5E-3"), 1e-12)
    }

    @Test
    fun doubleDecimalPoint_isError() {
        assertThrows(CalcException::class.java) { MathEvaluator.evaluate("1.2.3") }
    }

    @Test
    fun errors_haveFriendlyMessages() {
        val e1 = assertThrows(CalcException::class.java) { MathEvaluator.evaluate("1/0") }
        assertTrue(e1.message!!.contains("صفر"))
        assertThrows(CalcException::class.java) { MathEvaluator.evaluate("sin()") }
        assertThrows(CalcException::class.java) { MathEvaluator.evaluate("5+") }
        assertThrows(CalcException::class.java) { MathEvaluator.evaluate("abc") }
    }

    @Test
    fun functionWithoutParentheses_stopsAtOperator() {
        assertEquals(13.0, MathEvaluator.evaluate("√16+9"), 1e-9)
    }

    @Test
    fun persianDigitsAndSeparators_areAccepted() {
        assertEquals(15.0, MathEvaluator.evaluate("۱۰+۵"), 1e-9)
        assertEquals(1234.0, MathEvaluator.evaluate("1,234"), 1e-9)
    }

    @Test
    fun trigSnapsToZero() {
        assertEquals(0.0, MathEvaluator.evaluate("sin(180)", AngleMode.DEG), 0.0)
        assertEquals(0.0, MathEvaluator.evaluate("cos(90)", AngleMode.DEG), 0.0)
    }

    @Test
    fun formatPlain_hasNoCommasOrPrefixes() {
        assertEquals("1234.5", EngineeringFormatter.formatPlain(1234.5))
        assertEquals("0.3", EngineeringFormatter.formatPlain(0.1 + 0.2))
        assertEquals("-1500", EngineeringFormatter.formatPlain(-1500.0))
        // خروجی formatPlain باید دوباره قابل محاسبه باشد
        val v = 98765.4321
        assertEquals(v, MathEvaluator.evaluate(EngineeringFormatter.formatPlain(v)), 1e-9)
        val tiny = 1.5e-12
        assertEquals(tiny, MathEvaluator.evaluate(EngineeringFormatter.formatPlain(tiny)), 1e-20)
    }

    @Test
    fun engineeringFormatter_roundingBoundary() {
        assertEquals("1 k", EngineeringFormatter.format(999.99999, NotationMode.ENG))
    }
}
