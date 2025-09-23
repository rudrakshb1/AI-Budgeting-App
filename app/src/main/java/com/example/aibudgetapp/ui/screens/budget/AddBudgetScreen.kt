package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBackClick: () -> Unit = {},  //added parameter for back

) {
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }
    val budgetSuccess by remember { derivedStateOf { budgetViewModel.budgetSuccess } }

    val date = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)
    // var selectedDate by remember { mutableStateOf(date[0]) }
    val type = listOf("Weekly", "Monthly")
    var chosenType by remember { mutableStateOf(type[0]) }
    val categories = listOf("Food & Drink", "Rent", "Gas", "Other")
    var chosenCategory by remember { mutableStateOf(categories[0]) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(0) }
    var isDateExpanded by remember { mutableStateOf(false) }
    var isTypeExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Top bar with Back button + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { onBackClick() }) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(75.dp))
            Text(
                text = "New Budget",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        //  existing form starts here
        OutlinedTextField(
            value = name,
            onValueChange = { name = it
                budgetViewModel.budgetSuccess = false },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))

//        ExposedDropdownMenuBox(
//            expanded = isDateExpanded,
//            onExpandedChange = { isDateExpanded = !isDateExpanded },
//        ) {
//            TextField(
//                modifier = Modifier.menuAnchor().fillMaxWidth(),
//                value = selectedDate.toString(),
//                onValueChange = {},
//                readOnly = true,
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDateExpanded) }
//            )
//            ExposedDropdownMenu(expanded = isDateExpanded, onDismissRequest = { isDateExpanded = false }) {
//                date.forEachIndexed { index, day ->
//                    DropdownMenuItem(
//                        text = { Text(text = day.toString()) },
//                        onClick = {
//                            selectedDate = date[index]
//                            isDateExpanded = false
//                        },
//                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
//                    )
//                }
//            }
//        }
//
//        Text(text = "Currently selected: $selectedDate")

        ExposedDropdownMenuBox(
            expanded = isTypeExpanded,
            onExpandedChange = { isTypeExpanded = !isTypeExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = chosenType,
                onValueChange = {budgetViewModel.budgetSuccess = false},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeExpanded) }
            )
            ExposedDropdownMenu(expanded = isTypeExpanded, onDismissRequest = { isTypeExpanded = false }) {
                type.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            chosenType = type[index]
                            isTypeExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $chosenType")

        ExposedDropdownMenuBox(
            expanded = isCategoryExpanded,
            onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = chosenCategory,
                onValueChange = {budgetViewModel.budgetSuccess = false},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) }
            )
            ExposedDropdownMenu(expanded = isCategoryExpanded, onDismissRequest = { isCategoryExpanded = false }) {
                categories.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            chosenCategory = categories[index]
                            isCategoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $chosenCategory")

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = amount.toString(),
            onValueChange = { amount = it.toIntOrNull() ?: 0
                budgetViewModel.budgetSuccess = false},
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            Text("Include savings transfers")
            Spacer(modifier = Modifier.width(140.dp))
            Switch(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
        Button(
            onClick = { budgetViewModel.onAddBudget(name, chosenType, chosenCategory, amount, checked) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save")
        }
        if (budgetError){
            Text(
                "Budget Creation failed. \nPlease check for any incorrect values",
                color = Color.Red,
                modifier = Modifier.padding(16.dp),
            )
        }

        if (budgetSuccess){
            name = ""
            amount = 0
            Text(
                "Budget Successfully Created",
                color = Color.Green,
                modifier = Modifier.padding(16.dp),
            )
        }


    }
}

