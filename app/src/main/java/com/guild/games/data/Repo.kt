package com.guild.games.data

import android.net.Uri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Guild(
    val id: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val description: String = "",
    val requirements: String = "",
    val stars: Int = 1,
    val maxMembers: Int = 0,
    val memberCount: Int = 0
)

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val gmail: String = "",
    /** anggota / developer / admin. Kosong = belum bisa dibaca (misalnya offline). */
    val role: String = "",
    val photoUrl: String = "",
    val provider: String = ""
)

object Repo {
    private const val WRONG = "Username atau Password salah"
    private const val NO_NET = "Tidak ada koneksi internet"

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    /**
     * Firestore sering belum tersambung sesaat setelah login ("client is offline"),
     * jadi pembacaan/penulisan dicoba ulang beberapa kali sebelum dianggap gagal.
     */
    private suspend fun <T> retry(times: Int = 4, block: suspend () -> T): T {
        var last: Exception? = null
        for (i in 0 until times) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                last = e
                if (i < times - 1) delay(700L * (i + 1))
            }
        }
        throw last ?: IllegalStateException("Gagal setelah $times percobaan")
    }

    private fun Exception.isOffline() =
        this is FirebaseNetworkException ||
            (this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE) ||
            message?.contains("offline", ignoreCase = true) == true

    private suspend fun userDoc(uid: String): DocumentSnapshot =
        retry { db.collection("users").document(uid).get().await() }

    /** @return null jika sukses, atau pesan error */
    suspend fun login(username: String, gmail: String, password: String): String? = try {
        val uid = auth.signInWithEmailAndPassword(gmail.trim(), password).await().user!!.uid
        val doc = userDoc(uid)
        if (!doc.exists() || !doc.getString("username").orEmpty().equals(username.trim(), ignoreCase = true)) {
            auth.signOut()
            WRONG
        } else null
    } catch (e: FirebaseAuthInvalidUserException) {
        WRONG
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        WRONG
    } catch (e: Exception) {
        // Jangan biarkan setengah login kalau data akun gagal dibaca.
        auth.signOut()
        if (e.isOffline()) NO_NET else "Terjadi kesalahan: ${e.localizedMessage}"
    }

    suspend fun register(username: String, gmail: String, password: String, role: String): String? = try {
        val user = auth.createUserWithEmailAndPassword(gmail.trim(), password).await().user!!
        try {
            saveUser(user.uid, username.trim(), gmail.trim(), role, "")
        } catch (e: Exception) {
            // Batalkan akun setengah jadi (ada di Auth tapi tanpa data profil) supaya bisa daftar ulang.
            try { user.delete().await() } catch (x: Exception) { auth.signOut() }
            throw e
        }
        null
    } catch (e: FirebaseAuthUserCollisionException) {
        "Gmail sudah terdaftar"
    } catch (e: FirebaseAuthWeakPasswordException) {
        "Password minimal 6 karakter"
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        "Format Gmail tidak valid"
    } catch (e: Exception) {
        if (e.isOffline()) NO_NET else "Terjadi kesalahan: ${e.localizedMessage}"
    }

    /**
     * Setelah signInWithCredential berhasil, pengguna SUDAH login. Menyiapkan data profil hanya
     * usaha tambahan: kalau gagal (mis. Firestore belum tersambung) login tetap dianggap sukses
     * dan profil dibuat ulang nanti lewat [loadProfile].
     */
    suspend fun googleLogin(idToken: String): String? {
        val user = try {
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user!!
        } catch (e: Exception) {
            return if (e.isOffline()) NO_NET else "Login Google gagal: ${e.localizedMessage}"
        }
        try {
            val doc = userDoc(user.uid)
            if (!doc.exists()) {
                saveUser(
                    user.uid,
                    user.displayName ?: user.email.orEmpty().substringBefore("@"),
                    user.email.orEmpty(),
                    "anggota",
                    user.photoUrl?.toString().orEmpty()
                )
            }
        } catch (e: Exception) {
            // Diabaikan: login sudah berhasil.
        }
        return null
    }

    private suspend fun saveUser(uid: String, username: String, gmail: String, role: String, photoUrl: String) {
        retry {
            db.collection("users").document(uid).set(
                mapOf(
                    "uid" to uid,
                    "username" to username,
                    "gmail" to gmail,
                    "role" to role,
                    "photoUrl" to photoUrl,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    /** @return null jika belum login. Kalau data Firestore gagal dibaca, data dari akun login dipakai. */
    suspend fun loadProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        val fallbackName = user.displayName ?: user.email.orEmpty().substringBefore("@")
        val base = UserProfile(
            uid = user.uid,
            username = fallbackName,
            gmail = user.email.orEmpty(),
            role = "",
            photoUrl = user.photoUrl?.toString().orEmpty(),
            provider = if (user.providerData.any { it.providerId == "google.com" }) "Google" else "Email & Password"
        )
        return try {
            val doc = userDoc(user.uid)
            if (doc.exists()) {
                val photo = doc.getString("photoUrl").orEmpty()
                base.copy(
                    username = doc.getString("username") ?: fallbackName,
                    gmail = doc.getString("gmail") ?: base.gmail,
                    role = doc.getString("role") ?: "anggota",
                    photoUrl = if (photo.isNotBlank()) photo else base.photoUrl
                )
            } else {
                saveUser(user.uid, fallbackName, base.gmail, "anggota", base.photoUrl)
                base.copy(role = "anggota")
            }
        } catch (e: Exception) {
            base
        }
    }

    suspend fun currentRole(): String = loadProfile()?.role?.ifBlank { "guest" } ?: "guest"

    fun observeGuilds(): Flow<List<Guild>> = callbackFlow {
        val reg = db.collection("guilds")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    trySend(snap.documents.map { d ->
                        Guild(
                            id = d.id,
                            name = d.getString("name").orEmpty(),
                            logoUrl = d.getString("logoUrl").orEmpty(),
                            description = d.getString("description").orEmpty(),
                            requirements = d.getString("requirements").orEmpty(),
                            stars = (d.getLong("stars") ?: 1L).toInt(),
                            maxMembers = (d.getLong("maxMembers") ?: 0L).toInt(),
                            memberCount = (d.getLong("memberCount") ?: 0L).toInt()
                        )
                    })
                }
            }
        awaitClose { reg.remove() }
    }

    suspend fun createGuild(
        name: String, logo: Uri?, description: String, requirements: String,
        stars: Int, maxMembers: Int
    ): String? = try {
        val ref = db.collection("guilds").document()
        var url = ""
        if (logo != null) {
            val sref = storage.reference.child("guilds/${ref.id}.jpg")
            sref.putFile(logo).await()
            url = sref.downloadUrl.await().toString()
        }
        ref.set(
            mapOf(
                "name" to name.trim(),
                "logoUrl" to url,
                "description" to description.trim(),
                "requirements" to requirements.trim(),
                "stars" to stars,
                "maxMembers" to maxMembers,
                "memberCount" to 0,
                "ownerUid" to (auth.currentUser?.uid ?: ""),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        null
    } catch (e: FirebaseNetworkException) {
        NO_NET
    } catch (e: Exception) {
        "Gagal membuat guild: ${e.localizedMessage}"
    }
}
