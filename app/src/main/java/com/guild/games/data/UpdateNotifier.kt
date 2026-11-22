package com.guild.games.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.guild.games.MainActivity
import com.guild.games.R
import java.io.File

/** Notifikasi sistem untuk update: tersedia, progres unduhan, dan siap dipasang. */
@SuppressLint("MissingPermission")
object UpdateNotifier {
    private const val CH_INFO = "update_info"
    private const val CH_PROGRESS = "update_progress"
    private const val ID_AVAILABLE = 1001
    private const val ID_DOWNLOAD = 1002

    private fun ensureChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_INFO, "Pembaruan aplikasi", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Pemberitahuan versi baru Guild Games"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_PROGRESS, "Unduhan pembaruan", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Progres unduhan pembaruan"
            }
        )
    }

    private fun canNotify(ctx: Context): Boolean {
        if (!Prefs.notifyUpdate(ctx)) return false
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    }

    private fun openApp(ctx: Context): PendingIntent = PendingIntent.getActivity(
        ctx, 0,
        Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun builder(ctx: Context, channel: String) =
        NotificationCompat.Builder(ctx, channel)
            .setSmallIcon(R.drawable.ic_stat_update)
            .setColor(0xFFB026FF.toInt())
            .setContentIntent(openApp(ctx))

    fun available(ctx: Context, info: UpdateInfo) {
        if (!canNotify(ctx)) return
        ensureChannels(ctx)
        val n = builder(ctx, CH_INFO)
            .setContentTitle("Pembaruan tersedia: v${info.versionName}")
            .setContentText("Buka Pengaturan untuk mengunduh versi terbaru Guild Games.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(ID_AVAILABLE, n)
    }

    fun progress(ctx: Context, info: UpdateInfo, percent: Int) {
        if (!canNotify(ctx)) return
        ensureChannels(ctx)
        val n = builder(ctx, CH_PROGRESS)
            .setContentTitle("Mengunduh Guild Games v${info.versionName}")
            .setContentText("$percent%")
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(ID_DOWNLOAD, n)
    }

    fun ready(ctx: Context, info: UpdateInfo, file: File) {
        if (!canNotify(ctx)) return
        ensureChannels(ctx)
        val install = PendingIntent.getActivity(
            ctx, 1, UpdateApi.installIntent(ctx, file),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = builder(ctx, CH_INFO)
            .setContentTitle("Unduhan selesai")
            .setContentText("Ketuk untuk memasang Guild Games v${info.versionName}.")
            .setContentIntent(install)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(ID_DOWNLOAD, n)
    }

    fun cancel(ctx: Context) {
        NotificationManagerCompat.from(ctx).cancel(ID_DOWNLOAD)
    }
}
