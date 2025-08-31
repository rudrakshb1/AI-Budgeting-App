package com.example.aibudgetapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.aibudgetapp.ui.theme.Cream
import com.example.aibudgetapp.ui.theme.DarkNavy

@Composable
fun BottomNavBar(
    onHomeClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavy),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HomeButton(onHomeClick)
        BudgetButton(onBudgetClick)
        SettingsButton(onSettingsClick)
    }
}

@Composable
fun HomeButton(
    onHomeClick: () -> Unit,
) {
    Text(
        "Home",
        color= Cream,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            onHomeClick()
        })
}

@Composable
fun BudgetButton(
    onBudgetClick: () -> Unit
) {
    Text(
        "Budget",
        color= Cream,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            onBudgetClick()
        })
}

@Composable
fun SettingsButton(
    onSettingsClick: () -> Unit
) {
    Text(
        "Settings",
        color= Cream,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            onSettingsClick()
        })
}