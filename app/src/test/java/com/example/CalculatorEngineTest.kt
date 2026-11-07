package com.example

import com.example.calculator.CalculationResult
import com.example.calculator.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    @Test
    fun testBasicAddition() {
        val result = engine.evaluate("5 + 3")
        assertTrue(result is CalculationResult.Success)
        assertEquals("8", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testBasicSubtraction() {
        val result = engine.evaluate("15 − 7")
        assertTrue(result is CalculationResult.Success)
        assertEquals("8", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testBasicMultiplication() {
        val result = engine.evaluate("6 × 7")
        assertTrue(result is CalculationResult.Success)
        assertEquals("42", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testBasicDivision() {
        val result = engine.evaluate("48 ÷ 6")
        assertTrue(result is CalculationResult.Success)
        assertEquals("8", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testOperatorPrecedence() {
        // Multiplications should happen before addition
        val result = engine.evaluate("2 + 3 × 4")
        assertTrue(result is CalculationResult.Success)
        assertEquals("14", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testParentheses() {
        val result = engine.evaluate("(2 + 3) × 4")
        assertTrue(result is CalculationResult.Success)
        assertEquals("20", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testDecimals() {
        val result = engine.evaluate("2.5 × 4")
        assertTrue(result is CalculationResult.Success)
        assertEquals("10", (result as CalculationResult.Success).formatted)

        val result2 = engine.evaluate("1.1 + 2.2")
        assertTrue(result2 is CalculationResult.Success)
        assertEquals(3.3, (result2 as CalculationResult.Success).value, 1e-6)
    }

    @Test
    fun testPercentage() {
        val result = engine.evaluate("50%")
        assertTrue(result is CalculationResult.Success)
        assertEquals("0.5", (result as CalculationResult.Success).formatted)

        val result2 = engine.evaluate("200 × 15%")
        assertTrue(result2 is CalculationResult.Success)
        assertEquals("30", (result2 as CalculationResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val result = engine.evaluate("10 ÷ 0")
        assertTrue(result is CalculationResult.Error)
        assertEquals("Cannot divide by zero", (result as CalculationResult.Error).message)
    }

    @Test
    fun testNegativeNumbers() {
        val result = engine.evaluate("-5 + 12")
        assertTrue(result is CalculationResult.Success)
        assertEquals("7", (result as CalculationResult.Success).formatted)
    }

    @Test
    fun testAppendOperatorSafely() {
        val expr1 = engine.appendOperator("10", "+")
        assertEquals("10 + ", expr1)

        // Replacing trailing operator
        val expr2 = engine.appendOperator(expr1, "×")
        assertEquals("10 × ", expr2)
    }

    @Test
    fun testBackspace() {
        val expr1 = engine.backspace("123")
        assertEquals("12", expr1)

        val expr2 = engine.backspace("10 + ")
        assertEquals("10", expr2)
    }
}
