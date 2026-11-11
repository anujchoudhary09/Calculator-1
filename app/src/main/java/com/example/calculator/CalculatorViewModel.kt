package com.example.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
    // Tab Mode (0: Step-by-Step Form Mode, 1: Keypad Mode)
    val selectedTab: Int = 0,

    // Step-by-Step Form Mode State (Direct 1:1 to your console code)
    val num1Input: String = "",
    val num2Input: String = "",
    val selectedOp: String = "+",
    val formResult: String? = null,
    val formExplanation: String? = null,
    val formError: String? = null,

    // Keypad Mode State (Built using the exact same add, sub, mul, div functions)
    val keypadDisplay: String = "0",
    val keypadSubtext: String = "",
    val keypadFirstNum: Double? = null,
    val keypadOperator: String? = null,
    val isEnteringSecondNum: Boolean = false,
    val keypadError: String? = null
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // -------------------------------------------------------------
    // Form Mode (Direct mapping of readln() & when(op))
    // -------------------------------------------------------------

    fun onNum1Change(value: String) {
        // Allow digits, minus at start, and single decimal point
        if (value.isEmpty() || value == "-" || value.matches(Regex("^-?\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(num1Input = value, formError = null) }
        }
    }

    fun onNum2Change(value: String) {
        if (value.isEmpty() || value == "-" || value.matches(Regex("^-?\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(num2Input = value, formError = null) }
        }
    }

    fun onOperatorSelect(op: String) {
        _uiState.update { it.copy(selectedOp = op, formError = null) }
    }

    fun onCalculateForm() {
        val s = _uiState.value
        val a = s.num1Input.toDoubleOrNull()
        val b = s.num2Input.toDoubleOrNull()

        if (a == null) {
            _uiState.update { it.copy(formError = "Please enter a valid first number") }
            return
        }
        if (b == null) {
            _uiState.update { it.copy(formError = "Please enter a valid second number") }
            return
        }

        // Call the student's exact logic
        when (val res = calculate(a, s.selectedOp, b)) {
            is CalcResult.Success -> {
                _uiState.update {
                    it.copy(
                        formResult = res.formatted,
                        formExplanation = res.explanation,
                        formError = null
                    )
                }
            }
            is CalcResult.Error -> {
                _uiState.update {
                    it.copy(
                        formResult = null,
                        formExplanation = null,
                        formError = res.message
                    )
                }
            }
        }
    }

    fun onClearForm() {
        _uiState.update {
            it.copy(
                num1Input = "",
                num2Input = "",
                selectedOp = "+",
                formResult = null,
                formExplanation = null,
                formError = null
            )
        }
    }

    // -------------------------------------------------------------
    // Keypad Mode (Interactive keypad using your add/sub/mul/div)
    // -------------------------------------------------------------

    fun onKeypadDigit(digit: String) {
        _uiState.update { state ->
            if (state.keypadError != null) {
                return@update state.copy(
                    keypadDisplay = digit,
                    keypadError = null,
                    isEnteringSecondNum = false
                )
            }

            if (state.isEnteringSecondNum) {
                state.copy(
                    keypadDisplay = digit,
                    isEnteringSecondNum = false
                )
            } else {
                val newDisplay = if (state.keypadDisplay == "0" && digit != ".") {
                    digit
                } else if (digit == "." && state.keypadDisplay.contains(".")) {
                    state.keypadDisplay
                } else {
                    state.keypadDisplay + digit
                }
                state.copy(keypadDisplay = newDisplay)
            }
        }
    }

    fun onKeypadOperator(op: String) {
        _uiState.update { state ->
            val currentVal = state.keypadDisplay.toDoubleOrNull() ?: 0.0

            // If we already had a first number and an operator, calculate partial result first
            if (state.keypadFirstNum != null && state.keypadOperator != null && !state.isEnteringSecondNum) {
                when (val res = calculate(state.keypadFirstNum, state.keypadOperator, currentVal)) {
                    is CalcResult.Success -> {
                        state.copy(
                            keypadFirstNum = res.value,
                            keypadOperator = op,
                            keypadDisplay = res.formatted,
                            keypadSubtext = "${res.formatted} $op",
                            isEnteringSecondNum = true,
                            keypadError = null
                        )
                    }
                    is CalcResult.Error -> {
                        state.copy(
                            keypadError = res.message,
                            keypadFirstNum = null,
                            keypadOperator = null
                        )
                    }
                }
            } else {
                state.copy(
                    keypadFirstNum = currentVal,
                    keypadOperator = op,
                    keypadSubtext = "${formatNumber(currentVal)} $op",
                    isEnteringSecondNum = true,
                    keypadError = null
                )
            }
        }
    }

    fun onKeypadEquals() {
        val state = _uiState.value
        val firstNum = state.keypadFirstNum ?: return
        val op = state.keypadOperator ?: return
        val secondNum = state.keypadDisplay.toDoubleOrNull() ?: return

        when (val res = calculate(firstNum, op, secondNum)) {
            is CalcResult.Success -> {
                _uiState.update {
                    it.copy(
                        keypadDisplay = res.formatted,
                        keypadSubtext = "${formatNumber(firstNum)} $op ${formatNumber(secondNum)} =",
                        keypadFirstNum = null,
                        keypadOperator = null,
                        isEnteringSecondNum = true,
                        keypadError = null
                    )
                }
            }
            is CalcResult.Error -> {
                _uiState.update {
                    it.copy(
                        keypadError = res.message,
                        keypadSubtext = "",
                        keypadFirstNum = null,
                        keypadOperator = null
                    )
                }
            }
        }
    }

    fun onKeypadClear() {
        _uiState.update {
            it.copy(
                keypadDisplay = "0",
                keypadSubtext = "",
                keypadFirstNum = null,
                keypadOperator = null,
                isEnteringSecondNum = false,
                keypadError = null
            )
        }
    }

    fun onKeypadBackspace() {
        _uiState.update { state ->
            if (state.keypadError != null) {
                state.copy(keypadDisplay = "0", keypadError = null)
            } else if (state.keypadDisplay.length <= 1 || (state.keypadDisplay.length == 2 && state.keypadDisplay.startsWith("-"))) {
                state.copy(keypadDisplay = "0")
            } else {
                state.copy(keypadDisplay = state.keypadDisplay.dropLast(1))
            }
        }
    }
}
