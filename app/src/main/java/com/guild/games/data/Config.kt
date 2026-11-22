package com.guild.games.data

import com.guild.games.BuildConfig

/**
 * Nilai diisi saat build dari GitHub Secrets (lihat README), jadi tidak perlu diedit di kode.
 * Kosong = fitur kirim kode belum aktif.
 */
object Config {
    val EMAILJS_SERVICE_ID: String = BuildConfig.EMAILJS_SERVICE_ID
    val EMAILJS_TEMPLATE_ID: String = BuildConfig.EMAILJS_TEMPLATE_ID
    val EMAILJS_PUBLIC_KEY: String = BuildConfig.EMAILJS_PUBLIC_KEY

    /**
     * Kosong  = kode dikirim ke Gmail yang didaftarkan (verifikasi Gmail).
     * Diisi   = kode dikirim ke Gmail pemilik aplikasi (pemilik yang menyetujui developer baru).
     */
    val APPROVER_EMAIL: String = BuildConfig.APPROVER_EMAIL

    /** Repo GitHub tempat rilis APK dipublikasikan (dipakai fitur update di Pengaturan). */
    const val GITHUB_REPO = "FeynJf/Guild-Games"
}
