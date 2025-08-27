package com.example.aibudgetapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.theme.*

@Composable
fun HomeScreen(
    userName: String
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

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    HomeScreen("Spencer")
}
