package com.example.expense_tracker_android.model

import androidx.room.Dao
import androidx.room.Update

@Dao
interface ExpenseDaoExt {
    @Update
    fun updateExpense(expense: Expense)
}