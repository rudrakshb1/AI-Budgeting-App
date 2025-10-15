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

    fun maybeNotifyBudget(
        context: Context,
        label: String,            // "Monthly" or "Weekly"
        spent: Double,
        budget: Double
    ) {
        if (budget <= 0.0) return
        val pct = (spent / budget) * 100.0

        // once-per-day guard per label
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val todayKey = "notified_${label.lowercase()}_${LocalDate.now()}"
        val last = prefs.getString(todayKey, null)
        val today = LocalDate.now().toString()

        if (pct < THRESHOLD) {
            // dropped below: allow future notifications again
            prefs.edit { remove(todayKey) }
            return
        }
        if (last == today) return

        // Android 13+ runtime permission guard
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // no permission → skip safely
        }

        NotificationChannels.ensureCreated(context)

        val text = "$label budget at ${pct.toInt()}%. Spent " +
                "$${"%.2f".format(spent)} / $${"%.2f".format(budget)}"

        val notif = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with app icon later
            .setContentTitle("Budget nearing limit")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        val id = if (label == "Monthly") 2001 else 2002
        NotificationManagerCompat.from(context).notify(id, notif)

        prefs.edit { putString(todayKey, today) }
    }
}