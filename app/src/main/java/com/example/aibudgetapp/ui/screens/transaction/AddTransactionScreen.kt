package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.theme.*
import androidx.navigation.NavController




@Composable
fun AddTransactionScreen(navController: NavController? = null) {
    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Text("AddTransactionPage")

                // Button for navigating to DocumentUploadScreen
                Button(
                    onClick = { navController?.navigate("documentUpload") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Text("Scan/Upload Document (Camera)")
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AddTransactionScreen()
}