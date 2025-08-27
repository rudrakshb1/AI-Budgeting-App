package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aibudgetapp.ui.theme.*

@Composable
fun SettingsScreen() {
    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Text("SettingPage")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SettingsScreen()
}
