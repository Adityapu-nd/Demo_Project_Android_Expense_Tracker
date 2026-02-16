package com.example.expense_tracker_android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.*

data class CategoryAnalytics(val icon: String, val name: String, val amount: Double, val color: Color)

class AnalyticsViewModel(expenses: List<Expense>) {
    var categories by mutableStateOf(listOf<CategoryAnalytics>())
        private set
    var total by mutableStateOf(0.0)
        private set

    init {
        updateAnalytics(expenses)
    }

    fun updateAnalytics(expenses: List<Expense>) {
        val grouped = expenses.groupBy { it.category ?: "Other" }
        val analytics = grouped.map { (cat, list) ->
            val icon = when (cat.lowercase(Locale.getDefault())) {
                "transport" -> "\uD83D\uDE97"
                "food" -> "\uD83C\uDF74"
                else -> "\uD83D\uDCB0"
            }
            val color = when (cat.lowercase(Locale.getDefault())) {
                "transport" -> Color(0xFF90CAF9)
                "food" -> Color(0xFFA5D6A7)
                else -> Color(0xFFFFF59D)
            }
            CategoryAnalytics(
                icon = icon,
                name = cat,
                amount = list.sumOf { it.amount?.toDouble() ?: 0.0 },
                color = color
            )
        }
        categories = analytics
        total = expenses.sumOf { it.amount?.toDouble() ?: 0.0 }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    expenses: List<Expense>,
    categories: List<String>, // Added categories parameter
    onBack: () -> Unit = {}
) {
    // Month selection logic
    val months = remember { (1..12).map { SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time) } }
    var selectedMonthIndex by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    val selectedMonth = months[selectedMonthIndex]
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Filter expenses for selected month
    val filteredExpenses = expenses.filter {
        val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date ?: "") } catch (e: Exception) { null }
        date != null && Calendar.getInstance().apply { time = date }.get(Calendar.MONTH) == selectedMonthIndex && Calendar.getInstance().apply { time = date }.get(Calendar.YEAR) == currentYear
    }

    // Group by category and sum, but include all categories (even if 0)
    val categoryTotals = categories.associateWith { cat ->
        filteredExpenses.filter { (it.category ?: "Other").equals(cat, ignoreCase = true) }.sumOf { it.amount ?: 0.0 }
    }
    val total = categoryTotals.values.sum()

    // Prepare categories for chart
    val analyticsCategories = categoryTotals.map { (cat, amt) ->
        val icon = when (cat.lowercase(Locale.getDefault())) {
            "transport" -> "\uD83D\uDE97"
            "food" -> "\uD83C\uDF74"
            else -> "\uD83D\uDCB0"
        }
        val color = when (cat.lowercase(Locale.getDefault())) {
            "transport" -> Color(0xFF90CAF9)
            "food" -> Color(0xFFA5D6A7)
            else -> Color(0xFFFFF59D)
        }
        CategoryAnalytics(icon = icon, name = cat, amount = amt, color = color)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 28.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF6F8FF))
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Month Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { if (selectedMonthIndex > 0) selectedMonthIndex-- }) {
                    Text("<", fontSize = 24.sp)
                }
                Text(
                    text = "$selectedMonth $currentYear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { if (selectedMonthIndex < 11) selectedMonthIndex++ }) {
                    Text(">", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Total Spending Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Spending", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Gray)
                    Text("₹%.2f".format(total), fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF1976D2))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Pie Chart
            PieChart(data = analyticsCategories, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            // Bar Chart (per week)
            BarChart(
                data = analyticsCategories,
                expenses = expenses,
                selectedMonthIndex = selectedMonthIndex,
                currentYear = currentYear,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Category Breakdown
            Text(
                "By Category",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(analyticsCategories) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = cat.color.copy(alpha = 0.15f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(cat.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            }
                            Text("₹%.2f".format(cat.amount), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<CategoryAnalytics>,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val total = data.sumOf { it.amount }
    Canvas(modifier = modifier.size(size)) {
        var startAngle = -90f
        data.forEach { cat ->
            val sweep = if (total == 0.0) 0f else (cat.amount / total * 360f).toFloat()
            drawArc(
                color = cat.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }
}

@Composable
fun BarChart(
    data: List<CategoryAnalytics>,
    expenses: List<Expense>,
    selectedMonthIndex: Int,
    currentYear: Int,
    modifier: Modifier = Modifier,
    barWidth: Dp = 32.dp,
    chartHeight: Dp = 120.dp
) {
    // Calculate weekly spending for the selected month
    val weeks = 5 // Max weeks in a month
    val weekTotals = MutableList(weeks) { 0.0 }
    val calendar = Calendar.getInstance()
    expenses.forEach { expense ->
        val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date ?: "") } catch (e: Exception) { null }
        if (date != null) {
            calendar.time = date
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            if (month == selectedMonthIndex && year == currentYear) {
                val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH) - 1 // 0-based
                if (weekOfMonth in 0 until weeks) {
                    weekTotals[weekOfMonth] += expense.amount ?: 0.0
                }
            }
        }
    }
    val max = weekTotals.maxOrNull() ?: 1.0
    Row(
        modifier = modifier.height(chartHeight),
        verticalAlignment = Alignment.Bottom
    ) {
        weekTotals.forEachIndexed { i, amt ->
            val barHeight = if (max == 0.0) 0.dp else (amt / max * chartHeight.value * 0.7f).dp // 0.7f to leave space for text
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                // Show amount above the bar
                Text("₹%.0f".format(amt), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight)
                        .background(Color(0xFF1976D2), shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Week ${i + 1}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
            }
            // No extra Spacer after last bar
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    val sampleExpenses = listOf(
        Expense(uid = 1, Expense_Name = "Other", amount = 123.0, date = "2026-02-11", time = null, category = "Other"),
        Expense(uid = 2, Expense_Name = "Transport", amount = 456.0, date = "2026-02-10", time = null, category = "Transport"),
        Expense(uid = 3, Expense_Name = "Food", amount = 789.0, date = "2026-02-09", time = null, category = "Food")
    )
    val sampleCategories = listOf("Transport", "Food", "Other", "Entertainment", "Health")
    AnalyticsScreen(expenses = sampleExpenses, categories = sampleCategories)
}
