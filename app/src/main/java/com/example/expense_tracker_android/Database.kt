package com.example.expense_tracker_android

import androidx.room.*
import java.sql.Time

@Entity
data class Expense(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "Expense_Name") val Expense_Name: String?,
    @ColumnInfo(name = "Amount") val amount: Double?,
    @ColumnInfo(name = "Date") val date: String?, // Store as String (yyyy-MM-dd)
    @ColumnInfo(name = "Time")val time: Time?,
    @ColumnInfo(name = "Category") val category: String?
)



@Dao
interface ExpenseDao {
    @Query("SELECT * FROM Expense")
    fun getAll(): List<Expense>

    @Query("SELECT * FROM Expense WHERE uid IN (:expenseIds)")
    fun loadAllByIds(expenseIds: IntArray): List<Expense>

    @Query("SELECT * FROM Expense WHERE Expense_Name LIKE :expenseName LIMIT 1")
    fun findByName(expenseName: String): Expense

    @Insert
    fun insertAll(vararg expenses: Expense)

    @Delete
    fun delete(expense: Expense)

    @Query("SELECT COALESCE(SUM(Amount), 0) FROM Expense WHERE strftime('%m', Date) = :month AND strftime('%Y', Date) = :year")
    fun getTotalExpenseForMonth(month: String, year: String): Double

    @Query("SELECT COALESCE(SUM(Amount), 0) FROM Expense WHERE Date = :date")
    fun getDailyExpense(date: String): Double
}

@Database(entities = [Expense::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseDaoExt(): ExpenseDaoExt // Added for update support
}
