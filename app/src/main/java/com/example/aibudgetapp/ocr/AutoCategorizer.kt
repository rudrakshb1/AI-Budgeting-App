package com.example.aibudgetapp.ocr


object AutoCategorizer {

    // Add/adjust keywords freely. Keep everything lowercase.
    private val rules: Map<String, List<String>> = mapOf(
        "Groceries" to listOf(
            // NZ majors
            "countdown", "new world", "paknsave", "pak 'n save", "four square", "foodstuffs", "farro",
            // generic/local market words
            "supermarket", "grocery", "grocer", "mart", "asian", "indian", "korean", "japanese",
            "thai", "china", "chinese", "fruit & veg", "produce", "butchery", "fishmonger"
        ),
        "Food & Drink" to listOf(
            "cafe", "coffee", "restaurant", "eatery", "bar", "pub", "bistro",
            "kfc", "mcdonald", "subway", "burger king", "domino", "pizza hut", "starbucks", "bubble tea", "boba"
        ),
        "Transport" to listOf(
            "uber", "lyft", "taxi", "bus", "train", "metro",
            "fuel", "petrol", "gas", "caltex", "bp", "z energy", "parking"
        ),
        "Entertainment" to listOf(
            "netflix", "spotify", "disney", "prime video", "movie", "cinema", "theatre", "concert"
        ),
        "Bills" to listOf(
            "spark", "vodafone", "2degrees", "mercury", "genesis", "contact energy",
            "watercare", "electricity", "internet", "broadband", "mobile", "phone", "utilities"
        ),
        "Shopping" to listOf(
            "kmart", "the warehouse", "warehouse", "briscoes", "noel leeming",
            "harvey norman", "jb hi-fi", "mitre 10", "bunnings", "farmers"
        )
    )

    /**
     * Guess a category from OCR text.
     * - Normalizes punctuation/case.
     * - Scores categories by count of keyword hits; highest score wins.
     */
    fun guess(text: String): String {
        val normalized = normalize(text)

        var bestCategory: String? = null
        var bestScore = 0

        for ((category, keys) in rules) {
            val score = keys.count { key ->
                val k = normalize(key)
                // allow simple partial match (word boundary improves precision)
                normalized.contains(k)
            }
            if (score > bestScore) {
                bestScore = score
                bestCategory = category
            }
        }

        return bestCategory ?: "Uncategorized"
    }

    private fun normalize(s: String): String =
        s.lowercase()
            .replace(Regex("[^a-z0-9& ]"), " ")   // drop punctuation (keep &)
            .replace(Regex("\\s+"), " ")          // collapse spaces
            .trim()
}
