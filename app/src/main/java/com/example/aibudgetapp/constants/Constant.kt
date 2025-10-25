package com.example.aibudgetapp.constants

const val DAYS_PER_WEEK = 7.0
const val DATE_FORMAT = "yyyy-MM-dd"
const val DATE_FORMAT_MMM_D = "MMM d"
enum class BudgetType(val value: String) {
    MONTHLY("Monthly"),
    WEEKLY("Weekly")
}
enum class CategoryType(val value: String) {
    FOOD_AND_DRINK("Food & Drink"),
    RENT("Rent"),
    UTILITIES("Utilities"),
    FUEL_TRANSPORT("Fuel & Transport"),
    HEALTH("Health"),
    SHOPPING("Shopping"),
    OTHER("Other")
}