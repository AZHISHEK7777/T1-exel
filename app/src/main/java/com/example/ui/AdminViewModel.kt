package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AccessKeyRecord
import com.example.data.auth.AuthRepository
import com.example.data.auth.KeyTier
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Key entity matching Firestore collection 'keys':
 * - value: The actual key string (e.g. "T1-PRO-AB12CD")
 * - type: The tier ("basic", "pro", "admin")
 * - isUsed: Whether this key has already been redeemed
 */
data class FirestoreKey(
    val value: String = "",
    val type: String = "basic", // "basic" | "pro" | "admin"
    val isUsed: Boolean = false,
    val label: String = "",
    val createdAt: String = "",
    val usedBy: String = "",
    val pin: String = ""
)

sealed interface RoleValidationState {
    data object Idle : RoleValidationState
    data object Loading : RoleValidationState
    data class Valid(val key: String, val role: String, val isUsed: Boolean, val message: String) : RoleValidationState
    data class Invalid(val message: String) : RoleValidationState
}

class AdminViewModel(
    application: Application,
    val authRepository: AuthRepository = AuthRepository(application.applicationContext)
) : AndroidViewModel(application) {

    companion object {
        const val KEYS_COLLECTION = "keys"
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            val context = getApplication<Application>().applicationContext
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Firestore initialization error: ${e.message}")
            null
        }
    }

    // Live list of Firestore keys
    private val _keysList = MutableStateFlow<List<FirestoreKey>>(emptyList())
    val keysList: StateFlow<List<FirestoreKey>> = _keysList.asStateFlow()

    // Last generated key state
    private val _lastGeneratedKey = MutableStateFlow<FirestoreKey?>(null)
    val lastGeneratedKey: StateFlow<FirestoreKey?> = _lastGeneratedKey.asStateFlow()

    // Status message (e.g. toast/notification)
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Role validation state
    private val _roleValidationState = MutableStateFlow<RoleValidationState>(RoleValidationState.Idle)
    val roleValidationState: StateFlow<RoleValidationState> = _roleValidationState.asStateFlow()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                fetchKeys()
                listenToKeysCollection()
            } catch (e: Exception) {
                Log.w("AdminViewModel", "Background init: ${e.message}")
            }
        }
    }

    /**
     * Realtime listener on the Firestore 'keys' collection
     */
    private fun listenToKeysCollection() {
        try {
            getFirestore()?.collection(KEYS_COLLECTION)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("AdminViewModel", "Error listening to 'keys': ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val parsed = snapshot.documents.mapNotNull { doc ->
                            try {
                                val value = doc.getString("value") ?: doc.getString("key") ?: doc.id
                                val type = doc.getString("type") ?: doc.getString("tier")?.lowercase(Locale.ROOT) ?: "basic"
                                val isUsed = doc.getBoolean("isUsed") ?: false
                                val label = doc.getString("label") ?: ""
                                val createdAt = doc.getString("createdAt") ?: ""
                                val usedBy = doc.getString("usedBy") ?: ""
                                val pin = doc.getString("pin") ?: ""
                                FirestoreKey(
                                    value = value,
                                    type = type,
                                    isUsed = isUsed,
                                    label = label,
                                    createdAt = createdAt,
                                    usedBy = usedBy,
                                    pin = pin
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _keysList.value = parsed
                    }
                }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Failed to attach listener: ${e.message}")
        }
    }

    fun fetchKeys() {
        viewModelScope.launch {
            try {
                val fs = getFirestore()
                if (fs != null) {
                    val snapshot = fs.collection(KEYS_COLLECTION).get().awaitIfPossible()
                    if (snapshot != null && !snapshot.isEmpty) {
                        val parsed = snapshot.documents.mapNotNull { doc ->
                            val value = doc.getString("value") ?: doc.getString("key") ?: doc.id
                            val type = doc.getString("type") ?: doc.getString("tier")?.lowercase(Locale.ROOT) ?: "basic"
                            val isUsed = doc.getBoolean("isUsed") ?: false
                            val label = doc.getString("label") ?: ""
                            val createdAt = doc.getString("createdAt") ?: ""
                            val usedBy = doc.getString("usedBy") ?: ""
                            val pin = doc.getString("pin") ?: ""
                            FirestoreKey(value, type, isUsed, label, createdAt, usedBy, pin)
                        }
                        _keysList.value = parsed
                    }
                }
            } catch (e: Exception) {
                Log.w("AdminViewModel", "fetchKeys: ${e.message}")
            }
        }
    }

    /**
     * Generate key with fields: 'value', 'type' (basic/pro/admin), 'isUsed'
     * Stores in Firestore collection 'keys' and syncs locally with AuthRepository.
     */
    fun generateKey(type: String, label: String = ""): FirestoreKey {
        val normalizedType = when (type.lowercase(Locale.ROOT)) {
            "pro" -> "pro"
            "admin" -> "admin"
            else -> "basic"
        }

        val random = SecureRandom()
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val suffix = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        val prefix = when (normalizedType) {
            "pro" -> "T1-PRO"
            "admin" -> "T1-ADMIN"
            else -> "T1-BASIC"
        }
        val keyValue = "$prefix-$suffix"
        val timeNow = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val firestoreKey = FirestoreKey(
            value = keyValue,
            type = normalizedType,
            isUsed = false,
            label = label.ifBlank { "${normalizedType.uppercase(Locale.ROOT)} License" },
            createdAt = timeNow,
            usedBy = "",
            pin = ""
        )

        // 1. Save in AuthRepository local cache
        val keyTier = when (normalizedType) {
            "pro" -> KeyTier.PRO
            "admin" -> KeyTier.ADMIN
            else -> KeyTier.BASIC
        }
        authRepository.saveSingleKeyLocally(
            AccessKeyRecord(
                key = firestoreKey.value,
                label = firestoreKey.label,
                tier = keyTier,
                isUsed = false,
                createdAt = firestoreKey.createdAt,
                isRevoked = false,
                usedBy = "",
                usedAt = "",
                pin = ""
            )
        )

        // 2. Upload to Firestore collection 'keys' with requested fields:
        // 'value', 'type' (basic/pro/admin), and 'isUsed'
        viewModelScope.launch {
            try {
                val fs = getFirestore()
                if (fs != null) {
                    val keyData = hashMapOf(
                        "value" to firestoreKey.value,
                        "type" to firestoreKey.type,
                        "isUsed" to firestoreKey.isUsed,
                        "label" to firestoreKey.label,
                        "createdAt" to firestoreKey.createdAt,
                        "usedBy" to firestoreKey.usedBy,
                        "pin" to firestoreKey.pin
                    )
                    fs.collection(KEYS_COLLECTION).document(firestoreKey.value).set(keyData)

                    // Also store duplicate in legacy 'access_keys' for backwards compatibility
                    fs.collection("access_keys").document(firestoreKey.value).set(
                        hashMapOf(
                            "key" to firestoreKey.value,
                            "label" to firestoreKey.label,
                            "tier" to keyTier.name,
                            "isUsed" to false,
                            "isRevoked" to false,
                            "createdAt" to firestoreKey.createdAt,
                            "usedBy" to "",
                            "usedAt" to "",
                            "pin" to ""
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Failed to push key to Firestore: ${e.message}")
            }
        }

        // Update local state flow
        _lastGeneratedKey.value = firestoreKey
        val current = _keysList.value.toMutableList()
        current.add(0, firestoreKey)
        _keysList.value = current
        _statusMessage.value = "Generated [${firestoreKey.type.uppercase(Locale.ROOT)}] Key: ${firestoreKey.value}"

        return firestoreKey
    }

    /**
     * Validates a user's role and key status.
     * Checks Firestore 'keys' collection and local AuthRepository.
     */
    fun validateUserRole(rawKey: String) {
        val cleanKey = rawKey.trim()
        if (cleanKey.isBlank()) {
            _roleValidationState.value = RoleValidationState.Invalid("Key cannot be blank.")
            return
        }

        if (cleanKey == AuthRepository.ADMIN_PASSCODE || cleanKey.equals(AuthRepository.MASTER_KEY, ignoreCase = true)) {
            _roleValidationState.value = RoleValidationState.Valid(
                key = cleanKey,
                role = "admin",
                isUsed = false,
                message = "Verified: Abhishek Master Admin Role"
            )
            return
        }

        _roleValidationState.value = RoleValidationState.Loading

        viewModelScope.launch {
            try {
                val fs = getFirestore()
                if (fs != null) {
                    val doc = fs.collection(KEYS_COLLECTION).document(cleanKey).get().awaitIfPossibleDoc()
                    if (doc != null && doc.exists()) {
                        val roleType = doc.getString("type") ?: "basic"
                        val isUsed = doc.getBoolean("isUsed") ?: false
                        val usedBy = doc.getString("usedBy") ?: ""
                        val msg = if (isUsed) {
                            "Role: ${roleType.uppercase(Locale.ROOT)} (ALREADY REDEEMED by '$usedBy')"
                        } else {
                            "Role: ${roleType.uppercase(Locale.ROOT)} (ACTIVE & READY FOR USE)"
                        }

                        _roleValidationState.value = RoleValidationState.Valid(
                            key = cleanKey,
                            role = roleType,
                            isUsed = isUsed,
                            message = msg
                        )
                        return@launch
                    }
                }

                // Fallback to local keys repository check
                val localKey = authRepository.getAllKeys().find { it.key.equals(cleanKey, ignoreCase = true) }
                if (localKey != null) {
                    val roleType = localKey.tier.name.lowercase(Locale.ROOT)
                    val msg = if (localKey.isUsed) {
                        "Role: ${roleType.uppercase(Locale.ROOT)} (ALREADY REDEEMED by '${localKey.usedBy}')"
                    } else {
                        "Role: ${roleType.uppercase(Locale.ROOT)} (ACTIVE & READY FOR USE)"
                    }
                    _roleValidationState.value = RoleValidationState.Valid(
                        key = localKey.key,
                        role = roleType,
                        isUsed = localKey.isUsed,
                        message = msg
                    )
                } else {
                    _roleValidationState.value = RoleValidationState.Invalid("Key '$cleanKey' not found in database.")
                }
            } catch (e: Exception) {
                _roleValidationState.value = RoleValidationState.Invalid("Validation error: ${e.message}")
            }
        }
    }

    fun deleteKey(key: String) {
        authRepository.deleteKey(key)
        _keysList.value = _keysList.value.filterNot { it.value.equals(key, ignoreCase = true) }
        viewModelScope.launch {
            try {
                getFirestore()?.collection(KEYS_COLLECTION)?.document(key)?.delete()
            } catch (_: Exception) {}
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearValidationState() {
        _roleValidationState.value = RoleValidationState.Idle
    }
}

// Extension helper to avoid blocking coroutines if tasks are async
private suspend fun com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>.awaitIfPossible(): com.google.firebase.firestore.QuerySnapshot? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) {} }
        addOnFailureListener { continuation.resume(null) {} }
    }
}

private suspend fun com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot>.awaitIfPossibleDoc(): com.google.firebase.firestore.DocumentSnapshot? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) {} }
        addOnFailureListener { continuation.resume(null) {} }
    }
}
