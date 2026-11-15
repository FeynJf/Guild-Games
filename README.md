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
