package com.example.expense_tracker_android.ui.screen


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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expense_tracker_android.model.ExchangeRatesService
import com.example.expense_tracker_android.ui.theme.Expense_Tracker_AndroidTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import android.util.Log

@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    categories: List<String>,
    onSaveClick: (AddExpenseFormState) -> Unit = {},
    onCancel: () -> Unit = {},
    onBack: () -> Unit = {},
    onCreateCategory: () -> Unit = {},
    api: ExchangeRatesService, // Pass Retrofit API instance
    apiKey: String // Pass API key explicitly
) {
    val categoryOptions = categories + "Create a new category..."
    val paymentMethods = listOf("Cash", "Card", "UPI", "Other")
    val currencyOptions = listOf("INR", "USD", "EUR", "GBP", "JPY")

    val context = LocalContext.current
    val now = remember { Calendar.getInstance() }

    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(categories.firstOrNull() ?: "") }
    var dateMillis by rememberSaveable { mutableStateOf(now.timeInMillis) }
    var hour by rememberSaveable { mutableStateOf(now.get(Calendar.HOUR_OF_DAY)) }
    var minute by rememberSaveable { mutableStateOf(now.get(Calendar.MINUTE)) }
    var paymentMethod by rememberSaveable { mutableStateOf(paymentMethods.first()) }
    var note by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(currencyOptions.first()) }

    val date = remember(dateMillis) { formatDate(dateMillis) }
    val time = remember(hour, minute) { formatTime(hour, minute) }

    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var paymentExpanded by rememberSaveable { mutableStateOf(false) }
    var currencyExpanded by rememberSaveable { mutableStateOf(false) }

    // Validation for Save button
    val isAmountValid = try {
        val amt = amount.toDouble()
        val digits = amount.trim().takeWhile { it != '.' }.length
        amount.isNotBlank() && amt > 0 && digits <= 10 && amount.toDoubleOrNull() != null
    } catch (_: Exception) {
        false
    }
    val saveButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (isAmountValid) MaterialTheme.colorScheme.primary else Color.Gray
    )

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Use AutoMirrored version
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Add Expense",
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

                HorizontalDivider()

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
                            categoryOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        if (item == "Create a new category...") {
                                            categoryExpanded = false
                                            onCreateCategory()
                                        } else {
                                            category = item
                                            categoryExpanded = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AssistChip(
                        onClick = {
                            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selected = Calendar.getInstance().apply {
                                        set(year, month, day, hour, minute)
                                    }
                                    dateMillis = selected.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        label = { Text(date) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    hour = selectedHour
                                    minute = selectedMinute
                                },
                                hour,
                                minute,
                                false
                            ).show()
                        },
                        label = { Text(time) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                HorizontalDivider()

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

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currencyExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Currency",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currency,
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
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            currencyOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        currency = item
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

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
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    errorMessage = null
                    if (currency == "INR") {
                        onSaveClick(
                            AddExpenseFormState(
                                amount = amount,
                                category = category,
                                dateMillis = dateMillis,
                                hour = hour,
                                minute = minute,
                                paymentMethod = paymentMethod,
                                note = note
                            )
                        )
                    } else {
                        isLoading = true
                        CoroutineScope(Dispatchers.Main).launch {
                            when (val result = convertToINR(amount, currency, api, apiKey, context)) {
                                is ConversionResult.Success -> {
                                    isLoading = false
                                    onSaveClick(
                                        AddExpenseFormState(
                                            amount = result.amount,
                                            category = category,
                                            dateMillis = dateMillis,
                                            hour = hour,
                                            minute = minute,
                                            paymentMethod = paymentMethod,
                                            note = note
                                        )
                                    )
                                }
                                is ConversionResult.Error -> {
                                    isLoading = false
                                    errorMessage = result.message
                                    Log.e("AddExpense", "Currency conversion error: ${result.message}")
                                }
                            }
                        }
                    }
                },
                enabled = isAmountValid && !isLoading,
                colors = saveButtonColors,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Save")
                }
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

data class AddExpenseFormState(
    val amount: String,
    val category: String,
    val dateMillis: Long,
    val hour: Int,
    val minute: Int,
    val paymentMethod: String,
    val note: String
)

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

sealed class ConversionResult {
    data class Success(val amount: String) : ConversionResult()
    data class Error(val message: String) : ConversionResult()
}

suspend fun convertToINR(
    amount: String,
    currency: String,
    api: ExchangeRatesService,
    apiKey: String,
    context: android.content.Context
): ConversionResult {
    val amt = amount.toDoubleOrNull() ?: return ConversionResult.Error("Invalid amount")
    if (currency == "INR") return ConversionResult.Success("%.2f".format(amt))
    try {
        // Construct the URL for logging
        val url = "https://api.exchangeratesapi.io/v1/latest?access_key=$apiKey"
        Log.d("CurrencyAPI", "Requesting: $url")
        println("CurrencyAPI: Requesting $url")
        // Fetch all rates with base EUR
        val response = api.getLatestRates(apiKey)
        if (response.success && response.rates != null) {
            val ratesMapEUR = response.rates
            val eurToInr = ratesMapEUR["INR"] ?: return ConversionResult.Error("No EUR to INR rate found")
            // Convert all rates to INR base
            val ratesMapINR = ratesMapEUR.mapValues { (_, rate) -> eurToInr / rate }
            // Save the INR-based rates map to a JSON file for future use
            val json = com.google.gson.Gson().toJson(ratesMapINR)
            context.openFileOutput("inr_rates_map.json", android.content.Context.MODE_PRIVATE).use { fos ->
                fos.write(json.toByteArray())
            }
            // Use the map for conversion
            val rate = ratesMapINR[currency] ?: return ConversionResult.Error("No rate found for $currency")
            val inrAmount = amt * rate
            return ConversionResult.Success("%.2f".format(inrAmount))
        } else if (response.error != null) {
            val code = response.error.code
            val info = response.error.info
            println("Currency API error: code=$code, info=$info")
            Log.e("CurrencyAPI", "Error code: $code, info: $info")
            return ConversionResult.Error("API error: ${info ?: code}")
        } else {
            return ConversionResult.Error("Conversion failed: Unknown error")
        }
    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        Log.e("CurrencyAPI", "HTTP ${e.code()} error: $errorBody")
        println("CurrencyAPI: HTTP ${e.code()} error: $errorBody")
        return ConversionResult.Error("HTTP ${e.code()} error: $errorBody")
    } catch (e: Exception) {
        Log.e("CurrencyAPI", "Currency conversion failed. Please check your network or API key. ${e.localizedMessage}")
        return ConversionResult.Error("Currency conversion failed. Please check your network or API key.")
    }
}

//@Preview(showBackground = true)
//@Composable
//fun AddExpensePreview() {
//    // Provide a mock API for preview
//    val mockApi = object : ExchangeRatesService {
//        override suspend fun getLatestRates(
//            apiKey: String,
//            base: String?
//        ) = com.example.expense_tracker_android.model.LatestRatesResponse(
//            success = true,
//            rates = mapOf("USD" to 80.0, "EUR" to 90.0)
//        )
//    }
//    Expense_Tracker_AndroidTheme {
//        AddExpenseScreen(
//            categories = listOf("Food", "Transport", "Shopping"),
//            api = mockApi,
//            apiKey = "FAKE_KEY"
//        )
//    }
//}
