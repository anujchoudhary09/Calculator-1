package com.example.calculator

/**
 * Basic arithmetic functions from your Kotlin code:
 *
 * fun add(a: Double, b: Double): Double = a + b
 * fun sub(a: Double, b: Double): Double = a - b
 * fun mul(a: Double, b: Double): Double = a * b
 * fun div(a: Double, b: Double): Double = a / b
 */

fun add(a: Double, b: Double): Double {
    return a + b
}

fun sub(a: Double, b: Double): Double {
    return a - b
}

fun mul(a: Double, b: Double): Double {
    return a * b
}

fun div(a: Double, b: Double): Double {
    return a / b
}

sealed class CalcResult {
    data class Success(val value: Double, val formatted: String, val explanation: String) : CalcResult()
    data class Error(val message: String) : CalcResult()
}

/**
 * Directly executes your console calculator logic:
 *
 * val result = when (op) {
 *     "+" -> add(num1, num2)
 *     "-" -> sub(num1, num2)
 *     "*" -> mul(num1, num2)
 *     "/" -> {
 *         if (num2 != 0.0) div(num1, num2)
 *         else "Cannot divide by zero"
 *     }
 *     else -> "Invalid operator"
 * }
 */
fun calculate(num1: Double, op: String, num2: Double): CalcResult {
    return when (op) {
        "+", "add" -> {
            val r = add(num1, num2)
            CalcResult.Success(
                value = r,
                formatted = formatNumber(r),
                explanation = "add($num1, $num2) = ${formatNumber(r)}"
            )
        }
        "-", "−", "sub" -> {
            val r = sub(num1, num2)
            CalcResult.Success(
                value = r,
                formatted = formatNumber(r),
                explanation = "sub($num1, $num2) = ${formatNumber(r)}"
            )
        }
        "*", "×", "mul" -> {
            val r = mul(num1, num2)
            CalcResult.Success(
                value = r,
                formatted = formatNumber(r),
                explanation = "mul($num1, $num2) = ${formatNumber(r)}"
            )
        }
        "/", "÷", "div" -> {
            if (num2 != 0.0) {
                val r = div(num1, num2)
                CalcResult.Success(
                    value = r,
                    formatted = formatNumber(r),
                    explanation = "div($num1, $num2) = ${formatNumber(r)}"
                )
            } else {
                CalcResult.Error("Cannot divide by zero")
            }
        }
        else -> CalcResult.Error("Invalid operator: $op")
    }
}

/**
 * Helper to display clean whole numbers without unnecessary ".0" decimals (e.g. 5 instead of 5.0)
 */
fun formatNumber(value: Double): String {
    return if (value.isNaN()) {
        "NaN"
    } else if (value.isInfinite()) {
        if (value > 0) "Infinity" else "-Infinity"
    } else if (value % 1.0 == 0.0 && kotlin.math.abs(value) < 1e12) {
        value.toLong().toString()
    } else {
        // Format to max 6 decimals
        val str = "%.6f".format(java.util.Locale.US, value).trimEnd('0')
        if (str.endsWith('.')) str.dropLast(1) else str
    }
}
