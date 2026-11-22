package com.guild.games.data

import com.guild.games.BuildConfig

/**
 * Semua nilai EmailJS diisi dari GitHub Secrets saat build, jadi file ini tidak perlu diedit.
 * Secret: EMAILJS_SERVICE_ID, EMAILJS_TEMPLATE_ID, EMAILJS_PUBLIC_KEY, EMAILJS_PRIVATE_KEY, APPROVER_EMAIL (opsional).
 *
 * APPROVER_EMAIL kosong = kode dikirim ke Gmail yang didaftarkan.
 * APPROVER_EMAIL diisi  = kode dikirim ke Gmail pemilik aplikasi (pemilik yang menyetujui developer baru).
 */
object Config {
    val EMAILJS_SERVICE_ID: String = BuildConfig.EMAILJS_SERVICE_ID
    val EMAILJS_TEMPLATE_ID: String = BuildConfig.EMAILJS_TEMPLATE_ID
    val EMAILJS_PUBLIC_KEY: String = BuildConfig.EMAILJS_PUBLIC_KEY
    val APPROVER_EMAIL: String = BuildConfig.APPROVER_EMAIL
}
