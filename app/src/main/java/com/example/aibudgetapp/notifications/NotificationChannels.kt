package com.example.aibudgetapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHANNEL_BUDGET_ALERTS = "budget_alerts"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            CHANNEL_BUDGET_ALERTS,
            "Budget Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts when you’re nearing or exceeding budgets." }
        nm.createNotificationChannel(ch)
    }
}