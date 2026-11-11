package com.example

import com.example.calculator.CalcResult
import com.example.calculator.add
import com.example.calculator.calculate
import com.example.calculator.div
import com.example.calculator.mul
import com.example.calculator.sub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testStudentBasicFunctions() {
        // Direct testing of your functions:
        assertEquals(7.0, add(3.0, 4.0), 1e-6)
        assertEquals(5.0, sub(12.0, 7.0), 1e-6)
        assertEquals(24.0, mul(6.0, 4.0), 1e-6)
        assertEquals(4.0, div(20.0, 5.0), 1e-6)
    }

    @Test
    fun testCalculateAddition() {
        val res = calculate(10.0, "+", 5.0)
        assertTrue(res is CalcResult.Success)
        assertEquals("15", (res as CalcResult.Success).formatted)
    }

    @Test
    fun testCalculateSubtraction() {
        val res = calculate(20.0, "-", 8.0)
        assertTrue(res is CalcResult.Success)
        assertEquals("12", (res as CalcResult.Success).formatted)
    }

    @Test
    fun testCalculateMultiplication() {
        val res = calculate(7.0, "*", 6.0)
        assertTrue(res is CalcResult.Success)
        assertEquals("42", (res as CalcResult.Success).formatted)
    }

    @Test
    fun testCalculateDivision() {
        val res = calculate(50.0, "/", 5.0)
        assertTrue(res is CalcResult.Success)
        assertEquals("10", (res as CalcResult.Success).formatted)
    }

    @Test
    fun testCalculateDivisionByZero() {
        val res = calculate(10.0, "/", 0.0)
        assertTrue(res is CalcResult.Error)
        assertEquals("Cannot divide by zero", (res as CalcResult.Error).message)
    }

    @Test
    fun testCalculateDecimals() {
        val res = calculate(2.5, "*", 4.0)
        assertTrue(res is CalcResult.Success)
        assertEquals("10", (res as CalcResult.Success).formatted)
    }
}
