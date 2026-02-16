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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AllExpensesScreen(
    expenses: List<Expense>,
    onModifyExpense: (Expense) -> Unit,
    onBack: () -> Unit
) {
    var expandedUid by remember { mutableStateOf<Int?>(null) }
    // Remove any parent Column/Scroll, use LazyColumn as the root
    LazyColumn(
        modifier = Modifier
            .background(Color(0xFFF7F9FB))
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header as an item in LazyColumn
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "<",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF2B5DF5),
                    modifier = Modifier.clickable { onBack() }
                )
                Spacer(Modifier.width(8.dp))
                Text("All Expenses", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2B5DF5))
            }
            Spacer(Modifier.height(16.dp))
        }
        // Sort expenses by date and time descending (recent first)
        val sortedExpenses = expenses.sortedWith(compareByDescending<Expense> {
            it.date ?: ""
        }.thenByDescending {
            it.time?.time ?: 0L
        })
        // Expense items
        items(sortedExpenses) { expense ->
            val isExpanded = expandedUid == expense.uid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { expandedUid = if (isExpanded) null else expense.uid },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF7F9FB)),
                        contentAlignment = Alignment.Center
                    ) {
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
                if (isExpanded) {
                    Spacer(Modifier.height(4.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F9FB), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date & Time:", color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold)
                            Text(
                                buildString {
                                    append(
                                        expense.date?.let {
                                            try {
                                                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                                                if (parsed != null) SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(parsed) else "-"
                                            } catch (e: Exception) { "-" }
                                        } ?: "-"
                                    )
                                    append("  ")
                                    append(
                                        expense.time?.let {
                                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
                                        } ?: "-"
                                    )
                                },
                                color = Color(0xFF2B5DF5), fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Category:", color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold)
                            Text(expense.category ?: "-", color = Color(0xFF2B5DF5))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Note:", color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold)
                            Text(expense.Expense_Name ?: "-", color = Color(0xFF2B5DF5))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount:", color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold)
                            Text("₹%.2f".format(expense.amount ?: 0.0), color = Color(0xFF2B5DF5), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onModifyExpense(expense) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5DF5)),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("\uD83D\uDD8C Modify", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllExpensesScreenPreview() {
    val mockExpenses = listOf(
        Expense(
            uid = 1,
            Expense_Name = "Dinner at restaurant",
            amount = 123.0,
            date = "2026-02-11",
            time = java.sql.Time(17, 11, 0),
            category = "Other"
        ),
        Expense(
            uid = 2,
            Expense_Name = "Bus ticket",
            amount = 123.0,
            date = "2026-02-10",
            time = java.sql.Time(8, 30, 0),
            category = "Transport"
        )
    )
    AllExpensesScreen(
        expenses = mockExpenses,
        onModifyExpense = {},
        onBack = {}
    )
}
