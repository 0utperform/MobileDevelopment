package com.example.a0utperform.decidelogin

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager


import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.a0utperform.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) {
    suspend fun signInWithGoogle(context: Context): FirebaseUser? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val result = credentialManager.getCredential(context, request)
                val googleToken = (result.credential as? GoogleIdTokenCredential)?.idToken
                    ?: throw Exception("Google ID token not found")
                val credential = GoogleAuthProvider.getCredential(googleToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                authResult.user
            } catch (e: GetCredentialException) {
                throw Exception("Google Sign-in failed: ${e.message}")
            }
        }
    }

    fun signInWithFacebook(
        activity: Activity,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (String) -> Unit
    ) {
        LoginManager.getInstance().logInWithReadPermissions(
            activity, listOf("email", "public_profile")
        )

        LoginManager.getInstance().registerCallback(
            CallbackManager.Factory.create(),
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { onSuccess(it.user) }
                        .addOnFailureListener { onError(it.message ?: "Facebook login failed") }
                }

                override fun onCancel() = onError("Facebook login canceled")
                override fun onError(error: FacebookException) = onError(error.message ?: "Error")
            }
        )
    }
}