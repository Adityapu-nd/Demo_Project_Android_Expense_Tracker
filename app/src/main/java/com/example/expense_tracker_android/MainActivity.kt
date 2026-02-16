package com.example.expense_tracker_android

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.expense_tracker_android.ui.theme.Expense_Tracker_AndroidTheme
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object NewUser : Screen()
    object NewUser2 : Screen()
    object Dashboard : Screen()
    object AddExpense : Screen()
    object AllExpenses : Screen()
    object Analytics : Screen()
    object CreateCategory : Screen() // Added for category creation
    data class ModifyExpense(val expense: Expense) : Screen()
}

class MainActivity : ComponentActivity() {
    private fun isFtuxCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences("ftux_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("ftux_completed", false)
    }
    private fun setFtuxCompleted(context: Context) {
        val prefs = context.getSharedPreferences("ftux_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("ftux_completed", true).apply()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide system bars for immersive experience
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        enableEdgeToEdge()
        setContent {
            Expense_Tracker_AndroidTheme {
                // Set up Room database and DAO
                val db = remember {
                    Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "expense-db"
                    ).fallbackToDestructiveMigration()
                    .allowMainThreadQueries().build() // For demo; use background thread in production
                }
                val expenseDao = db.expenseDao()
                val categoryDao = db.categoryDao()
                val dashboardViewModel = remember { DashboardViewModel(expenseDao) }

                // --- CATEGORY STATE ---
                val defaultCategories = listOf("Food", "Transport", "Shopping", "Bills", "Others")

                // Load categories from database or initialize with defaults
                val categories = remember {
                    val savedCategories = categoryDao.getAllCategoryNames()
                    if (savedCategories.isEmpty()) {
                        // Initialize database with default categories
                        defaultCategories.forEach { catName ->
                            categoryDao.insert(Category(name = catName))
                        }
                        mutableStateListOf<String>().apply { addAll(defaultCategories) }
                    } else {
                        mutableStateListOf<String>().apply { addAll(savedCategories) }
                    }
                }

                var currentScreen by remember {
                    mutableStateOf<Screen>(
                        if (isFtuxCompleted(applicationContext)) Screen.Dashboard else Screen.NewUser
                    )
                }
                // Add new Screen.NewUser and Screen.NewUser2

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val screen = currentScreen) {
                        is Screen.NewUser -> NewUserScreen(
                            onGetStarted = { currentScreen = Screen.NewUser2 },
                            onSkip = {
                                setFtuxCompleted(applicationContext)
                                currentScreen = Screen.Dashboard
                            }
                        )
                        is Screen.NewUser2 -> NewUserScreen2(
                            onContinue = {
                                setFtuxCompleted(applicationContext)
                                currentScreen = Screen.Dashboard
                            }
                        )
                        is Screen.Dashboard -> {
                            setFtuxCompleted(applicationContext)
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onAddExpense = { currentScreen = Screen.AddExpense },
                                onAllExpenses = { currentScreen = Screen.AllExpenses },
                                onAnalytics = { currentScreen = Screen.Analytics }
                            )
                        }
                        is Screen.AddExpense -> AddExpenseScreen(
                            modifier = Modifier.padding(innerPadding),
                            categories = categories,
                            onSaveClick = { form ->
                                // Use dateMillis, hour, and minute to create Date and Time
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = form.dateMillis
                                    set(Calendar.HOUR_OF_DAY, form.hour)
                                    set(Calendar.MINUTE, form.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val sqlDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                val sqlTime = Time(cal.timeInMillis)
                                expenseDao.insertAll(
                                    Expense(
                                        uid = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                                        Expense_Name = form.note.ifBlank { form.amount + " " + form.category },
                                        amount = form.amount.toDoubleOrNull() ?: 0.0,
                                        date = sqlDateString, // Store as string
                                        time = sqlTime,
                                        category = form.category
                                    )
                                )
                                dashboardViewModel.loadDashboardData()
                                currentScreen = Screen.Dashboard
                            },
                            onCancel = { currentScreen = Screen.Dashboard },
                            onBack = { currentScreen = Screen.Dashboard },
                            onCreateCategory = { currentScreen = Screen.CreateCategory }
                        )
                        is Screen.CreateCategory -> CreateCategoryScreen(
                            existingCategories = categories,
                            onSave = { newCategory ->
                                if (newCategory.isNotBlank() && !categories.any { it.equals(newCategory, ignoreCase = true) }) {
                                    categories.add(newCategory)
                                    // Persist to database
                                    categoryDao.insert(Category(name = newCategory))
                                }
                                currentScreen = Screen.AddExpense
                            },
                            onCancel = { currentScreen = Screen.AddExpense },
                            onBack = { currentScreen = Screen.AddExpense }
                        )
                        is Screen.AllExpenses -> AllExpensesScreen(
                            expenses = expenseDao.getAll(),
                            onModifyExpense = { expense -> currentScreen = Screen.ModifyExpense(expense) },
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                        is Screen.ModifyExpense -> ModifyExpenseScreen(
                            expense = screen.expense,
                            onSaveClick = { updatedExpense ->
                                if (expenseDao.getAll().any { it.uid == updatedExpense.uid }) {
                                    // Update existing
                                    db.expenseDaoExt().updateExpense(updatedExpense)
                                } else {
                                    // Insert new
                                    expenseDao.insertAll(updatedExpense)
                                }
                                dashboardViewModel.loadDashboardData()
                                currentScreen = Screen.AllExpenses
                            },
                            onDeleteClick = {
                                expenseDao.delete(screen.expense)
                                dashboardViewModel.loadDashboardData()
                                currentScreen = Screen.AllExpenses
                            },
                            onBack = { currentScreen = Screen.AllExpenses }
                        )
                        is Screen.Analytics -> AnalyticsScreen(
                            expenseDao = expenseDao,
                            categoryDao = categoryDao,
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}