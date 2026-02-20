package com.example.expense_tracker_android.model

import androidx.room.*
import java.sql.Time
import androidx.room.Query

@Entity
data class Expense(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "Expense_Name") val Expense_Name: String?,
    @ColumnInfo(name = "Amount") val amount: Double?,
    @ColumnInfo(name = "Date") val date: String?, // Store as String (yyyy-MM-dd)
    @ColumnInfo(name = "Time")val time: Time?,
    @ColumnInfo(name = "Category") val category: String?
)

@Entity
data class Category(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "icon") val icon: String = "💰",
    @ColumnInfo(name = "color") val color: Long = 0xFFFFF59D // Default color in ARGB format
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

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category ORDER BY name ASC")
    fun getAll(): List<Category>

    @Query("SELECT name FROM Category ORDER BY name ASC")
    fun getAllCategoryNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Category)

    @Delete
    fun delete(category: Category)

    @Query("SELECT * FROM Category WHERE name = :name LIMIT 1")
    fun getByName(name: String): Category?
}

@Database(entities = [Expense::class, Category::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseDaoExt(): ExpenseDaoExt // Added for update support
    abstract fun categoryDao(): CategoryDao
}
