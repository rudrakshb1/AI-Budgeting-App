package com.example.aibudgetapp.notifications


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.aibudgetapp.R
import java.util.Locale

object ThresholdNotifier {
    private const val TAG = "Threshold"
    private const val PREFS = "budget_threshold_prefs"
    private const val THRESHOLD = 90.0 // percent

    private const val KEY_REMINDERS_ENABLED = "reminders_enabled"

    // Backfill state
    private const val MIGRATION_PREFS = "notif_migrations"
    private const val BACKFILLED_V1 = "notif_backfilled_v1"

    fun isRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
    }

    /** Persists the reminders toggle (true = enabled). */
    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_REMINDERS_ENABLED, enabled) }
    }

    /**
     * One-time: write a history entry (NO system notification) if already ≥ THRESHOLD for this period
     * AND we've previously been above (once-per-period latch is set).
     *
     * @param scale multiplies the given budget before computing percentages
     *              (e.g. 10.0 to interpret 20 from the chart as 200 real dollars).
     */
    fun backfillIfNeeded(
        context: Context,
        label: String,      // "Weekly" or "Monthly"
        periodId: String,   // e.g., "2025-W42" or "2025-10"
        spent: Double,
        budget: Double,
        scale: Double = 1.0
    ) {
        val mig = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        val key = "$BACKFILLED_V1-$label-$periodId"
        if (mig.getBoolean(key, false)) {
            Log.d(TAG, "backfill: already backfilled for $label/$periodId")
            return
        }

        val effBudget = budget * scale
        if (effBudget <= 0.0) {
            Log.d(TAG, "backfill: budget<=0, skip ($label/$periodId)")
            return
        }

        val pct = (spent / effBudget) * 100.0

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val aboveKey = "above_${label.lowercase()}_$periodId"
        val wasAbove = prefs.getBoolean(aboveKey, false)

        Log.d(
            TAG,
            "backfill? label=$label period=$periodId spent=$spent budget(raw)=$budget scale=$scale effBudget=$effBudget pct=$pct wasAbove=$wasAbove"
        )

        var didLog = false
        if (pct >= THRESHOLD && wasAbove) {
            NotificationLog.log(
                context,
                NotificationEvent(
                    id = System.currentTimeMillis(),
                    label = label,
                    periodId = periodId,
                    percent = pct,
                    spent = spent,
                    budget = effBudget,
                    message = "${label} budget ${"%.1f".format(Locale.US, pct)}% used (backfilled)",
                    read = false
                )
            )
            didLog = true
            Log.d(TAG, "backfill: logged entry for $label/$periodId")
        } else {
            Log.d(TAG, "backfill: conditions not met (no log)")
        }

        if (didLog) mig.edit { putBoolean(key, true) }
    }

    /**
     * Notify exactly once per period when crossing from <THRESHOLD to ≥THRESHOLD.
     * Accepts a scale to convert chart budgets into real $ budgets.
     *
     * @return true if a system notification was posted (and the event logged).
     */
    fun maybeNotifyCrossing(
        context: Context,
        label: String,
        periodId: String,
        spent: Double,
        budget: Double,
        scale: Double = 1.0
    ): Boolean {
        val effBudget = budget * scale
        if (!isRemindersEnabled(context)) {
            Log.d(TAG, "maybe: reminders OFF; skip ($label/$periodId)")
            return false
        }
        if (effBudget <= 0.0) {
            Log.d(TAG, "maybe: budget<=0, skip ($label/$periodId)")
            return false
        }

        val pct = (spent / effBudget) * 100.0
        val nowAbove = pct >= THRESHOLD

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val aboveKey = "above_${label.lowercase()}_$periodId"
        val wasAbove = prefs.getBoolean(aboveKey, false)
        prefs.edit { putBoolean(aboveKey, nowAbove) }

        Log.d(
            TAG,
            "maybe: label=$label period=$periodId spent=$spent budget(raw)=$budget scale=$scale effBudget=$effBudget pct=$pct wasAbove=$wasAbove nowAbove=$nowAbove"
        )

        if (!wasAbove && nowAbove) {
            val title =
                if (spent >= effBudget) "$label budget exceeded" else "$label budget alert"
            val text = buildText(spent, effBudget, label)

            val posted = postSystemNotificationIfPermitted(
                context = context,
                label = label,
                title = title,
                text = text
            )

            // Always log the in-app event (even if OS permission blocked the banner)
            NotificationLog.log(
                context,
                NotificationEvent(
                    id = System.currentTimeMillis(),
                    label = label,
                    periodId = periodId,
                    percent = pct,
                    spent = spent,
                    budget = effBudget,
                    message = text,
                    read = false
                )
            )
            Log.d(TAG, "maybe: logged entry for $label/$periodId (postedSystem=$posted)")
            return posted
        }

        Log.d(TAG, "maybe: no new alert (already above or still below)")
        return false
    }

    private fun buildText(spent: Double, budget: Double, label: String): String {
        fun fmt(v: Double) = String.format(Locale.US, "%.2f", v)
        val pctInt = ((spent / budget) * 100.0).toInt()
        val remaining = (budget - spent).coerceAtLeast(0.0)
        return "$label budget is $${fmt(budget)}. Spent $${fmt(spent)} (${pctInt}%). Remaining $${fmt(remaining)}."
    }

    private fun postSystemNotificationIfPermitted(
        context: Context,
        label: String,
        title: String,
        text: String
    ): Boolean {
        if (!isRemindersEnabled(context)) {
            Log.d(TAG, "post: reminders OFF; skip system banner")
            return false
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "post: notifications permission missing; skip system banner")
            return false
        }

        NotificationChannels.ensureCreated(context)

        // Use a safe built-in small icon to avoid missing resource errors.
        val smallIcon = android.R.drawable.ic_dialog_info
        val channelId = NotificationChannels.CHANNEL_BUDGET_ALERTS

        val id = if (label == "Monthly") 2001 else 2002
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notif)
        Log.d(TAG, "post: posted system notification id=$id")
        return true
    }

    /** Optional: clears the once-per-period latch for quick manual re-tests. */
    fun resetThresholdState(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { clear() }
        Log.d(TAG, "reset: cleared $PREFS")
    }


    //Yearly convenience wrappers (reuse the generic threshold logic)
    fun backfillYearlyIfNeeded(
        context: Context,
        yearId: String,   // e.g. "2025"
        spent: Double,
        budget: Double
    ) {
        backfillIfNeeded(context, label = "Yearly", periodId = yearId, spent = spent, budget = budget)
    }

    fun maybeNotifyYearly(
        context: Context,
        yearId: String,   // e.g. "2025"
        spent: Double,
        budget: Double
    ): Boolean {
        return maybeNotifyCrossing(context, label = "Yearly", periodId = yearId, spent = spent, budget = budget)
    }

}
