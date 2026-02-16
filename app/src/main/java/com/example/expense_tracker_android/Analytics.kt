package com.example.expense_tracker_android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlin.math.abs
import kotlin.random.Random

// Helper function to generate a color from a category name
fun getCategoryColor(categoryName: String): Color {
    val predefinedColors = mapOf(
        "transport" to Color(0xFF90CAF9),
        "food" to Color(0xFFA5D6A7),
        "shopping" to Color(0xFFFFCC80),
        "bills" to Color(0xFFEF9A9A),
        "entertainment" to Color(0xFFCE93D8),
        "health" to Color(0xFF80DEEA),
        "education" to Color(0xFFFFF59D),
        "travel" to Color(0xFFBCAAA4),
        "groceries" to Color(0xFFC5E1A5),
        "utilities" to Color(0xFFB0BEC5),
        "other" to Color(0xFFFFF59D),
        "others" to Color(0xFFFFF59D)
    )

    val lowerName = categoryName.lowercase(Locale.getDefault())
    predefinedColors[lowerName]?.let { return it }

    // Generate a color based on the category name hash
    val hash = categoryName.hashCode()
    val hue = abs(hash % 360)
    val saturation = 0.4f + (abs((hash / 360) % 100) / 100f) * 0.3f // 0.4-0.7
    val lightness = 0.7f + (abs((hash / 36000) % 100) / 100f) * 0.2f // 0.7-0.9

    return hslToRgb(hue.toFloat(), saturation, lightness)
}

// Convert HSL to RGB
fun hslToRgb(hue: Float, saturation: Float, lightness: Float): Color {
    val h = hue / 360f
    val s = saturation
    val l = lightness

    fun hueToRgb(p: Float, q: Float, t: Float): Float {
        var tVar = t
        if (tVar < 0f) tVar += 1f
        if (tVar > 1f) tVar -= 1f
        if (tVar < 1f / 6f) return p + (q - p) * 6f * tVar
        if (tVar < 1f / 2f) return q
        if (tVar < 2f / 3f) return p + (q - p) * (2f / 3f - tVar) * 6f
        return p
    }

    val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
    val p = 2 * l - q

    val r = hueToRgb(p, q, h + 1f / 3f)
    val g = hueToRgb(p, q, h)
    val b = hueToRgb(p, q, h - 1f / 3f)

    return Color(r, g, b, 1f)
}

fun getCategoryIcon(categoryName: String): String {
    val predefinedIcons = mapOf(
        "transport" to "🚗",
        "food" to "🍴",
        "shopping" to "🛍️",
        "bills" to "📄",
        "entertainment" to "🎬",
        "health" to "🏥",
        "education" to "📚",
        "travel" to "✈️",
        "groceries" to "🛒",
        "utilities" to "💡",
        "other" to "💰",
        "others" to "💰"
    )

    val lowerName = categoryName.lowercase(Locale.getDefault())
    return predefinedIcons[lowerName] ?: "💰"
}

data class CategoryAnalytics(val icon: String, val name: String, val amount: Double, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    expenseDao: ExpenseDao,
    categoryDao: CategoryDao,
    onBack: () -> Unit = {}
) {
    // Month selection logic
    val months = remember { (1..12).map { SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time) } }
    var selectedMonthIndex by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    val selectedMonth = months[selectedMonthIndex]
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Query categories and expenses from DB
    val categories = remember { categoryDao.getAll() }
    val expenses = remember { expenseDao.getAll() }

    // Filter expenses for selected month
    val filteredExpenses = expenses.filter {
        val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date ?: "") } catch (e: Exception) { null }
        date != null && Calendar.getInstance().apply { time = date }.get(Calendar.MONTH) == selectedMonthIndex && Calendar.getInstance().apply { time = date }.get(Calendar.YEAR) == currentYear
    }

    // Assign a random color to each category for this session
    val colorMap = remember {
        val map = mutableMapOf<String, Color>()
        categories.forEach { cat ->
            map[cat.name] = Color(Random(cat.name.hashCode()).nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        }
        map
    }
    val constantIcon = "💰"

    // Group by category and sum, but include all categories (even if 0)
    val categoryTotals = categories.associate { cat ->
        cat.name to filteredExpenses.filter { (it.category ?: "Other").equals(cat.name, ignoreCase = true) }.sumOf { it.amount ?: 0.0 }
    }
    val total = categoryTotals.values.sum()

    // Prepare categories for chart
    val analyticsCategories = categoryTotals.map { (cat, amt) ->
        CategoryAnalytics(
            icon = constantIcon,
            name = cat,
            amount = amt,
            color = colorMap[cat] ?: Color.Gray
        )
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
    // Provide fake/mock DAOs for preview
    val fakeExpenseDao = object : ExpenseDao {
        override fun getAll() = listOf(
            Expense(uid = 1, Expense_Name = "Other", amount = 123.0, date = "2026-02-11", time = null, category = "Other"),
            Expense(uid = 2, Expense_Name = "Transport", amount = 456.0, date = "2026-02-10", time = null, category = "Transport"),
            Expense(uid = 3, Expense_Name = "Food", amount = 789.0, date = "2026-02-09", time = null, category = "Food")
        )
        override fun loadAllByIds(expenseIds: IntArray) = emptyList<Expense>()
        override fun findByName(expenseName: String) = Expense(0, "", 0.0, null, null, "")
        override fun insertAll(vararg expenses: Expense) {}
        override fun delete(expense: Expense) {}
        override fun getTotalExpenseForMonth(month: String, year: String) = 0.0
        override fun getDailyExpense(date: String) = 0.0
    }
    val fakeCategoryDao = object : CategoryDao {
        override fun getAll() = listOf(
            Category(name = "Transport"),
            Category(name = "Food"),
            Category(name = "Other"),
            Category(name = "Entertainment"),
            Category(name = "Health")
        )
        override fun getAllCategoryNames() = getAll().map { it.name }
        override fun insert(category: Category) {}
        override fun delete(category: Category) {}
        override fun getByName(name: String) = getAll().find { it.name == name }
    }
    AnalyticsScreen(expenseDao = fakeExpenseDao, categoryDao = fakeCategoryDao)
}
