package com.example.a0utperform.dashboard

import android.content.Context
import androidx.credentials.CredentialManager


import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.a0utperform.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Weak password: ${e.reason}"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email already in use"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email format"))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception(e.message ?: "Authentication failed"))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid credentials"))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception(e.message ?: "Authentication failed"))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}