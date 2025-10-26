package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.constants.CategoryType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBackClick: () -> Unit = {},
    budgetToEdit: Budget? = null
) {
    val budgetViewModel = remember { BudgetViewModel(BudgetRepository()) }
    val budgetError by remember { derivedStateOf { budgetViewModel.budgetError } }
    val errorMessage by remember { derivedStateOf { budgetViewModel.errorMessage } }
    val budgetSuccess by remember { derivedStateOf { budgetViewModel.budgetSuccess } }
    val isEditMode = budgetToEdit != null

    // form states
    var name by remember { mutableStateOf("") }
    var recursion by remember { mutableStateOf(1) }
    var amount by remember { mutableStateOf(0.0) }
    val type = listOf("Weekly", "Monthly", "Yearly")
    var chosenType by remember { mutableStateOf(type[0]) }
    val categories = CategoryType.entries.map { it.value }
    var chosenCategory by remember { mutableStateOf(categories[0]) }
    var isTypeExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var isManualCategory by remember { mutableStateOf(false) }

    // Auto-calculate end date whenever start/type changes
    val endDate = if (recursion != 0) {
        val start: LocalDate? = try {
            LocalDate.parse(startDate.trim(), DateTimeFormatter.ofPattern("yyyy-M-d"))
        } catch(e: Exception) {
            null
        }

        if (start != null) {
            if (chosenType.equals("Weekly", ignoreCase = true)) {
                // unchanged
                start.plusDays(7L * recursion - 1).toString()
            } else if (chosenType.equals("Yearly", ignoreCase = true)) {
                // NEW: yearly uses years instead of months (same “minus 1 day if same-day” rule)
                val endRaw = start.plusYears(recursion.toLong())
                if (endRaw.dayOfMonth == start.dayOfMonth) {
                    endRaw.minusDays(1).toString()
                } else {
                    endRaw.toString()
                }
            } else {
                // unchanged: Monthly (or other non-weekly types you treat as monthly)
                val endRaw = start.plusMonths(recursion.toLong())
                if (endRaw.dayOfMonth == start.dayOfMonth) {
                    endRaw.minusDays(1).toString()
                } else {
                    endRaw.toString()
                }
            }
        } else {
            "Invalid date"
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
                text = if (isEditMode) "Edit Budget" else "New Budget",
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

        //Editable START date
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start Date") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        //Editable Recursive
        OutlinedTextField(
            value = recursion.toString(),
            onValueChange = {
                recursion = max(1, it.toIntOrNull() ?: 1)
                budgetViewModel.budgetSuccess = false
            },
            label = { Text("Recursion") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        //System-calculated END date
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
                            // CHANGE: when switching to Yearly, close & clear category UI state
                            if (text == "Yearly") {
                                isCategoryExpanded = false
                                isManualCategory = false
                                // (optional) keep chosenCategory as-is; it won't be used for Yearly
                            }
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $chosenType")


        // Category controls
        if (chosenType != "Yearly") {
            // CHANGE: show category picker ONLY for Weekly/Monthly
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
                                isManualCategory = text == "Other"
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Text(text = "Currently selected: $chosenCategory")

            if (isManualCategory) {
                OutlinedTextField(
                    value = chosenCategory,
                    onValueChange = { chosenCategory = it },
                    label = { Text("Manual Category") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            // CHANGE: simple hint for Yearly
            Text(
                text = "Yearly goals don’t use categories. All transactions are included.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Amount input
        OutlinedTextField(
            value = amount.toString(),
            onValueChange = {
                val parsed = it.toDoubleOrNull()?.times(100) ?: 0.0
                val limited = min(parsed, 999999999.0)
                amount = floor(limited) / 100
                budgetViewModel.budgetSuccess = false
            },
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
                // Yearly should not pass a category
                val categoryForSave: String? =
                    if (chosenType == "Yearly") null else chosenCategory

                if (isEditMode){
                    val updatedbudget = budgetToEdit!!.copy(
                        name = name,
                        chosenType = chosenType,
                        chosenCategory = categoryForSave,
                        amount = amount,
                        checked = checked,
                        startDate = startDate,
                        endDate = endDate
                    )
                    budgetViewModel.updateBudget(updatedbudget)
                } else {

                    budgetViewModel.onAddBudget(
                        name,
                        chosenType,
                        categoryForSave ?: "",   // <-- if function requires String; safe fallback
                        amount,
                        checked,
                        startDate,
                        endDate
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(if (isEditMode) "Update" else "Save")
        }


        // Error or Success messages
        if (budgetError) {
            Text(
                text = errorMessage ?: "Budget Creation failed.\nPlease check for any incorrect values",
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }

        if (budgetSuccess) {
            name = ""
            amount = 0.0
            Text(
                if (isEditMode) "Budget Successfully Updated" else "Budget Successfully Created",
                color = Color.Green,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
