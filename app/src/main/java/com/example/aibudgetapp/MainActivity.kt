package com.example.aibudgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aibudgetapp.ui.theme.AIBudgetAppTheme
import com.example.aibudgetapp.ui.components.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import com.example.aibudgetapp.ui.theme.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .padding(bottom = 35.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {
                    HomeButton()
                    AddTransactionButton()
                    SettingsButton()
                }
            }

        ) { innerPadding ->
            Greeting(
                name = "Spencer",
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
    AppContent()
}