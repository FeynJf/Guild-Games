package com.guild.games.data

import android.content.Context

/** Pengaturan sederhana yang disimpan di perangkat. */
object Prefs {
    private fun sp(ctx: Context) =
        ctx.applicationContext.getSharedPreferences("guild_prefs", Context.MODE_PRIVATE)

    fun autoCheck(ctx: Context) = sp(ctx).getBoolean("auto_check", true)
    fun setAutoCheck(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("auto_check", v).apply()

    fun notifyUpdate(ctx: Context) = sp(ctx).getBoolean("notify_update", true)
    fun setNotifyUpdate(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("notify_update", v).apply()

    /** Versi terakhir yang sudah diberi notifikasi, supaya tidak spam. */
    fun lastNotified(ctx: Context): String = sp(ctx).getString("last_notified", "").orEmpty()
    fun setLastNotified(ctx: Context, v: String) = sp(ctx).edit().putString("last_notified", v).apply()

    fun lastCheck(ctx: Context) = sp(ctx).getLong("last_check", 0L)
    fun setLastCheck(ctx: Context, v: Long) = sp(ctx).edit().putLong("last_check", v).apply()
}
