package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GoogleUserData(
    val idToken: String?,
    val email: String,
    val displayName: String,
    val photoUrl: String?
)

object GoogleSignInHelper {
    // Standard default OAuth Web Client ID for Google Credential Manager
    // Can also be updated dynamically from Firebase or app settings
    const val DEFAULT_WEB_CLIENT_ID = "950693135305-ff0lsl5vjh76o43g7m7b4f5khn4h8fl3.apps.googleusercontent.com"

    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = DEFAULT_WEB_CLIENT_ID
    ): Result<GoogleUserData> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val name = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                Result.success(
                    GoogleUserData(
                        idToken = googleIdTokenCredential.idToken,
                        email = email,
                        displayName = name,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                    )
                )
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialException) {
            Log.w("GoogleSignInHelper", "Google Credential Manager: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Unexpected sign in exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
