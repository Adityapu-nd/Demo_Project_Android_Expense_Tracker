package com.example.expense_tracker_android

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker_android.ui.theme.Expense_Tracker_AndroidTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ModifyExpenseScreen(
    expense: Expense? = null,
    modifier: Modifier = Modifier,
    onSaveClick: (Expense) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onBack: () -> Unit = {} // Added onBack parameter
) {
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Others")
    val paymentMethods = listOf("Cash", "Card", "UPI", "Other")

    val context = LocalContext.current
    val now = remember { Calendar.getInstance() }

    var amount by rememberSaveable { mutableStateOf(expense?.amount?.toString() ?: "") }
    var category by rememberSaveable { mutableStateOf(expense?.category ?: categories.first()) }
    var dateMillis by rememberSaveable { mutableStateOf(
        expense?.date?.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time ?: now.timeInMillis
            } catch (e: Exception) { now.timeInMillis }
        } ?: now.timeInMillis
    ) }
    var hour by rememberSaveable { mutableStateOf(
        expense?.time?.hours ?: now.get(Calendar.HOUR_OF_DAY)
    ) }
    var minute by rememberSaveable { mutableStateOf(
        expense?.time?.minutes ?: now.get(Calendar.MINUTE)
    ) }
    var paymentMethod by rememberSaveable { mutableStateOf(paymentMethods.first()) }
    var note by rememberSaveable { mutableStateOf(expense?.Expense_Name ?: "") }

    val date = remember(dateMillis) { formatDate(dateMillis) }
    val time = remember(hour, minute) { formatTime(hour, minute) }

    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var paymentExpanded by rememberSaveable { mutableStateOf(false) }

    // Validation for Save button
    val isAmountValid = try {
        val amt = amount.toDouble()
        val digits = amount.trim().takeWhile { it != '.' }.length
        amount.isNotBlank() && amt > 0 && digits <= 10 && amount.toDoubleOrNull() != null
    } catch (e: Exception) {
        false
    }
    val saveButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (isAmountValid) MaterialTheme.colorScheme.primary else Color.Gray
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { // Hooked up navigation
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Modify Expense",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("            0.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(120.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                androidx.compose.material3.HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        category = item
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                androidx.compose.material3.HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { paymentExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = paymentMethod,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false }
                        ) {
                            paymentMethods.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        paymentMethod = item
                                        paymentExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                androidx.compose.material3.HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("              Optional") },
                        singleLine = true,
                        modifier = Modifier.width(160.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Delete")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = dateMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val sqlDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    val sqlTime = java.sql.Time(cal.timeInMillis)
                    onSaveClick(
                        Expense(
                            uid = expense?.uid ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                            Expense_Name = note.ifBlank { amount + " " + category },
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            date = sqlDateString,
                            time = sqlTime,
                            category = category
                        )
                    )
                },
                enabled = isAmountValid,
                colors = saveButtonColors,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModifyExpensePreview() {
    Expense_Tracker_AndroidTheme {
        ModifyExpenseScreen()
    }
}

private fun formatDate(dateMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(dateMillis)
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(calendar.time)
}
