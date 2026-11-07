package com.example.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val history: List<CalculationHistoryItem> = emptyList(),
    val isHistoryOpen: Boolean = false,
    val isResultFinal: Boolean = false,
    val error: String? = null
)

class CalculatorViewModel(
    private val engine: CalculatorEngine = CalculatorEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val newExpr = if (state.isResultFinal) {
                digit
            } else {
                engine.appendDigit(state.expression, digit)
            }
            val preview = calculatePreview(newExpr)
            state.copy(
                expression = newExpr,
                previewResult = preview,
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onOperator(op: String) {
        _uiState.update { state ->
            val baseExpr = if (state.isResultFinal && state.previewResult.isNotEmpty()) {
                state.previewResult
            } else if (state.expression.isEmpty() && state.previewResult.isNotEmpty()) {
                state.previewResult
            } else {
                state.expression
            }

            val newExpr = engine.appendOperator(baseExpr, op)
            val preview = calculatePreview(newExpr)
            state.copy(
                expression = newExpr,
                previewResult = preview,
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            val baseExpr = if (state.isResultFinal) "" else state.expression
            val newExpr = engine.appendDecimal(baseExpr)
            val preview = calculatePreview(newExpr)
            state.copy(
                expression = newExpr,
                previewResult = preview,
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onPercentage() {
        _uiState.update { state ->
            val newExpr = engine.appendPercentage(state.expression)
            val preview = calculatePreview(newExpr)
            state.copy(
                expression = newExpr,
                previewResult = preview,
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onParenthesis() {
        _uiState.update { state ->
            val baseExpr = if (state.isResultFinal) "" else state.expression
            val newExpr = engine.appendParenthesis(baseExpr)
            val preview = calculatePreview(newExpr)
            state.copy(
                expression = newExpr,
                previewResult = preview,
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val baseExpr = if (state.isResultFinal && state.previewResult.isNotEmpty()) {
                state.previewResult
            } else {
                state.expression
            }
            val newExpr = engine.toggleSign(baseExpr)
            val preview = calculatePreview(newExpr)
            state.copy(
                expression = newExpr,
                previewResult = preview,
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.isResultFinal) {
                state.copy(expression = "", previewResult = "", isResultFinal = false, error = null)
            } else {
                val newExpr = engine.backspace(state.expression)
                val preview = calculatePreview(newExpr)
                state.copy(
                    expression = newExpr,
                    previewResult = preview,
                    error = null
                )
            }
        }
    }

    fun onClear() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                previewResult = "",
                isResultFinal = false,
                error = null
            )
        }
    }

    fun onEquals() {
        _uiState.update { state ->
            if (state.expression.isBlank()) return@update state

            when (val result = engine.evaluate(state.expression)) {
                is CalculationResult.Success -> {
                    val historyItem = CalculationHistoryItem(
                        expression = state.expression,
                        result = result.formatted
                    )
                    state.copy(
                        expression = result.formatted,
                        previewResult = "",
                        history = listOf(historyItem) + state.history,
                        isResultFinal = true,
                        error = null
                    )
                }
                is CalculationResult.Error -> {
                    state.copy(
                        error = result.message,
                        isResultFinal = false
                    )
                }
            }
        }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(isHistoryOpen = !it.isHistoryOpen) }
    }

    fun selectHistoryItem(item: CalculationHistoryItem) {
        _uiState.update { state ->
            state.copy(
                expression = item.result,
                previewResult = "",
                isHistoryOpen = false,
                isResultFinal = true,
                error = null
            )
        }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList(), isHistoryOpen = false) }
    }

    private fun calculatePreview(expr: String): String {
        if (expr.isBlank()) return ""
        val trimmed = expr.trimEnd()
        // If it ends with an operator, evaluate expression up to operator
        val lastChar = trimmed.takeLast(1)
        val testExpr = if (CalculatorEngine.OPERATORS.contains(lastChar)) {
            trimmed.dropLast(1).trimEnd()
        } else {
            trimmed
        }

        if (testExpr.isBlank()) return ""

        return when (val res = engine.evaluate(testExpr)) {
            is CalculationResult.Success -> res.formatted
            is CalculationResult.Error -> ""
        }
    }
}
