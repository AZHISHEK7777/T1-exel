package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KeyTier {
    BASIC,
    PRO,
    ADMIN
}

data class AccessKeyRecord(
    val key: String,
    val label: String,
    val tier: KeyTier = KeyTier.BASIC,
    val isUsed: Boolean = false,
    val createdAt: String = "",
    val isRevoked: Boolean = false,
    val usedBy: String = "",
    val usedAt: String = "",
    val pin: String = "" // 4-digit PIN set by the user upon first activation
)

sealed interface KeyValidationResult {
    data object AdminPanel : KeyValidationResult
    data class RequirePinSetup(val key: String, val userName: String, val tier: KeyTier) : KeyValidationResult
    data class Success(val key: String, val userName: String, val tier: KeyTier, val message: String) : KeyValidationResult
    data class Error(val message: String) : KeyValidationResult
}

class AuthRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("t1_auth_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val ADMIN_PASSCODE = "111"
        const val MASTER_KEY = "ABHISHEK-ADMIN-999"
        private const val PREF_IS_LOGGED_IN = "pref_is_logged_in"
        private const val PREF_CURRENT_KEY = "pref_current_key"
        private const val PREF_USER_NAME = "pref_user_name"
        private const val PREF_USER_TIER = "pref_user_tier"
        private const val PREF_USER_PIN = "pref_user_pin"
        private const val PREF_STORED_KEYS = "pref_stored_keys_json"
        private const val PREF_LAST_ADMIN_NOTIFICATION = "pref_last_admin_notification"
    }

    private var isFirebaseAvailable: Boolean? = null

    private fun getFirestore(): FirebaseFirestore? {
        if (isFirebaseAvailable == false) return null
        return try {
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            val apps = FirebaseApp.getApps(context)
            if (resId == 0 && apps.isEmpty()) {
                isFirebaseAvailable = false
                return null
            }
            val app = if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                apps[0]
            }
            if (app != null) {
                isFirebaseAvailable = true
                FirebaseFirestore.getInstance(app)
            } else {
                isFirebaseAvailable = false
                null
            }
        } catch (_: Exception) {
            isFirebaseAvailable = false
            null
        }
    }

    init {
        // Initialize default seed keys if empty
        if (!prefs.contains(PREF_STORED_KEYS)) {
            val initialList = listOf(
                AccessKeyRecord(
                    key = "T1-BASIC-101",
                    label = "Trial Player",
                    tier = KeyTier.BASIC,
                    createdAt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                ),
                AccessKeyRecord(
                    key = "T1-PRO-555",
                    label = "VIP Tournament Player",
                    tier = KeyTier.PRO,
                    createdAt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                ),
                AccessKeyRecord(
                    key = MASTER_KEY,
                    label = "Developer Master Abhishek",
                    tier = KeyTier.ADMIN,
                    createdAt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                )
            )
            saveKeysToPrefs(initialList)
            syncAllLocalKeysToFirestore(initialList)
        } else {
            // Listen for Firestore keys updates
            setupFirestoreKeysListener()
        }
    }

    private fun setupFirestoreKeysListener() {
        try {
            getFirestore()?.collection("access_keys")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("AuthRepository", "Firestore keys listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val cloudKeys = snapshot.documents.mapNotNull { doc ->
                            try {
                                val key = doc.getString("key") ?: return@mapNotNull null
                                val label = doc.getString("label") ?: "VIP Key"
                                val tierStr = doc.getString("tier") ?: "BASIC"
                                val tier = try { KeyTier.valueOf(tierStr) } catch (_: Exception) { KeyTier.BASIC }
                                val isUsed = doc.getBoolean("isUsed") ?: false
                                val isRevoked = doc.getBoolean("isRevoked") ?: false
                                val createdAt = doc.getString("createdAt") ?: ""
                                val usedBy = doc.getString("usedBy") ?: ""
                                val usedAt = doc.getString("usedAt") ?: ""
                                val pin = doc.getString("pin") ?: ""
                                AccessKeyRecord(
                                    key = key,
                                    label = label,
                                    tier = tier,
                                    isUsed = isUsed,
                                    createdAt = createdAt,
                                    isRevoked = isRevoked,
                                    usedBy = usedBy,
                                    usedAt = usedAt,
                                    pin = pin
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (cloudKeys.isNotEmpty()) {
                            saveKeysToPrefs(cloudKeys)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to setup firestore key listener: ${e.message}")
        }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(PREF_IS_LOGGED_IN, false)
    }

    fun getCurrentKey(): String {
        return prefs.getString(PREF_CURRENT_KEY, "") ?: ""
    }

    fun getUserName(): String {
        val stored = prefs.getString(PREF_USER_NAME, "") ?: ""
        return if (stored.isNotBlank()) stored else "Pro Player"
    }

    fun getUserTier(): KeyTier {
        val tierStr = prefs.getString(PREF_USER_TIER, KeyTier.BASIC.name) ?: KeyTier.BASIC.name
        return try { KeyTier.valueOf(tierStr) } catch (_: Exception) { KeyTier.BASIC }
    }

    fun getUserPin(): String {
        return prefs.getString(PREF_USER_PIN, "") ?: ""
    }

    fun setLoggedIn(key: String, userName: String, tier: KeyTier, pin: String = "") {
        val cleanName = userName.trim().ifBlank { "Pro Player" }
        prefs.edit()
            .putBoolean(PREF_IS_LOGGED_IN, true)
            .putString(PREF_CURRENT_KEY, key)
            .putString(PREF_USER_NAME, cleanName)
            .putString(PREF_USER_TIER, tier.name)
            .putString(PREF_USER_PIN, pin)
            .apply()
    }

    fun getLastAdminNotification(): String? {
        return prefs.getString(PREF_LAST_ADMIN_NOTIFICATION, null)
    }

    fun clearAdminNotification() {
        prefs.edit().remove(PREF_LAST_ADMIN_NOTIFICATION).apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(PREF_IS_LOGGED_IN, false)
            .putString(PREF_CURRENT_KEY, "")
            .apply()
    }

    /**
     * Validate Key with Single-Use policy:
     * - If key not found -> Error
     * - If key revoked -> Error
     * - If key already used -> Error: Key already activated once! Use 4-digit PIN login.
     * - If key is valid and not used -> prompt for 4-digit PIN creation
     */
    fun validateKey(rawInput: String, userName: String): KeyValidationResult {
        val cleanKey = rawInput.trim()
        val cleanName = userName.trim()

        if (cleanKey == ADMIN_PASSCODE) {
            return KeyValidationResult.AdminPanel
        }

        if (cleanName.isBlank()) {
            return KeyValidationResult.Error("Name is compulsory! Please enter your name.")
        }

        if (cleanKey.isBlank()) {
            return KeyValidationResult.Error("Please enter a valid Access Key.")
        }

        val allKeys = getAllKeys()
        val found = allKeys.find { it.key.equals(cleanKey, ignoreCase = true) }

        if (found != null) {
            if (found.isRevoked) {
                return KeyValidationResult.Error("This key has been REVOKED by Admin Abhishek.")
            }

            // Single Use Enforcement: Key cannot be used twice!
            if (found.isUsed) {
                return KeyValidationResult.Error(
                    "This key was already used by '${found.usedBy}'! Each key can only be used once. Please use your 4-digit PIN to login."
                )
            }

            // Valid & unused key -> Prompt user to set their 4-digit PIN
            return KeyValidationResult.RequirePinSetup(
                key = found.key,
                userName = cleanName,
                tier = found.tier
            )
        }

        return KeyValidationResult.Error("Invalid Access Key! Please check your key or contact the administrator.")
    }

    /**
     * Finalize key activation with the chosen 4-digit PIN.
     * Marks the key as USED in Firestore & local database permanently.
     */
    fun activateKeyWithPin(key: String, userName: String, tier: KeyTier, pin: String): Boolean {
        val cleanName = userName.trim().ifBlank { "Pro Player" }
        val cleanPin = pin.trim()
        if (cleanPin.length != 4 || !cleanPin.all { it.isDigit() }) {
            return false
        }

        val timestamp = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        val allKeys = getAllKeys()
        val updated = allKeys.map {
            if (it.key.equals(key, ignoreCase = true)) {
                it.copy(
                    isUsed = true,
                    usedBy = cleanName,
                    usedAt = timestamp,
                    pin = cleanPin
                )
            } else {
                it
            }
        }
        saveKeysToPrefs(updated)

        // Save session locally
        setLoggedIn(key, cleanName, tier, cleanPin)

        // Push to Firebase Firestore so no other device can ever reuse this key!
        scope.launch {
            try {
                val fs = getFirestore() ?: return@launch
                val keyData = hashMapOf(
                    "key" to key,
                    "label" to cleanName,
                    "tier" to tier.name,
                    "isUsed" to true,
                    "isRevoked" to false,
                    "createdAt" to timestamp,
                    "usedBy" to cleanName,
                    "usedAt" to timestamp,
                    "pin" to cleanPin
                )
                fs.collection("access_keys").document(key).set(keyData)

                // Also update the 'keys' collection with fields: value, type, isUsed
                val keysCollectionData = hashMapOf(
                    "value" to key,
                    "type" to tier.name.lowercase(Locale.ROOT),
                    "isUsed" to true,
                    "usedBy" to cleanName,
                    "pin" to cleanPin
                )
                fs.collection("keys").document(key).set(keysCollectionData)

                // Push admin alert
                val alert = hashMapOf(
                    "key" to key,
                    "tier" to tier.name,
                    "userName" to cleanName,
                    "timestamp" to timestamp,
                    "timeMillis" to System.currentTimeMillis()
                )
                fs.collection("admin_alerts").add(alert)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to sync key activation to Firestore: ${e.message}")
            }
        }

        // Set local admin notification
        val notification = "🔔 NEW: [$tier] Key '$key' activated by '$cleanName' (PIN set)"
        prefs.edit().putString(PREF_LAST_ADMIN_NOTIFICATION, notification).apply()

        return true
    }

    /**
     * Login using registered Name / Key and 4-digit PIN (for returning users)
     */
    fun loginWithPin(identifier: String, pin: String): KeyValidationResult {
        val cleanId = identifier.trim()
        val cleanPin = pin.trim()

        if (cleanId.isBlank()) {
            return KeyValidationResult.Error("Please enter your registered Name or Key.")
        }
        if (cleanPin.length != 4 || !cleanPin.all { it.isDigit() }) {
            return KeyValidationResult.Error("PIN must be exactly 4 digits.")
        }

        val allKeys = getAllKeys()
        val matchedKey = allKeys.find {
            (it.key.equals(cleanId, ignoreCase = true) || it.usedBy.equals(cleanId, ignoreCase = true)) &&
                    it.pin == cleanPin
        }

        if (matchedKey != null) {
            if (matchedKey.isRevoked) {
                return KeyValidationResult.Error("Your account/key has been REVOKED by Admin Abhishek.")
            }
            setLoggedIn(matchedKey.key, matchedKey.usedBy.ifBlank { cleanId }, matchedKey.tier, matchedKey.pin)
            return KeyValidationResult.Success(
                key = matchedKey.key,
                userName = matchedKey.usedBy.ifBlank { cleanId },
                tier = matchedKey.tier,
                message = "Welcome back, ${matchedKey.usedBy}!"
            )
        }

        return KeyValidationResult.Error("Incorrect Name/Key or 4-digit PIN. Please check and retry.")
    }

    /**
     * Generate new Key in Firestore and local prefs with specific Tier
     * BASIC, PRO, or ADMIN
     */
    fun generateNewKey(label: String = "VIP Client", tier: KeyTier = KeyTier.BASIC): AccessKeyRecord {
        val random = SecureRandom()
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        val tierPrefix = when (tier) {
            KeyTier.BASIC -> "T1-BASIC"
            KeyTier.PRO -> "T1-PRO"
            KeyTier.ADMIN -> "T1-ADMIN"
        }
        val newKey = "$tierPrefix-$part"

        val record = AccessKeyRecord(
            key = newKey,
            label = label.ifBlank { "${tier.name} License" },
            tier = tier,
            isUsed = false,
            createdAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
            isRevoked = false,
            pin = ""
        )

        val keys = getAllKeys().toMutableList()
        keys.add(0, record)
        saveKeysToPrefs(keys)

        // Push directly to Firebase Firestore
        scope.launch {
            try {
                val fs = getFirestore() ?: return@launch
                val map = hashMapOf(
                    "key" to record.key,
                    "label" to record.label,
                    "tier" to record.tier.name,
                    "isUsed" to false,
                    "isRevoked" to false,
                    "createdAt" to record.createdAt,
                    "usedBy" to "",
                    "usedAt" to "",
                    "pin" to ""
                )
                fs.collection("access_keys").document(record.key).set(map)

                // Also store in 'keys' collection with fields: value, type (basic/pro/admin), isUsed
                val keysMap = hashMapOf(
                    "value" to record.key,
                    "type" to record.tier.name.lowercase(Locale.ROOT),
                    "isUsed" to false,
                    "label" to record.label,
                    "createdAt" to record.createdAt,
                    "usedBy" to "",
                    "pin" to ""
                )
                fs.collection("keys").document(record.key).set(keysMap)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to upload new key to Firestore: ${e.message}")
            }
        }

        return record
    }

    fun saveSingleKeyLocally(record: AccessKeyRecord) {
        val keys = getAllKeys().toMutableList()
        val index = keys.indexOfFirst { it.key.equals(record.key, ignoreCase = true) }
        if (index >= 0) {
            keys[index] = record
        } else {
            keys.add(0, record)
        }
        saveKeysToPrefs(keys)
    }

    fun revokeKey(key: String) {
        val keys = getAllKeys().map {
            if (it.key.equals(key, ignoreCase = true)) {
                it.copy(isRevoked = true)
            } else {
                it
            }
        }
        saveKeysToPrefs(keys)
        scope.launch {
            try {
                getFirestore()?.collection("access_keys")?.document(key)
                    ?.update("isRevoked", true)
            } catch (_: Exception) {}
        }
    }

    fun deleteKey(key: String) {
        val keys = getAllKeys().filterNot { it.key.equals(key, ignoreCase = true) }
        saveKeysToPrefs(keys)
        scope.launch {
            try {
                getFirestore()?.collection("access_keys")?.document(key)?.delete()
            } catch (_: Exception) {}
        }
    }

    fun getAllKeys(): List<AccessKeyRecord> {
        val rawJson = prefs.getString(PREF_STORED_KEYS, null) ?: return emptyList()
        val list = mutableListOf<AccessKeyRecord>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val tierStr = obj.optString("tier", "BASIC")
                val tier = try { KeyTier.valueOf(tierStr) } catch (_: Exception) { KeyTier.BASIC }
                list.add(
                    AccessKeyRecord(
                        key = obj.optString("key"),
                        label = obj.optString("label"),
                        tier = tier,
                        isUsed = obj.optBoolean("isUsed", false),
                        createdAt = obj.optString("createdAt"),
                        isRevoked = obj.optBoolean("isRevoked", false),
                        usedBy = obj.optString("usedBy", ""),
                        usedAt = obj.optString("usedAt", ""),
                        pin = obj.optString("pin", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveKeysToPrefs(keys: List<AccessKeyRecord>) {
        val array = JSONArray()
        for (k in keys) {
            val obj = JSONObject().apply {
                put("key", k.key)
                put("label", k.label)
                put("tier", k.tier.name)
                put("isUsed", k.isUsed)
                put("createdAt", k.createdAt)
                put("isRevoked", k.isRevoked)
                put("usedBy", k.usedBy)
                put("usedAt", k.usedAt)
                put("pin", k.pin)
            }
            array.put(obj)
        }
        prefs.edit().putString(PREF_STORED_KEYS, array.toString()).apply()
    }

    private fun syncAllLocalKeysToFirestore(keys: List<AccessKeyRecord>) {
        scope.launch {
            try {
                val fs = getFirestore() ?: return@launch
                for (record in keys) {
                    val map = hashMapOf(
                        "key" to record.key,
                        "label" to record.label,
                        "tier" to record.tier.name,
                        "isUsed" to record.isUsed,
                        "isRevoked" to record.isRevoked,
                        "createdAt" to record.createdAt,
                        "usedBy" to record.usedBy,
                        "usedAt" to record.usedAt,
                        "pin" to record.pin
                    )
                    fs.collection("access_keys").document(record.key).set(map)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed initial keys sync: ${e.message}")
            }
        }
    }
}

