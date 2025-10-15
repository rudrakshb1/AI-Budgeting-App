package com.example.aibudgetapp.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aibudgetapp.R

object SimpleNotifier {
    fun showTest(context: Context) {
        NotificationChannels.ensureCreated(context)

        // Android 13+ requires runtime permission
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                // If we have an Activity context, request it
                if (context is Activity) {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
                return
            }
        }

        val notif = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_BUDGET_ALERTS)
            // TEMP icon so you can compile even if you haven't added a vector asset yet:
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // (Later you can switch to R.drawable.ic_stat_notify after you add the vector)
            .setContentTitle("Budget nearing limit")
            .setContentText("Demo alert from the bell. Wiring works!")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notif)
    }
}