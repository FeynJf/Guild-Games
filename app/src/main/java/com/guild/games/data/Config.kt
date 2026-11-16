package com.guild.games.data

/** Isi dari akun EmailJS (emailjs.com). Kosong = fitur kirim kode belum aktif. */
object Config {
    const val EMAILJS_SERVICE_ID = ""
    const val EMAILJS_TEMPLATE_ID = ""
    const val EMAILJS_PUBLIC_KEY = ""

    /**
     * Kosong  = kode dikirim ke Gmail yang didaftarkan (verifikasi Gmail).
     * Diisi   = kode dikirim ke Gmail pemilik aplikasi (pemilik yang menyetujui developer baru).
     */
    const val APPROVER_EMAIL = ""
}
