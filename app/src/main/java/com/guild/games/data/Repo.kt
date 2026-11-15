package com.guild.games.data

import android.net.Uri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
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

object Repo {
    const val DEV_CODE = "GUILD-DEV-2026"
    private const val WRONG = "Username atau Password salah"
    private const val NO_NET = "Tidak ada koneksi internet"

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    /** @return null jika sukses, atau pesan error */
    suspend fun login(username: String, gmail: String, password: String): String? = try {
        val uid = auth.signInWithEmailAndPassword(gmail.trim(), password).await().user!!.uid
        val doc = db.collection("users").document(uid).get().await()
        if (!doc.exists() || !doc.getString("username").orEmpty().equals(username.trim(), ignoreCase = true)) {
            auth.signOut()
            WRONG
        } else null
    } catch (e: FirebaseNetworkException) {
        NO_NET
    } catch (e: FirebaseAuthInvalidUserException) {
        WRONG
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        WRONG
    } catch (e: Exception) {
        "Terjadi kesalahan: ${e.localizedMessage}"
    }

    suspend fun register(username: String, gmail: String, password: String, role: String): String? = try {
        val user = auth.createUserWithEmailAndPassword(gmail.trim(), password).await().user!!
        saveUser(user.uid, username.trim(), gmail.trim(), role, "")
        null
    } catch (e: FirebaseNetworkException) {
        NO_NET
    } catch (e: FirebaseAuthUserCollisionException) {
        "Gmail sudah terdaftar"
    } catch (e: FirebaseAuthWeakPasswordException) {
        "Password minimal 6 karakter"
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        "Format Gmail tidak valid"
    } catch (e: Exception) {
        "Terjadi kesalahan: ${e.localizedMessage}"
    }

    suspend fun googleLogin(idToken: String): String? = try {
        val user = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user!!
        val doc = db.collection("users").document(user.uid).get().await()
        if (!doc.exists()) {
            saveUser(
                user.uid,
                user.displayName ?: user.email.orEmpty().substringBefore("@"),
                user.email.orEmpty(),
                "anggota",
                user.photoUrl?.toString().orEmpty()
            )
        }
        null
    } catch (e: FirebaseNetworkException) {
        NO_NET
    } catch (e: Exception) {
        "Login Google gagal: ${e.localizedMessage}"
    }

    private suspend fun saveUser(uid: String, username: String, gmail: String, role: String, photoUrl: String) {
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

    suspend fun currentRole(): String = try {
        val uid = auth.currentUser?.uid ?: ""
        db.collection("users").document(uid).get().await().getString("role") ?: "guest"
    } catch (e: Exception) {
        "guest"
    }

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
