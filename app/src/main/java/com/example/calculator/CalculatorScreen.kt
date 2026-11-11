package com.example.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Teacher Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kotlin Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Powered by add(), sub(), mul(), div()",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Navigation Mode Tabs: Step-by-Step (Console style) vs Keypad Mode
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("Step-by-Step") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = "Step-by-step form mode"
                    )
                },
                modifier = Modifier.testTag("tab_step_by_step")
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("Keypad") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = "Interactive keypad mode"
                    )
                },
                modifier = Modifier.testTag("tab_keypad")
            )
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.selectedTab == 0) {
                StepByStepFormView(uiState = uiState, viewModel = viewModel)
            } else {
                KeypadCalculatorView(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

/**
 * Step-by-Step Mode: Directly maps to the student's console loop:
 *   print("Enter first number: ") -> num1
 *   print("Enter operator (+, -, *, /): ") -> op
 *   print("Enter second number: ") -> num2
 *   when(op) { "+" -> add(), "-" -> sub(), "*" -> mul(), "/" -> div() }
 */
@Composable
private fun StepByStepFormView(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: First Number
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. First number (num1):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.num1Input,
                    onValueChange = { viewModel.onNum1Change(it) },
                    placeholder = { Text("e.g. 15") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_num1")
                )
            }
        }

        // Step 2: Operator Selection (+, -, *, /)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Operator (op):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val operators = listOf(
                        "+" to "add",
                        "-" to "sub",
                        "*" to "mul",
                        "/" to "div"
                    )

                    operators.forEach { (symbol, name) ->
                        val isSelected = uiState.selectedOp == symbol
                        Surface(
                            onClick = { viewModel.onOperatorSelect(symbol) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("op_btn_$symbol"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            tonalElevation = if (isSelected) 4.dp else 1.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = symbol,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Second Number
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Second number (num2):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.num2Input,
                    onValueChange = { viewModel.onNum2Change(it) },
                    placeholder = { Text("e.g. 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_num2")
                )
            }
        }

        // Action Buttons: Calculate & Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.onClearForm() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_form_clear")
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear")
            }

            Button(
                onClick = { viewModel.onCalculateForm() },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp)
                    .testTag("btn_form_calculate"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Calculate", fontWeight = FontWeight.Bold)
            }
        }

        // Results or Error Display
        AnimatedVisibility(
            visible = uiState.formResult != null || uiState.formError != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            if (uiState.formError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_form_error"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.formError,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (uiState.formResult != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_form_result"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Result",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = uiState.formResult,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (uiState.formExplanation != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = uiState.formExplanation,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Keypad Mode: An interactive keypad driven by the exact same add, sub, mul, div functions!
 */
@Composable
private fun KeypadCalculatorView(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 480.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Top subtext (e.g. "12 + ")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Basic Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.keypadSubtext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Main Number Display
                Text(
                    text = uiState.keypadError ?: uiState.keypadDisplay,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (uiState.keypadError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("keypad_display")
                )
            }
        }

        // Keypad Grid: 4x4 layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: C, ⌫, [blank], ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeyButton(
                    text = "C",
                    type = KeyStyle.Action,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_clear"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onKeypadClear()
                }
                KeyButton(
                    text = "⌫",
                    type = KeyStyle.Action,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_backspace"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadBackspace()
                }
                Spacer(modifier = Modifier.weight(1f))
                KeyButton(
                    text = "÷",
                    type = KeyStyle.Operator,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_div"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadOperator("/")
                }
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeyButton(text = "7", modifier = Modifier.weight(1f), testTag = "keypad_7") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("7")
                }
                KeyButton(text = "8", modifier = Modifier.weight(1f), testTag = "keypad_8") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("8")
                }
                KeyButton(text = "9", modifier = Modifier.weight(1f), testTag = "keypad_9") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("9")
                }
                KeyButton(
                    text = "×",
                    type = KeyStyle.Operator,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_mul"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadOperator("*")
                }
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeyButton(text = "4", modifier = Modifier.weight(1f), testTag = "keypad_4") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("4")
                }
                KeyButton(text = "5", modifier = Modifier.weight(1f), testTag = "keypad_5") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("5")
                }
                KeyButton(text = "6", modifier = Modifier.weight(1f), testTag = "keypad_6") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("6")
                }
                KeyButton(
                    text = "−",
                    type = KeyStyle.Operator,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_sub"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadOperator("-")
                }
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeyButton(text = "1", modifier = Modifier.weight(1f), testTag = "keypad_1") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("1")
                }
                KeyButton(text = "2", modifier = Modifier.weight(1f), testTag = "keypad_2") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("2")
                }
                KeyButton(text = "3", modifier = Modifier.weight(1f), testTag = "keypad_3") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("3")
                }
                KeyButton(
                    text = "+",
                    type = KeyStyle.Operator,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_add"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadOperator("+")
                }
            }

            // Row 5: 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeyButton(text = "0", modifier = Modifier.weight(2f), testTag = "keypad_0") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit("0")
                }
                KeyButton(text = ".", modifier = Modifier.weight(1f), testTag = "keypad_dot") {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKeypadDigit(".")
                }
                KeyButton(
                    text = "=",
                    type = KeyStyle.Equals,
                    modifier = Modifier.weight(1f),
                    testTag = "keypad_equals"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onKeypadEquals()
                }
            }
        }
    }
}

enum class KeyStyle {
    Number,
    Operator,
    Action,
    Equals
}

@Composable
private fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    type: KeyStyle = KeyStyle.Number,
    testTag: String,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor) = when (type) {
        KeyStyle.Number -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        KeyStyle.Operator -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        KeyStyle.Action -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        KeyStyle.Equals -> Pair(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (type == KeyStyle.Equals) 4.dp else 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (type == KeyStyle.Equals || type == KeyStyle.Operator) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
