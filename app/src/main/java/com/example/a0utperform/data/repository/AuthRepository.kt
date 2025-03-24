package com.example.a0utperform.data.repository

import android.content.Context
import androidx.credentials.CredentialManager


import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.a0utperform.R
import com.example.a0utperform.data.datastore.UserModel
import com.example.a0utperform.data.datastore.UserPreference
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val firestore: FirebaseFirestore,
    private val userPreference: UserPreference
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
                val user = authResult.user ?: return@withContext null

                val userDoc = firestore.collection("users").document(user.uid).get().await()

                if (!userDoc.exists()) {
                    throw Exception("User does not exist. Please register first.")
                }
                else {
                    val userModel = UserModel(
                        userId = userDoc.getString("uid") ?: "",
                        name = userDoc.getString("name") ?: "",
                        email = userDoc.getString("email") ?: "",
                        phone = userDoc.getString("phone") ?: "",
                        isLogin = true
                    )

                    userPreference.saveSession(userModel)
                }

                user
            } catch (e: GetCredentialException) {
                throw Exception("Google Sign-in failed: ${e.message}")
            }
        }
    }



    suspend fun registerUser(email: String, password: String, phone: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            firebaseUser?.let { user ->
                // Create a user profile map to store additional details
                val userProfile = hashMapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "phone" to phone,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users")
                    .document(user.uid)
                    .set(userProfile)
                    .await()
            }
            Result.success(firebaseUser)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Weak password: ${e.reason}"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email already in use"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email format"))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception(e.message ?: "Authentication failed"))
        } catch (e: Exception) {

            Result.failure(e)
        }
    }
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == "ERROR_WRONG_PASSWORD") {
                Result.failure(Exception("Wrong password. Please try again."))
            } else if (e.errorCode == "ERROR_USER_NOT_FOUND") {
                Result.failure(Exception("No account found with this email."))
            } else {
                Result.failure(Exception("Invalid email or password."))
            }
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") {
                Result.failure(Exception("This account is registered via Google. Please use Google sign-in."))
            } else {
                Result.failure(Exception(e.message ?: "Authentication failed"))
            }
        }
    }
    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): UserModel? {
        val uid = auth.currentUser?.uid ?: return null

        val documentSnapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .await()

        return documentSnapshot.toObject(UserModel::class.java)
    }
}