package com.example.expense_tracker_android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onAddExpense: () -> Unit, onAllExpenses: () -> Unit, onAnalytics: () -> Unit) {
    val todaySpending by viewModel.todaySpending.collectAsStateWithLifecycle()
    val monthSpending by viewModel.monthSpending.collectAsStateWithLifecycle()
    val recentExpenses by viewModel.recentExpenses.collectAsStateWithLifecycle()
    val calendarExpenses by viewModel.calendarExpenses.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val calendarYear by viewModel.calendarYear.collectAsStateWithLifecycle()

    LaunchedEffect(calendarMonth, calendarYear) { viewModel.loadDashboardData() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
            .padding(16.dp)
    ) {
        item {
            Text("Dashboard", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2B5DF5))
            Text("Hello.", fontSize = 20.sp, color = Color(0xFFB0B8C1), modifier = Modifier.padding(bottom = 16.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Today's Spending", color = Color(0xFFB0B8C1), fontSize = 14.sp)
                        Text("₹%.2f".format(todaySpending), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF2B5DF5))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("This Month", color = Color(0xFFB0B8C1), fontSize = 14.sp)
                        Text("₹%.2f".format(monthSpending), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF2B5DF5))
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAnalytics() }, // Changed to call onAnalytics
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5DF5))
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Analytics", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Recent Expenses", color = Color(0xFF2B5DF5), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    // Replaced LazyColumn with Column to avoid nested scrollable containers
                    // Sort recentExpenses by date descending, then by time descending if dates are equal
                    val sortedRecentExpenses = recentExpenses.sortedWith(compareByDescending<Expense> { expense ->
                        expense.date?.let {
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        } ?: 0L
                    }.thenByDescending { expense ->
                        expense.time?.let { it.time } ?: 0L
                    })
                    // Show only the last 3 recent expenses
                    val lastThreeExpenses = if (sortedRecentExpenses.size > 3) sortedRecentExpenses.take(3) else sortedRecentExpenses
                    Column {
                        lastThreeExpenses.forEach { expense ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF7F9FB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // TODO: Use category icon
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color(0xFFB0B8C1))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(expense.category ?: "Other", fontWeight = FontWeight.Bold, color = Color(0xFF2B5DF5))
                                    Text(expense.Expense_Name ?: "-", color = Color(0xFFB0B8C1), fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹%.2f".format(expense.amount ?: 0.0), fontWeight = FontWeight.Bold, color = Color(0xFF2B5DF5))
                                    Text(
                                        expense.date?.let {
                                            try {
                                                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                                                if (parsed != null) SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(parsed) else "-"
                                            } catch (e: Exception) {
                                                "-"
                                            }
                                        } ?: "-",
                                        color = Color(0xFFB0B8C1), fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAllExpenses() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5DF5))
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("All Expenses", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            // Calendar Section (now interactive)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFFB0B8C1), modifier = Modifier.clickable { viewModel.goToPrevMonth() })
                        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().apply {
                            set(Calendar.MONTH, calendarMonth)
                            set(Calendar.YEAR, calendarYear)
                        }.time)
                        Text(monthName, fontWeight = FontWeight.Bold, color = Color(0xFF2B5DF5), fontSize = 18.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFB0B8C1), modifier = Modifier.clickable { viewModel.goToNextMonth() })
                    }
                    Spacer(Modifier.height(8.dp))
                    // Days row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach {
                            Text(it, color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                    // Calendar days (interactive)
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.MONTH, calendarMonth)
                        set(Calendar.YEAR, calendarYear)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday
                    val todayCal = Calendar.getInstance()
                    val isCurrentMonth = todayCal.get(Calendar.MONTH) == calendarMonth && todayCal.get(Calendar.YEAR) == calendarYear
                    var dayCounter = 1
                    for (week in 0 until 6) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (dayOfWeek in 1..7) {
                                if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                                    Spacer(Modifier.weight(1f))
                                } else {
                                    val isToday = isCurrentMonth && dayCounter == todayCal.get(Calendar.DAY_OF_MONTH)
                                    val spent = calendarExpenses[dayCounter] ?: 0.0
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isToday) Color(0xFF2B5DF5) else Color(0xFFF7F9FB)),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            dayCounter.toString(),
                                            color = if (isToday) Color.White else Color(0xFF2B5DF5),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            if (spent > 0) "₹%.0f".format(spent) else "0",
                                            color = if (isToday) Color.White else Color(0xFFB0B8C1),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    dayCounter++
                                }
                            }
                        }
                        if (dayCounter > daysInMonth) break
                    }
                }
            }
        }
    }
    // Floating Action Button (FAB) for Add Expense
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = onAddExpense,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5DF5)),
            modifier = Modifier.size(56.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
        }
    }
}

// Helper to convert Brush to Color for Button
fun Brush.toColor(): Color = Color(0xFF2B5DF5)
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // Provide a fake/mock ExpenseDao for preview
    val fakeDao = object : ExpenseDao {
        override fun getAll() = emptyList<Expense>()
        override fun loadAllByIds(expenseIds: IntArray) = emptyList<Expense>()
        override fun findByName(expenseName: String) = Expense(0, "", 0.0, null, null, "")
        override fun insertAll(vararg expenses: Expense) {}
        override fun delete(expense: Expense) {}
        override fun getTotalExpenseForMonth(month: String, year: String) = 0.0
        override fun getDailyExpense(date: String) = 0.0
    }
    DashboardScreen(viewModel = DashboardViewModel(fakeDao), onAddExpense = {}, onAllExpenses = {}, onAnalytics = {})
}
