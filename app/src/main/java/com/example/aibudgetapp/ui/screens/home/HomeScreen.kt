package com.example.aibudgetapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.screenContainer.Screen
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainer
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainerViewModel
import com.example.aibudgetapp.ui.theme.*

@Composable
fun HomeScreen(
    userName: String,
    screenContainerViewModel: ScreenContainerViewModel,
) {
    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

        ) { innerPadding ->
            Greeting(
                name = userName,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd,
    ){
    Button(
        onClick = { screenContainerViewModel.navigateTo(Screen.ADDTRANSACTION) },
    ) {
        Text("+")
    }
}
    }

@Composable
fun Greeting (
    name: String,
    modifier: Modifier = Modifier) {
    Text(
        text = "Welcome, $name!",
        modifier = Modifier.padding(32.dp)
    )
}


