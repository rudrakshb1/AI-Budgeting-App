package com.example.aibudgetapp.notifications

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class NotificationEvent(
    val id: Long,          // System.currentTimeMillis()
    val label: String,     // "Weekly" / "Monthly"
    val periodId: String,  // "2025-W42" or "2025-10"
    val percent: Double,
    val spent: Double,
    val budget: Double,
    val message: String,
    val read: Boolean = false
)

object NotificationLog {
    private const val PREFS = "notif_log_prefs"
    private const val KEY = "events" // JSON array as string

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun toJson(ev: NotificationEvent): JSONObject {
        val o = JSONObject()
        o.put("id", ev.id)
        o.put("label", ev.label)
        o.put("periodId", ev.periodId)
        o.put("percent", ev.percent)
        o.put("spent", ev.spent)
        o.put("budget", ev.budget)
        o.put("message", ev.message)
        o.put("read", ev.read)
        return o
    }

    private fun fromJson(o: JSONObject): NotificationEvent =
        NotificationEvent(
            id = o.optLong("id"),
            label = o.optString("label"),
            periodId = o.optString("periodId"),
            percent = o.optDouble("percent"),
            spent = o.optDouble("spent"),
            budget = o.optDouble("budget"),
            message = o.optString("message"),
            read = o.optBoolean("read", false)
        )

    private fun readArray(ctx: Context): JSONArray {
        val raw = prefs(ctx).getString(KEY, "[]") ?: "[]"
        return try { JSONArray(raw) } catch (_: JSONException) { JSONArray() }
    }

    private fun writeArray(ctx: Context, arr: JSONArray) {
        prefs(ctx).edit().putString(KEY, arr.toString()).apply()
    }

    fun log(ctx: Context, ev: NotificationEvent) {
        // newest first
        val existing = readArray(ctx)
        val out = JSONArray().apply { put(toJson(ev)) }
        for (i in 0 until existing.length()) out.put(existing.getJSONObject(i))
        writeArray(ctx, out)
    }

    fun getAll(ctx: Context): List<NotificationEvent> {
        val arr = readArray(ctx)
        val list = ArrayList<NotificationEvent>(arr.length())
        for (i in 0 until arr.length()) list.add(fromJson(arr.getJSONObject(i)))
        return list
    }

    fun getUnreadCount(ctx: Context): Int = getAll(ctx).count { !it.read }

    fun markAllRead(ctx: Context) {
        val arr = readArray(ctx)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            o.put("read", true)
        }
        writeArray(ctx, arr)
    }
}

