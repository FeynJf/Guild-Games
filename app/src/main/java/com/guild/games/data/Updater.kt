package com.guild.games.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.guild.games.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionName: String,
    val notes: String,
    val apkUrl: String,
    val sizeBytes: Long
)

sealed interface UpdateCheck {
    object UpToDate : UpdateCheck
    data class Available(val info: UpdateInfo) : UpdateCheck
    data class Failed(val message: String) : UpdateCheck
}

/** Status yang dilihat layar Pengaturan. */
sealed interface UpdateUi {
    object Idle : UpdateUi
    object Checking : UpdateUi
    object UpToDate : UpdateUi
    data class Available(val info: UpdateInfo) : UpdateUi
    data class Downloading(val info: UpdateInfo, val done: Long, val total: Long, val bytesPerSec: Long) : UpdateUi
    data class Ready(val info: UpdateInfo, val file: File) : UpdateUi
    data class Error(val message: String, val info: UpdateInfo? = null) : UpdateUi
}

/** Bicara dengan GitHub Releases: cek versi, unduh APK, pasang. */
object UpdateApi {
    private const val TIMEOUT = 15_000

    fun isNewer(remote: String, local: String): Boolean {
        fun parts(v: String) = v.trim().removePrefix("v").removePrefix("V")
            .split('.', '-', '+')
            .map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val r = parts(remote)
        val l = parts(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    /** Blocking, panggil dari Dispatchers.IO. */
    fun latest(): UpdateCheck {
        val conn = try {
            (URL("https://api.github.com/repos/${Config.GITHUB_REPO}/releases/latest").openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT
                readTimeout = TIMEOUT
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GuildGames-Android")
            }
        } catch (e: Exception) {
            return UpdateCheck.Failed("Tidak bisa menghubungi server pembaruan")
        }
        return try {
            when (val code = conn.responseCode) {
                200 -> {
                    val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                    val tag = json.optString("tag_name")
                    var apkUrl = ""
                    var size = 0L
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val a = assets.getJSONObject(i)
                            if (a.optString("name").endsWith(".apk", ignoreCase = true)) {
                                apkUrl = a.optString("browser_download_url")
                                size = a.optLong("size")
                                break
                            }
                        }
                    }
                    if (tag.isBlank() || apkUrl.isBlank() || !isNewer(tag, BuildConfig.VERSION_NAME)) {
                        UpdateCheck.UpToDate
                    } else {
                        UpdateCheck.Available(
                            UpdateInfo(tag.removePrefix("v").removePrefix("V"), json.optString("body"), apkUrl, size)
                        )
                    }
                }
                404 -> UpdateCheck.UpToDate // belum ada rilis
                403, 429 -> UpdateCheck.Failed("Batas pengecekan GitHub tercapai, coba lagi nanti")
                else -> UpdateCheck.Failed("Server pembaruan membalas $code")
            }
        } catch (e: Exception) {
            UpdateCheck.Failed("Gagal cek pembaruan. Periksa koneksi internet kamu.")
        } finally {
            conn.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    suspend fun download(ctx: Context, info: UpdateInfo, onProgress: (done: Long, total: Long) -> Unit): File =
        withContext(Dispatchers.IO) {
            val dir = File(ctx.cacheDir, "updates").apply {
                mkdirs()
                listFiles()?.forEach { it.delete() }
            }
            val out = File(dir, "GuildGames-${info.versionName}.apk")
            val part = File(dir, out.name + ".part")
            val conn = URL(info.apkUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = TIMEOUT
            conn.readTimeout = 30_000
            conn.instanceFollowRedirects = true
            var total = info.sizeBytes
            try {
                if (conn.responseCode !in 200..299) throw IOException("Server membalas ${conn.responseCode}")
                if (conn.contentLengthLong > 0) total = conn.contentLengthLong
                conn.inputStream.use { input ->
                    part.outputStream().use { output ->
                        val buf = ByteArray(32 * 1024)
                        var done = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            output.write(buf, 0, n)
                            done += n
                            ensureActive()
                            onProgress(done, total)
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
            if (total > 0 && part.length() != total) throw IOException("Unduhan tidak lengkap, coba lagi")
            if (!part.renameTo(out)) throw IOException("Gagal menyimpan file pembaruan")
            val pkg = ctx.packageManager.getPackageArchiveInfo(out.path, 0)
            if (pkg == null || pkg.packageName != ctx.packageName) {
                out.delete()
                throw IOException("File pembaruan tidak valid")
            }
            out
        }

    fun canInstall(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        return ctx.packageManager.canRequestPackageInstalls()
    }

    fun installIntent(ctx: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /** @return false kalau pengguna masih harus mengizinkan "Pasang aplikasi tak dikenal" dulu. */
    fun install(ctx: Context, file: File): Boolean {
        val app = ctx.applicationContext
        if (!canInstall(app)) {
            app.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${app.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return false
        }
        app.startActivity(installIntent(app, file))
        return true
    }
}

/** Satu-satunya pemegang status update. Unduhan tetap jalan walau layar Pengaturan ditutup. */
object UpdateManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<UpdateUi>(UpdateUi.Idle)
    val state: StateFlow<UpdateUi> = _state.asStateFlow()

    private var job: Job? = null
    private var autoChecked = false

    /** Dipanggil saat aplikasi dibuka: cek diam-diam sekali per proses kalau diizinkan di Pengaturan. */
    fun autoCheckOnce(ctx: Context) {
        if (autoChecked || !Prefs.autoCheck(ctx)) return
        autoChecked = true
        check(ctx, auto = true)
    }

    fun check(ctx: Context, auto: Boolean = false) {
        val app = ctx.applicationContext
        when (_state.value) {
            is UpdateUi.Checking, is UpdateUi.Downloading, is UpdateUi.Ready -> return
            else -> Unit
        }
        _state.value = UpdateUi.Checking
        job = scope.launch {
            val result = withContext(Dispatchers.IO) { UpdateApi.latest() }
            Prefs.setLastCheck(app, System.currentTimeMillis())
            when (result) {
                is UpdateCheck.Available -> {
                    _state.value = UpdateUi.Available(result.info)
                    if (auto && Prefs.lastNotified(app) != result.info.versionName) {
                        Prefs.setLastNotified(app, result.info.versionName)
                        UpdateNotifier.available(app, result.info)
                    }
                }
                is UpdateCheck.UpToDate -> _state.value = UpdateUi.UpToDate
                is UpdateCheck.Failed ->
                    _state.value = if (auto) UpdateUi.Idle else UpdateUi.Error(result.message)
            }
        }
    }

    fun download(ctx: Context) {
        val app = ctx.applicationContext
        val info = when (val s = _state.value) {
            is UpdateUi.Available -> s.info
            is UpdateUi.Error -> s.info ?: return
            else -> return
        }
        job = scope.launch {
            var lastTime = System.nanoTime()
            var lastBytes = 0L
            var lastPct = -1
            _state.value = UpdateUi.Downloading(info, 0L, info.sizeBytes, 0L)
            UpdateNotifier.progress(app, info, 0)
            try {
                val file = UpdateApi.download(app, info) { done, total ->
                    val now = System.nanoTime()
                    if (now - lastTime >= 300_000_000L) {
                        val speed = (done - lastBytes) * 1_000_000_000L / (now - lastTime)
                        lastBytes = done
                        lastTime = now
                        _state.value = UpdateUi.Downloading(info, done, total, speed)
                        val pct = if (total > 0) (done * 100 / total).toInt() else 0
                        if (pct != lastPct) {
                            lastPct = pct
                            UpdateNotifier.progress(app, info, pct)
                        }
                    }
                }
                _state.value = UpdateUi.Ready(info, file)
                UpdateNotifier.ready(app, info, file)
            } catch (e: CancellationException) {
                UpdateNotifier.cancel(app)
                _state.value = UpdateUi.Available(info)
                throw e
            } catch (e: Exception) {
                UpdateNotifier.cancel(app)
                _state.value = UpdateUi.Error("Unduhan gagal: ${e.localizedMessage}", info)
            }
        }
    }

    fun cancelDownload() {
        job?.cancel()
    }

    /** @return false kalau pengguna harus mengizinkan pemasangan dulu. */
    fun install(ctx: Context): Boolean {
        val ready = _state.value as? UpdateUi.Ready ?: return false
        return UpdateApi.install(ctx, ready.file)
    }
}
