package com.example.aibudgetapp.ocr

object AutoCategorizer {
    private val rules = mapOf(
        "Groceries" to listOf("countdown","new world","pak","four square","foodstuffs","farro"),
        "Food & Drink" to listOf("cafe","coffee","restaurant","kfc","mcdonald","subway","burger"),
        "Transport" to listOf("uber","fuel","petrol","gas","caltex","bp","z energy"),
        "Entertainment" to listOf("netflix","spotify","movie","cinema"),
        "Bills" to listOf("spark","vodafone","mercury","watercare"),
        "Shopping" to listOf("kmart","warehouse","briscoes","noel leeming")
    )
    fun guess(text: String): String {
        val t = text.lowercase()
        for ((cat, keys) in rules) if (keys.any { t.contains(it) }) return cat
        return "Uncategorized"
    }
}