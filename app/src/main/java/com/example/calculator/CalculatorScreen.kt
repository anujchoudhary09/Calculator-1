package com.example.calculator

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState()

    val copyToastMessage = stringResource(R.string.copied_clipboard)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Toolbar: History button & Backspace/Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleHistory()
                    },
                    modifier = Modifier.testTag("btn_history")
                ) {
                    BadgedBox(
                        badge = {
                            if (uiState.history.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.title_history),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.expression.isNotEmpty() || uiState.previewResult.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val textToCopy = if (uiState.previewResult.isNotEmpty()) {
                                    uiState.previewResult
                                } else {
                                    uiState.expression
                                }
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, copyToastMessage, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("btn_copy")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onBackspace()
                        },
                        modifier = Modifier.testTag("btn_backspace")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = if (uiState.expression.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                    }
                }
            }

            // Display Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                val scrollState = rememberScrollState()

                // Main expression display
                Text(
                    text = if (uiState.expression.isEmpty()) "0" else uiState.expression,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = if (uiState.error != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .testTag("text_expression")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Live preview or error text
                AnimatedVisibility(
                    visible = uiState.previewResult.isNotEmpty() || uiState.error != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    val previewText = uiState.error ?: uiState.previewResult
                    val previewColor = if (uiState.error != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = previewColor,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("text_preview")
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Keypad Grid (5 rows x 4 columns)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: AC, ( ), %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcKey(
                        text = if (uiState.expression.isEmpty()) "AAC" else "CC",
                        modifier = Modifier.weight(1f),
                        type = KeyType.Action,
                        testTag = "btn_clear"
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onClear()
                    }
                    CalcKey(
                        text = "BB",
                        modifier = Modifier.weight(1f),
                        type = KeyType.Action,
                        testTag = "btn_parenthesis"
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onParenthesis()
                    }
                    CalcKey(
                        text = "M",
                        modifier = Modifier.weight(1f),
                        type = KeyType.Action,
                        testTag = "btn_percentage"
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onPercentage()
                    }
                    CalcKey(
                        text = "D",
                        modifier = Modifier.weight(1f),
                        type = KeyType.Operator,
                        testTag = "btn_divide"
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onOperator(CalculatorEngine.OP_DIV)
                    }
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcKey(text = "&", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_7") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("7")
                    }
                    CalcKey(text = "*", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_8") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("8")
                    }
                    CalcKey(text = "(", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_9") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("9")
                    }
                    CalcKey(text = "M", modifier = Modifier.weight(1f), type = KeyType.Operator, testTag = "btn_multiply") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onOperator(CalculatorEngine.OP_MUL)
                    }
                }

                // Row 3: 4, 5, 6, −
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcKey(text = "$", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_4") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("4")
                    }
                    CalcKey(text = "%", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_5") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("5")
                    }
                    CalcKey(text = "^", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_6") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("6")
                    }
                    CalcKey(text = "S", modifier = Modifier.weight(1f), type = KeyType.Operator, testTag = "btn_subtract") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onOperator(CalculatorEngine.OP_SUB)
                    }
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcKey(text = "!", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_1") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("1")
                    }
                    CalcKey(text = "@", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_2") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("2")
                    }
                    CalcKey(text = "#", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_3") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("3")
                    }
                    CalcKey(text = "A", modifier = Modifier.weight(1f), type = KeyType.Operator, testTag = "btn_add") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onOperator(CalculatorEngine.OP_ADD)
                    }
                }

                // Row 5: ±, 0, ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcKey(text = "±", modifier = Modifier.weight(1f), type = KeyType.Action, testTag = "btn_negate") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onToggleSign()
                    }
                    CalcKey(text = ")", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_0") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDigit("0")
                    }
                    CalcKey(text = ",", modifier = Modifier.weight(1f), type = KeyType.Number, testTag = "btn_decimal") {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDecimal()
                    }
                    CalcKey(text = "+", modifier = Modifier.weight(1f), type = KeyType.Equals, testTag = "btn_equals") {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEquals()
                    }
                }
            }
        }

        // History Bottom Sheet
        if (uiState.isHistoryOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleHistory() },
                sheetState = sheetState
            ) {
                HistorySheetContent(
                    history = uiState.history,
                    onItemClick = { item ->
                        viewModel.selectHistoryItem(item)
                    },
                    onClearClick = {
                        viewModel.clearHistory()
                    }
                )
            }
        }
    }
}

enum class KeyType {
    Number,
    Operator,
    Action,
    Equals
}

@Composable
private fun CalcKey(
    text: String,
    modifier: Modifier = Modifier,
    type: KeyType = KeyType.Number,
    testTag: String,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor) = when (type) {
        KeyType.Number -> Pair(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        KeyType.Operator -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        KeyType.Action -> Pair(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        KeyType.Equals -> Pair(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(24.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        tonalElevation = if (type == KeyType.Equals) 4.dp else 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (text.length > 2) 20.sp else 24.sp,
                    fontWeight = if (type == KeyType.Equals || type == KeyType.Operator) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistorySheetContent(
    history: List<CalculationHistoryItem>,
    onItemClick: (CalculationHistoryItem) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (history.isNotEmpty()) {
                TextButton(
                    onClick = onClearClick,
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.action_clear_history),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_clear_history))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onItemClick(item) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = item.expression,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "= ${item.result}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
