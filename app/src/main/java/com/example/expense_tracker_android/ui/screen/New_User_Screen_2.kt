package com.example.expense_tracker_android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

class New_User_Screen_2 {
}

@Composable
fun NewUserScreen2(onContinue: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB3CFFB), Color(0xFF5B8DF7)),
                    startY = 0f, endY = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "How Expense\nTracker Works",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color(0xFF2B5DF5),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Easily add your daily expenses, categorize them, and get instant insights with beautiful charts and summaries. Stay on top of your spending and reach your financial goals!",
                fontSize = 15.sp,
                color = Color(0xFF3A5DB0),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    InfoRow(Icons.Filled.AddCircle, "Add expenses quickly with a tap.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(Icons.Filled.CheckCircle, "Categorize for better tracking.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(Icons.Filled.CheckCircle, "Visualize your spending with analytics.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(Icons.Filled.DateRange, "See your monthly and daily patterns.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(Icons.Filled.Edit, "Edit or delete expenses anytime.")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF5B8DF7)
                )
            ) {
                Text("Continue to Dashboard", color = Color(0xFF5B8DF7), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF5B8DF7), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF2B5DF5))
    }
}

@Preview(showBackground = true)
@Composable
fun NewUserScreen2Preview() {
    NewUserScreen2()
}
