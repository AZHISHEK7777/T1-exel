package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PlayerProfileData(
    val nickname: String,
    val accountId: String,
    val region: String,
    val isVerified: Boolean = true,
    val statusMessage: String = "ACTIVE / VERIFIED",
    val level: Int? = null,
    val exp: Long? = null,
    val likes: Long? = null,
    val bio: String? = null,
    val guildName: String? = null,
    val guildId: String? = null,
    val rankScore: Int? = null,
    val accountCreated: String? = null,
    val accountAge: String? = null,
    val lastLogin: String? = null,
    val gameVersion: String? = null,
    val booyahPass: String? = null,
    val primeLevel: String? = null,
    val dataSource: String = "freefiremania.com.br/free-fire-id-check.html",
    val rawJson: String
)

sealed class UidInspectionResult {
    data object Idle : UidInspectionResult()
    data object Loading : UidInspectionResult()
    data class NoInternet(val message: String = "Internet connection required to inspect player UID.") : UidInspectionResult()
    data class NotConfigured(
        val title: String = "Real data source not configured.",
        val details: String = "Player data unavailable.\n\nConfigure an authorized API endpoint in Backend Settings."
    ) : UidInspectionResult()
    data class Unavailable(
        val title: String = "Player data unavailable.",
        val details: String = "The authorized data source returned no record for this UID in the selected region or the server is unreachable."
    ) : UidInspectionResult()
    data class Success(val profile: PlayerProfileData) : UidInspectionResult()
}

/**
 * Thread-safe in-memory CookieJar to maintain session cookies for REDX Game.
 */
class InMemoryCookieJar : CookieJar {
    private val cookieStore = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(cookieStore) {
            cookieStore.removeAll { existing ->
                cookies.any { it.name == existing.name && it.domain == existing.domain }
            }
            cookieStore.addAll(cookies)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(cookieStore) {
            val now = System.currentTimeMillis()
            cookieStore.removeAll { it.expiresAt < now }
            return cookieStore.filter { it.matches(url) }
        }
    }

    fun clear() {
        synchronized(cookieStore) {
            cookieStore.clear()
        }
    }
}

/**
 * Direct client for REDX Game Free Fire ID Checker (https://redxgame.com/tools/free-fire-id-checker)
 */
class RedxGameIdChecker(private val client: OkHttpClient) {
    private var cachedCsrfToken: String? = null
    private var tokenTimestamp: Long = 0L

    companion object {
        const val CHECKER_URL = "https://redxgame.com/tools/free-fire-id-checker"
        const val CHECK_API_URL = "https://redxgame.com/tools/check"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
    }

    private fun fetchCsrfToken(): String? {
        try {
            val request = Request.Builder()
                .url(CHECKER_URL)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return null

            val regex1 = """name=["']csrf-token["']\s+content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
            val regex2 = """content=["']([^"']+)["']\s+name=["']csrf-token["']""".toRegex(RegexOption.IGNORE_CASE)

            val match = regex1.find(html) ?: regex2.find(html)
            val token = match?.groupValues?.get(1)
            if (!token.isNullOrBlank()) {
                cachedCsrfToken = token
                tokenTimestamp = System.currentTimeMillis()
            }
            return token
        } catch (e: Exception) {
            Log.e("RedxGameIdChecker", "Failed to fetch CSRF token", e)
            return null
        }
    }

    suspend fun checkUid(uid: String): UidInspectionResult = withContext(Dispatchers.IO) {
        var token = cachedCsrfToken
        val isExpired = (System.currentTimeMillis() - tokenTimestamp) > 10 * 60 * 1000 // 10 minutes

        if (token.isNullOrBlank() || isExpired) {
            token = fetchCsrfToken()
        }

        if (token.isNullOrBlank()) {
            return@withContext UidInspectionResult.Unavailable(
                title = "Connection Error",
                details = "Could not establish a secure connection with redxgame.com. Please check your internet and try again."
            )
        }

        var execution = executePost(uid, token)

        // If CSRF token expired on server side (HTTP 419 or 403), re-fetch fresh token and retry once
        if (execution is CheckExecutionResult.RetryNeeded) {
            token = fetchCsrfToken()
            if (!token.isNullOrBlank()) {
                execution = executePost(uid, token)
            }
        }

        return@withContext when (execution) {
            is CheckExecutionResult.Success -> {
                UidInspectionResult.Success(execution.profile)
            }
            is CheckExecutionResult.NotFound -> {
                UidInspectionResult.Unavailable(
                    title = "Player ID Not Found",
                    details = execution.message
                )
            }
            is CheckExecutionResult.Error -> {
                UidInspectionResult.Unavailable(
                    title = "Check Failed",
                    details = execution.message
                )
            }
            is CheckExecutionResult.RetryNeeded -> {
                UidInspectionResult.Unavailable(
                    title = "Session Expired",
                    details = "Session timed out while checking ID with redxgame.com. Please try again."
                )
            }
        }
    }

    private sealed interface CheckExecutionResult {
        data class Success(val profile: PlayerProfileData) : CheckExecutionResult
        data class NotFound(val message: String) : CheckExecutionResult
        data class Error(val message: String) : CheckExecutionResult
        data object RetryNeeded : CheckExecutionResult
    }

    private fun executePost(uid: String, token: String): CheckExecutionResult {
        try {
            val jsonPayload = JSONObject().apply {
                put("game", "free-fire")
                val inputs = JSONObject().apply {
                    put("user_id", uid)
                }
                put("inputs", inputs)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(CHECK_API_URL)
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-CSRF-TOKEN", token)
                .header("Origin", "https://redxgame.com")
                .header("Referer", CHECKER_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.code == 419 || response.code == 403) {
                return CheckExecutionResult.RetryNeeded
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful || bodyString.isNullOrBlank()) {
                return CheckExecutionResult.Error("REDX Game returned HTTP ${response.code}: Service temporarily unavailable.")
            }

            val json = JSONObject(bodyString)
            val status = json.optBoolean("status", false)

            if (status) {
                val nickname = json.optString("nickname", "Unknown")
                val region = json.optString("region", "Global")
                val profile = PlayerProfileData(
                    nickname = nickname,
                    accountId = uid,
                    region = region,
                    isVerified = true,
                    statusMessage = "ACTIVE / VERIFIED",
                    dataSource = "redxgame.com/tools/free-fire-id-checker",
                    rawJson = bodyString
                )
                return CheckExecutionResult.Success(profile)
            } else {
                val message = json.optString("message", "Couldn't verify that ID — please double-check and try again.")
                return CheckExecutionResult.NotFound(message)
            }
        } catch (e: Exception) {
            Log.e("RedxGameIdChecker", "Error executing check for UID $uid", e)
            return CheckExecutionResult.Error(e.message ?: "Failed to connect to REDX Game ID Checker.")
        }
    }
}

/**
 * Direct client for Free Fire Mania ID Check (https://www.freefiremania.com.br/free-fire-id-check.html)
 */
class FreeFireManiaIdChecker(private val client: OkHttpClient) {
    companion object {
        const val BASE_URL = "https://www.freefiremania.com.br"
        const val CHECK_TOOL_URL = "https://www.freefiremania.com.br/free-fire-id-check.html"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
    }

    suspend fun checkUid(uid: String, region: String): UidInspectionResult = withContext(Dispatchers.IO) {
        try {
            val formattedRegion = region.trim().lowercase()
            val url = "$BASE_URL/profile/$uid.html?region=$formattedRegion"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UidInspectionResult.Unavailable(
                    title = "Profile not found on Free Fire Mania",
                    details = "Free Fire Mania profile returned HTTP ${response.code} for UID $uid in region ${region.uppercase()}."
                )
            }

            val html = response.body?.string() ?: return@withContext UidInspectionResult.Unavailable()

            if (!html.contains("perfil-api-grid") && !html.contains("Player Profile of")) {
                return@withContext UidInspectionResult.Unavailable(
                    title = "Profile Not Found",
                    details = "No public record found on Free Fire Mania for UID $uid in region ${region.uppercase()}."
                )
            }

            // Extract fields with Regex
            val nickRegex = """<div><strong>Nick</strong><span>([^<]+)</span></div>""".toRegex(RegexOption.IGNORE_CASE)
            val nick = nickRegex.find(html)?.groupValues?.get(1)?.trim()
                ?: """<h1>Player Profile of ([^<]+) in Free Fire</h1>""".toRegex(RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)?.trim()
                ?: "Free Fire Player"

            val levelRegex = """Level (\d+)""".toRegex()
            val level = levelRegex.find(html)?.groupValues?.get(1)?.toIntOrNull()

            val likesRegex = """<div><strong>Likes</strong><span>([^<]+)</span></div>""".toRegex()
            val likes = likesRegex.find(html)?.groupValues?.get(1)?.trim()

            val expRegex = """<div><strong>Exp</strong><span>([^<]+)</span></div>""".toRegex()
            val exp = expRegex.find(html)?.groupValues?.get(1)?.trim()

            val createdRegex = """<div><strong>Account created on</strong><span>([^<]+)</span></div>""".toRegex()
            val created = createdRegex.find(html)?.groupValues?.get(1)?.trim()

            val ageRegex = """<span class="perfil-chip">([^<]+old)</span>""".toRegex()
            val accountAge = ageRegex.find(html)?.groupValues?.get(1)?.trim()

            val lastLoginRegex = """<div><strong>Last login on</strong><span>([^<]+)</span></div>""".toRegex()
            val lastLogin = lastLoginRegex.find(html)?.groupValues?.get(1)?.trim()

            val gameVersionRegex = """<div><strong>Game version</strong><span>([^<]+)</span></div>""".toRegex()
            val gameVersion = gameVersionRegex.find(html)?.groupValues?.get(1)?.trim()

            val booyahRegex = """<div><strong>Booyah Pass</strong><span>([^<]+)</span></div>""".toRegex()
            val booyahPass = booyahRegex.find(html)?.groupValues?.get(1)?.trim()

            val primeRegex = """title="Account Prime level".*?<span class="perfil-prime-num">(\d+)</span>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val primeLevel = primeRegex.find(html)?.groupValues?.get(1)?.let { "Level $it" }

            val verifiedRegex = """<div><strong>Verified</strong>.*?>(YES|SIM)<""".toRegex(RegexOption.IGNORE_CASE)
            val isVerified = verifiedRegex.containsMatchIn(html)

            val guildRegex = """<h2>Guild</h2>\s*<p>([^<]+)</p>""".toRegex(RegexOption.IGNORE_CASE)
            val guildName = guildRegex.find(html)?.groupValues?.get(1)?.trim()?.takeIf { !it.contains("No guild", ignoreCase = true) }

            val regionRegex = """<div><strong>Region</strong><span>([^<]+)</span></div>""".toRegex()
            val detectedRegion = regionRegex.find(html)?.groupValues?.get(1)?.trim() ?: region.uppercase()

            return@withContext UidInspectionResult.Success(
                PlayerProfileData(
                    nickname = nick,
                    accountId = uid,
                    region = detectedRegion,
                    isVerified = isVerified,
                    statusMessage = if (isVerified) "VERIFIED V-BADGE ATHLETE" else "ACTIVE / CLEAN ACCOUNT",
                    level = level,
                    exp = exp?.replace(",", "")?.replace(".", "")?.toLongOrNull(),
                    likes = likes?.replace(",", "")?.replace(".", "")?.toLongOrNull(),
                    bio = "Verified via Free Fire Mania",
                    guildName = guildName,
                    guildId = null,
                    rankScore = null,
                    accountCreated = created,
                    accountAge = accountAge,
                    lastLogin = lastLogin,
                    gameVersion = gameVersion,
                    booyahPass = booyahPass,
                    primeLevel = primeLevel,
                    dataSource = "freefiremania.com.br/free-fire-id-check.html",
                    rawJson = """{"source":"freefiremania.com.br","uid":"$uid","nickname":"$nick","level":$level,"likes":"$likes","region":"$detectedRegion","created":"$created","age":"$accountAge","lastLogin":"$lastLogin","booyahPass":"$booyahPass","exp":"$exp","verified":$isVerified}"""
                )
            )
        } catch (e: Exception) {
            Log.e("FreeFireManiaChecker", "Error scraping Free Fire Mania", e)
            return@withContext UidInspectionResult.Unavailable(
                title = "Lookup Error",
                details = "Could not fetch profile from freefiremania.com.br: ${e.message}"
            )
        }
    }
}

class UidInspectorRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("t1_uid_inspector_prefs", Context.MODE_PRIVATE)

    private val cookieJar = InMemoryCookieJar()

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val ffmChecker = FreeFireManiaIdChecker(client)
    private val redxChecker = RedxGameIdChecker(client)

    var configuredEndpoint: String
        get() = prefs.getString("custom_api_endpoint", "") ?: ""
        set(value) = prefs.edit().putString("custom_api_endpoint", value.trim()).apply()

    var configuredApiKey: String
        get() = prefs.getString("custom_api_key", "") ?: ""
        set(value) = prefs.edit().putString("custom_api_key", value.trim()).apply()

    /**
     * Inspects a player UID:
     * - If custom API configured, queries that endpoint.
     * - Queries Free Fire Mania (https://www.freefiremania.com.br/free-fire-id-check.html) first.
     * - Falls back to REDX Game if Free Fire Mania does not have public record.
     */
    suspend fun inspectUid(
        uid: String,
        region: String,
        isOnline: Boolean
    ): UidInspectionResult = withContext(Dispatchers.IO) {
        if (!isOnline) {
            return@withContext UidInspectionResult.NoInternet()
        }

        val customEndpoint = configuredEndpoint
        if (customEndpoint.isNotBlank()) {
            return@withContext queryCustomEndpoint(uid, region, customEndpoint)
        }

        // 1. Primary: Free Fire Mania (Official Free Fire ID Check Tool)
        val ffmResult = ffmChecker.checkUid(uid, region)
        if (ffmResult is UidInspectionResult.Success) {
            return@withContext ffmResult
        }

        // 2. Fallback: REDX Game
        val redxResult = redxChecker.checkUid(uid)
        if (redxResult is UidInspectionResult.Success) {
            return@withContext redxResult
        }

        return@withContext ffmResult
    }

    private fun queryCustomEndpoint(
        uid: String,
        region: String,
        endpoint: String
    ): UidInspectionResult {
        try {
            val urlWithParams = if (endpoint.contains("?")) {
                "$endpoint&uid=$uid&region=$region"
            } else {
                "$endpoint?uid=$uid&region=$region"
            }

            val requestBuilder = Request.Builder().url(urlWithParams)
            if (configuredApiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $configuredApiKey")
                requestBuilder.addHeader("X-API-Key", configuredApiKey)
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val bodyString = response.body?.string()

            if (!response.isSuccessful || bodyString.isNullOrBlank()) {
                return UidInspectionResult.Unavailable(
                    details = "Data source returned HTTP ${response.code}: Player data unavailable."
                )
            }

            val json = JSONObject(bodyString)
            val nickname = json.optString("nickname", json.optString("name", json.optString("AccountName", "")))
            if (nickname.isBlank() && !json.has("basicInfo") && !json.has("player")) {
                return UidInspectionResult.Unavailable()
            }

            val basicInfo = json.optJSONObject("basicInfo") ?: json.optJSONObject("player") ?: json
            val finalNickname = basicInfo.optString("nickname", basicInfo.optString("name", "Unknown Player"))
            val finalAccountId = basicInfo.optString("accountId", basicInfo.optString("uid", uid))
            val finalRegion = basicInfo.optString("region", region)
            val level = if (basicInfo.has("level")) basicInfo.optInt("level") else null
            val exp = if (basicInfo.has("exp")) basicInfo.optLong("exp") else null
            val likes = if (basicInfo.has("likes")) basicInfo.optLong("likes") else null
            val bio = basicInfo.optString("signature", basicInfo.optString("bio", "")).takeIf { it.isNotBlank() }

            val guild = json.optJSONObject("guild") ?: json.optJSONObject("clan")
            val guildName = guild?.optString("name", "")?.takeIf { it.isNotBlank() }
            val guildId = guild?.optString("id", "")?.takeIf { it.isNotBlank() }

            val rankScore = if (basicInfo.has("rankingPoints")) basicInfo.optInt("rankingPoints") else null

            return UidInspectionResult.Success(
                PlayerProfileData(
                    nickname = finalNickname,
                    accountId = finalAccountId,
                    region = finalRegion,
                    isVerified = true,
                    statusMessage = "ACTIVE / VERIFIED",
                    level = level,
                    exp = exp,
                    likes = likes,
                    bio = bio,
                    guildName = guildName,
                    guildId = guildId,
                    rankScore = rankScore,
                    dataSource = "Custom Authorized Endpoint",
                    rawJson = bodyString
                )
            )
        } catch (_: Exception) {
            return UidInspectionResult.Unavailable()
        }
    }
}

