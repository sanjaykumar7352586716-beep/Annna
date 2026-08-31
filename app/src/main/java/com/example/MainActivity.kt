package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalculatorState())
        private set

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> state = CalculatorState()
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Delete -> performDeletion()
            is CalculatorAction.ToggleSign -> toggleSign()
            is CalculatorAction.Percentage -> calculatePercentage()
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        if (state.number1.isNotBlank()) {
            state = state.copy(operation = operation)
        }
    }

    private fun performCalculation() {
        val number1 = state.number1.toDoubleOrNull()
        val number2 = state.number2.toDoubleOrNull()
        if (number1 != null && number2 != null) {
            val result = when (state.operation) {
                is CalculatorOperation.Add -> number1 + number2
                is CalculatorOperation.Subtract -> number1 - number2
                is CalculatorOperation.Multiply -> number1 * number2
                is CalculatorOperation.Divide -> number1 / number2
                null -> return
            }
            val formattedResult = if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString().take(15)
            }
            state = state.copy(
                number1 = formattedResult,
                number2 = "",
                operation = null
            )
        }
    }

    private fun performDeletion() {
        when {
            state.number2.isNotBlank() -> state = state.copy(
                number2 = state.number2.dropLast(1)
            )
            state.operation != null -> state = state.copy(
                operation = null
            )
            state.number1.isNotBlank() -> state = state.copy(
                number1 = state.number1.dropLast(1)
            )
        }
    }

    private fun enterDecimal() {
        if (state.operation == null && !state.number1.contains(".") && state.number1.isNotBlank()) {
            state = state.copy(
                number1 = state.number1 + "."
            )
            return
        } else if (state.operation == null && state.number1.isBlank()) {
            state = state.copy(
                number1 = "0."
            )
            return
        }

        if (state.operation != null && !state.number2.contains(".") && state.number2.isNotBlank()) {
            state = state.copy(
                number2 = state.number2 + "."
            )
        } else if (state.operation != null && state.number2.isBlank()) {
            state = state.copy(
                number2 = "0."
            )
        }
    }

    private fun enterNumber(number: Int) {
        if (state.operation == null) {
            if (state.number1.length >= MAX_NUM_LENGTH) return
            state = state.copy(
                number1 = state.number1 + number
            )
            return
        }
        if (state.number2.length >= MAX_NUM_LENGTH) return
        state = state.copy(
            number2 = state.number2 + number
        )
    }

    private fun toggleSign() {
        if (state.operation == null && state.number1.isNotBlank()) {
            val current = state.number1
            state = state.copy(
                number1 = if (current.startsWith("-")) current.drop(1) else "-$current"
            )
        } else if (state.operation != null && state.number2.isNotBlank()) {
            val current = state.number2
            state = state.copy(
                number2 = if (current.startsWith("-")) current.drop(1) else "-$current"
            )
        }
    }

    private fun calculatePercentage() {
        if (state.operation == null && state.number1.isNotBlank()) {
            val num = state.number1.toDoubleOrNull()
            if (num != null) {
                state = state.copy(number1 = (num / 100).toString())
            }
        } else if (state.operation != null && state.number2.isNotBlank()) {
            val num = state.number2.toDoubleOrNull()
            if (num != null) {
                state = state.copy(number2 = (num / 100).toString())
            }
        }
    }

    companion object {
        private const val MAX_NUM_LENGTH = 12
    }
}

data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: CalculatorOperation? = null
)

sealed class CalculatorOperation(val symbol: String) {
    object Add : CalculatorOperation("+")
    object Subtract : CalculatorOperation("-")
    object Multiply : CalculatorOperation("×")
    object Divide : CalculatorOperation("÷")
}

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
    object ToggleSign : CalculatorAction()
    object Percentage : CalculatorAction()
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel = viewModel()) {
    val state = viewModel.state
    val buttonSpacing = 12.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            val hasOperation = state.operation != null
            if (hasOperation) {
                Text(
                    text = state.number1 + " " + state.operation?.symbol,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    color = ThemeTextSecondary.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Text(
                text = if (hasOperation && state.number2.isNotBlank()) state.number2 else state.number1.ifBlank { "0" },
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Light,
                fontSize = 72.sp,
                letterSpacing = (-2).sp,
                color = ThemeTextPrimary,
                maxLines = 1
            )
        }

        // Keypad Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(KeypadBackground)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("C", Modifier.weight(1f), BtnClearBg, BtnClearText) { viewModel.onAction(CalculatorAction.Clear) }
                    CalculatorButton("( )", Modifier.weight(1f), BtnActionBg, BtnActionText) { /* No-op or parenthesis */ }
                    CalculatorButton("%", Modifier.weight(1f), BtnActionBg, BtnActionText) { viewModel.onAction(CalculatorAction.Percentage) }
                    CalculatorButton("÷", Modifier.weight(1f), BtnOperatorBg, BtnOperatorText) { viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Divide)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("7", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(7)) }
                    CalculatorButton("8", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(8)) }
                    CalculatorButton("9", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(9)) }
                    CalculatorButton("×", Modifier.weight(1f), BtnOperatorBg, BtnOperatorText) { viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Multiply)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("4", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(4)) }
                    CalculatorButton("5", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(5)) }
                    CalculatorButton("6", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(6)) }
                    CalculatorButton("-", Modifier.weight(1f), BtnOperatorBg, BtnOperatorText) { viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Subtract)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("1", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(1)) }
                    CalculatorButton("2", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(2)) }
                    CalculatorButton("3", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(3)) }
                    CalculatorButton("+", Modifier.weight(1f), BtnOperatorBg, BtnOperatorText) { viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Add)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("+/-", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.ToggleSign) }
                    CalculatorButton("0", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Number(0)) }
                    CalculatorButton(".", Modifier.weight(1f), BtnNumberBg, BtnNumberText) { viewModel.onAction(CalculatorAction.Decimal) }
                    CalculatorButton("=", Modifier.weight(1f), BtnEqualsBg, BtnEqualsText) { viewModel.onAction(CalculatorAction.Calculate) }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        Text(
            text = symbol,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            color = textColor
        )
    }
}
