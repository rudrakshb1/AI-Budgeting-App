package com.example.aibudgetapp.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.aibudgetapp.R
import java.time.LocalDate

object ThresholdNotifier {
    private const val PREFS = "budget_threshold_prefs"
    private const val THRESHOLD = 90.0 // percent

    /**
     * Notify exactly once per period when crossing from <THRESHOLD to ≥THRESHOLD.
     * Returns true if a notification was sent.
     *
     * @param label    "Weekly" or "Monthly"
     * @param periodId e.g. "2025-W42" (weekly) or "2025-10" (monthly)
     */
    fun maybeNotifyCrossing(
        context: Context,
        label: String,
        periodId: String,
        spent: Double,
        budget: Double
    ): Boolean {
        if (budget <= 0.0) return false

        val pct = (spent / budget) * 100.0
        val nowAbove = pct >= THRESHOLD

        // Track state: were we already above for this period?
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val aboveKey = "above_${label.lowercase()}_$periodId"
        val wasAbove = prefs.getBoolean(aboveKey, false)
        prefs.edit { putBoolean(aboveKey, nowAbove) }  // update for next time

        // Only alert on first crossing this period
        if (!wasAbove && nowAbove) {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return false

            NotificationChannels.ensureCreated(context)

            fun fmt(v: Double) = String.format("%.2f", v)
            val pctInt = pct.toInt()
            val remaining = (budget - spent).coerceAtLeast(0.0)
            val title = if (spent >= budget) "$label budget exceeded" else "$label budget alert"
            val text = "$label budget is $${fmt(budget)}. Spent $${fmt(spent)} (${pctInt}%). " +
                    "Remaining $${fmt(remaining)}."

            val id = if (label == "Monthly") 2001 else 2002
            val notif = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_BUDGET_ALERTS)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // swap to app icon later
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(id, notif)
            return true
        }

        return false
    }
}