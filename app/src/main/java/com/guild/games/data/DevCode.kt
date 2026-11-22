package com.guild.games.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom

/** Kode developer: dibuat acak oleh aplikasi, dikirim lewat email, berlaku 10 menit, maksimal 5 percobaan. */
object DevCode {
    private const val VALID_MS = 10 * 60 * 1000L
    private const val MAX_TRIES = 5

    private var code: String? = null
    private var email = ""
    private var issuedAt = 0L
    private var tries = 0

    fun isConfigured() = Config.EMAILJS_SERVICE_ID.isNotBlank() &&
        Config.EMAILJS_TEMPLATE_ID.isNotBlank() && Config.EMAILJS_PUBLIC_KEY.isNotBlank()

    /** @return null jika sukses, atau pesan error */
    suspend fun request(gmail: String): String? {
        if (!isConfigured()) return "Layanan email belum diatur. Pemilik aplikasi perlu mengisi EmailJS di GitHub Secrets (lihat README)."
        val newCode = String.format("%06d", SecureRandom().nextInt(1_000_000))
        val target = Config.APPROVER_EMAIL.ifBlank { gmail.trim() }
        val err = withContext(Dispatchers.IO) { send(target, newCode, gmail.trim()) }
        if (err == null) {
            code = newCode
            email = gmail.trim().lowercase()
            issuedAt = System.currentTimeMillis()
            tries = 0
        }
        return err
    }

    /** @return null jika kode benar, atau pesan error */
    fun verify(gmail: String, input: String): String? {
        val c = code ?: return "Minta kode dulu lewat tombol Kirim Kode"
        if (gmail.trim().lowercase() != email) return "Gmail berbeda dari yang meminta kode"
        if (System.currentTimeMillis() - issuedAt > VALID_MS) {
            code = null
            return "Kode kedaluwarsa, minta kode baru"
        }
        tries++
        if (tries > MAX_TRIES) {
            code = null
            return "Terlalu banyak percobaan, minta kode baru"
        }
        if (input.trim() != c) return "Kode Developer Salah"
        code = null
        return null
    }

    private fun send(to: String, code: String, requester: String): String? = try {
        val body = JSONObject().apply {
            put("service_id", Config.EMAILJS_SERVICE_ID)
            put("template_id", Config.EMAILJS_TEMPLATE_ID)
            put("user_id", Config.EMAILJS_PUBLIC_KEY)
            put("template_params", JSONObject().apply {
                put("to_email", to)
                put("code", code)
                put("requester", requester)
            })
        }.toString()
        val conn = (URL("https://api.emailjs.com/api/v1.0/email/send").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Content-Type", "application/json")
        }
        conn.outputStream.use { it.write(body.toByteArray()) }
        val status = conn.responseCode
        if (status in 200..299) null
        else {
            val detail = conn.errorStream?.bufferedReader()?.readText().orEmpty().take(160)
            when (status) {
                403 -> "EmailJS menolak (403). Aktifkan izin API untuk aplikasi non-browser di EmailJS > Account > Security."
                400 -> "EmailJS menolak (400): $detail"
                else -> "Gagal kirim email ($status): $detail"
            }
        }
    } catch (e: Exception) {
        "Tidak bisa mengirim email: ${e.localizedMessage}"
    }
}
