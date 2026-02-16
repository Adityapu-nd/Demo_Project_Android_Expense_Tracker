package com.example.expense_tracker_android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(private val expenseDao: ExpenseDao) : ViewModel() {
    private val _todaySpending = MutableStateFlow(0.0)
    val todaySpending: StateFlow<Double> = _todaySpending.asStateFlow()

    private val _monthSpending = MutableStateFlow(0.0)
    val monthSpending: StateFlow<Double> = _monthSpending.asStateFlow()

    private val _recentExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val recentExpenses: StateFlow<List<Expense>> = _recentExpenses.asStateFlow()

    // Calendar month/year state
    private val _calendarMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-based
    val calendarMonth: StateFlow<Int> = _calendarMonth.asStateFlow()
    private val _calendarYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val calendarYear: StateFlow<Int> = _calendarYear.asStateFlow()

    private val _calendarExpenses = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val calendarExpenses: StateFlow<Map<Int, Double>> = _calendarExpenses.asStateFlow()

    fun goToPrevMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, _calendarMonth.value)
            set(Calendar.YEAR, _calendarYear.value)
            add(Calendar.MONTH, -1)
        }
        _calendarMonth.value = cal.get(Calendar.MONTH)
        _calendarYear.value = cal.get(Calendar.YEAR)
        loadDashboardData()
    }
    fun goToNextMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, _calendarMonth.value)
            set(Calendar.YEAR, _calendarYear.value)
            add(Calendar.MONTH, 1)
        }
        _calendarMonth.value = cal.get(Calendar.MONTH)
        _calendarYear.value = cal.get(Calendar.YEAR)
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val calendar = Calendar.getInstance()
            val month = String.format(Locale.getDefault(), "%02d", _calendarMonth.value + 1)
            val year = _calendarYear.value.toString()

            _todaySpending.value = expenseDao.getDailyExpense(today)
            _monthSpending.value = expenseDao.getTotalExpenseForMonth(month, year)
            _recentExpenses.value = expenseDao.getAll().sortedByDescending { it.date }.take(5)

            // For calendar: map day of month to total spent
            calendar.set(Calendar.MONTH, _calendarMonth.value)
            calendar.set(Calendar.YEAR, _calendarYear.value)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val map = mutableMapOf<Int, Double>()
            for (day in 1..daysInMonth) {
                val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year.toInt(), month.toInt(), day)
                map[day] = expenseDao.getDailyExpense(dateStr)
            }
            _calendarExpenses.value = map
        }
    }
}
