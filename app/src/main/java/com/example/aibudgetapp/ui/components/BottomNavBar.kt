package com.example.aibudgetapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.aibudgetapp.ui.theme.Cream

@Composable
fun HomeButton() {
    Text(
        "Home",
        color= Cream,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            // TODO: Add click behaviour and delay
        })
}

@Composable
fun AddTransactionButton() {
    Text(
        "Add Transaction",
        color= Cream,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            // TODO: Add click behaviour and delay
        })
}

@Composable
fun SettingsButton() {
    Text(
        "Settings",
        color= Cream,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            // TODO: Add click behaviour and delay
        })
}