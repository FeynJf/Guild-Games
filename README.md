# Guild Games (com.guild.games)

Kotlin + Jetpack Compose, Firebase Auth + Firestore + Storage. Build APK lewat GitHub Actions.

## 1. Setup Firebase
1. Buat project di Firebase Console, tambah app Android dengan package `com.guild.games`.
2. Aktifkan **Authentication**: Email/Password dan Google.
3. Aktifkan **Firestore** dan **Storage**. Tempel isi `firebase/firestore.rules` dan `firebase/storage.rules` ke tab Rules masing-masing.
4. Tambahkan **SHA-1** keystore (langkah 2) di Project Settings > app Android, lalu unduh ulang `google-services.json`.

## 2. Buat keystore (sekali saja, supaya Google Login stabil)
```
keytool -genkeypair -v -keystore guild.jks -alias guild -keyalg RSA -keysize 2048 -validity 10000
keytool -list -v -keystore guild.jks -alias guild      # ambil SHA-1
base64 -w0 guild.jks                                   # isi KEYSTORE_BASE64
```

## 3. GitHub Secrets (Settings > Secrets and variables > Actions)
| Secret | Isi |
|---|---|
| `GOOGLE_SERVICES_JSON` | isi lengkap file google-services.json |
| `KEYSTORE_BASE64` | hasil base64 guild.jks |
| `KEYSTORE_PASSWORD` | password keystore |
| `KEY_ALIAS` | `guild` |
| `KEY_PASSWORD` | password key |

Tanpa `KEYSTORE_BASE64`, workflow membuild APK debug (Google Login tidak akan jalan karena SHA-1 berubah tiap build).

## 4. Build
Push ke branch `main` atau jalankan manual di tab Actions. APK ada di **Artifacts > GuildGames-APK**.

## Catatan
- Role `admin` diatur manual di Firestore (`users/{uid}.role`).
- Kode Developer dicek di sisi aplikasi, jadi bisa dibongkar dari APK. Untuk keamanan nyata, pindahkan ke Cloud Function.

## Update 1.2 - Kode Developer via GitHub Secrets, Profil, Pengaturan, Update dalam aplikasi
**Kode Developer / "Layanan email belum diatur"**: kunci EmailJS sekarang dibaca saat build dari GitHub Secrets (tidak perlu edit `Config.kt`). Tambahkan di Settings > Secrets and variables > Actions:

| Secret | Isi |
| ------ | --- |
| `EMAILJS_SERVICE_ID` | Service ID dari EmailJS (Email Services) |
| `EMAILJS_TEMPLATE_ID` | Template ID (kolom *To Email* = `{{to_email}}`, isi memuat `{{code}}`) |
| `EMAILJS_PUBLIC_KEY` | Public Key (Account > General) |
| `APPROVER_EMAIL` | Opsional. Isi Gmail kamu supaya kode developer selalu dikirim ke kamu |

Di EmailJS aktifkan juga Account > Security > *Allow EmailJS API for non-browser applications*.

**Update lewat aplikasi**: tiap push ke `main`, workflow membuat GitHub Release `v<versionName>` berisi APK dan catatan dari `RELEASE_NOTES.md`. Aplikasi (Pengaturan > Pembaruan Aplikasi) membandingkan rilis terbaru dengan versi terpasang. Untuk merilis update berikutnya: naikkan `versionCode` dan `versionName` di `app/build.gradle.kts`, ubah `RELEASE_NOTES.md`, lalu push. APK harus selalu ditandatangani keystore yang sama, kalau tidak Android menolak memasangnya sebagai update.
