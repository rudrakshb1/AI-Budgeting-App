package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBackClick: () -> Unit = {},
) {
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }
    val budgetSuccess by remember { derivedStateOf { budgetViewModel.budgetSuccess } }

    // form states
    var name by remember { mutableStateOf("") }
    var recursive by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf(0) }
    val type = listOf("Weekly", "Monthly")
    var chosenType by remember { mutableStateOf(type[0]) }
    val categories = listOf("Food & Drink", "Rent", "Gas", "Other")
    var chosenCategory by remember { mutableStateOf(categories[0]) }
    var isTypeExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }

    // --- Auto Start & End Date ---
    val endDate = if (recursive != 0) {
        val start = LocalDate.parse(startDate)
        if (chosenType.equals("Weekly", ignoreCase = true)) {
            start.plusDays((7L * recursive) - 1).toString()
        } else {
            val endRaw = start.plusMonths(recursive.toLong())
            if (endRaw.dayOfMonth == start.dayOfMonth) {
                endRaw.minusDays(1).toString()
            } else {
                endRaw.toString()
            }
        }
    } else {
        startDate
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; budgetViewModel.budgetSuccess = false },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))

        // --- Editable START date
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start Date") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // --- Editable Recursive
        OutlinedTextField(
            value = recursive.toString(),
            onValueChange = { recursive = it.toIntOrNull() ?: 0; budgetViewModel.budgetSuccess = false },
            label = { Text("Recursive") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // --- System-calculated END date
        OutlinedTextField(
            value = endDate,
            onValueChange = {},
            label = { Text("End Date (auto)") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Budget Type Dropdown
        ExposedDropdownMenuBox(
            expanded = isTypeExpanded,
            onExpandedChange = { isTypeExpanded = !isTypeExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = chosenType,
                onValueChange = { budgetViewModel.budgetSuccess = false },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isTypeExpanded,
                onDismissRequest = { isTypeExpanded = false }
            ) {
                type.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            chosenType = text
                            isTypeExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $chosenType")

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = isCategoryExpanded,
            onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = chosenCategory,
                onValueChange = { budgetViewModel.budgetSuccess = false },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false }
            ) {
                categories.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            chosenCategory = text
                            isCategoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $chosenCategory")

        Spacer(modifier = Modifier.height(12.dp))

        // Amount input
        OutlinedTextField(
            value = amount.toString(),
            onValueChange = { amount = it.toIntOrNull() ?: 0; budgetViewModel.budgetSuccess = false },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Savings switch
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

        // Save button
        Button(
            onClick = {
                budgetViewModel.onAddBudget(
                    name,
                    chosenType,
                    chosenCategory,
                    amount,
                    checked,
                    startDate,
                    endDate
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Save")
        }

        // Error or Success messages
        if (budgetError) {
            Text(
                "Budget Creation failed. \nPlease check for any incorrect values",
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }

        if (budgetSuccess) {
            name = ""
            amount = 0
            Text(
                "Budget Successfully Created",
                color = Color.Green,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
