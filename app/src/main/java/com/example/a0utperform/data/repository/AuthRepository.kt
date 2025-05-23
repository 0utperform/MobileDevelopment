package com.example.a0utperform.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.local.user.UserPreference
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseAuth: Auth,
    private val supabaseDatabase: Postgrest,
    private val userPreference: UserPreference
) {
    suspend fun signInWithGoogleIdToken(context: Context): Result<UserModel> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val rawNonce = UUID.randomUUID().toString()
            val digest = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
            val hashedNonce = digest.joinToString("") { "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("1053351222036-ekud8l21pj6ss7rhdk930h48595dhqo7.apps.googleusercontent.com")
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context,request)

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = googleIdTokenCredential.idToken
            val payload = decodeGoogleIdToken(googleIdToken)
            val email = payload?.get("email") as? String
                ?: return Result.failure(Exception("Failed to extract email from Google token"))

            val existingUsers = supabaseDatabase
                .from("users")
                .select(Columns.list())
                .decodeList<UserModel>()
            val cleanEmail = email.trim().lowercase()
            val existingUser = existingUsers.find { it.email.trim().lowercase() == cleanEmail }

            if (existingUser == null) {
                return Result.failure(Exception("Account not registered. Please log in using email first and link Google."))
            }

            else {
                supabaseAuth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }
            }

            val session = supabaseAuth.currentSessionOrNull()
                ?: return Result.failure(Exception("Session is null"))
            val user = session.user ?: return Result.failure(Exception("User is null"))


            val userData = storeUserDataIfNotExists(user)

            supabaseDatabase.from("users").update(
                mapOf("isLogin" to true)
            ) {
                filter {
                    eq("user_id", user.id)
                }
            }

            val userModel = UserModel(
                userId = user.id,
                age = userData.age,
                email = user.email.orEmpty(),
                name = userData.name,
                phone = userData.phone,
                role = userData.role,
                isLogin = true,
                createdAt = userData.createdAt
            )

            userPreference.saveSession(userModel)
            Result.success(userModel)
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Google Credential error: ${e.message}"))
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(Exception("Google ID Token parse error: ${e.message}"))
        } catch (e: RestException) {
            Result.failure(Exception("Supabase auth error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unknown error during Google sign-in: ${e.message}"))
        }

    }
    fun decodeGoogleIdToken(idToken: String): JSONObject? {
        return try {
            val parts = idToken.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val json = String(decodedBytes, Charsets.UTF_8)
            JSONObject(json)
        } catch (e: Exception) {
            null
        }
    }
    suspend fun registerUser(name: String, age: String, email: String, password: String, phone: String): Result<UserModel> {
        return try {
            val firstName = name.split(" ").firstOrNull() ?: name

            val response = supabaseAuth.signUpWith(Email) {
                this.email = email
                this.password = password

                this.data = buildJsonObject {
                    put("display_name", JsonPrimitive(firstName))
                    put("full_name", JsonPrimitive(name))
                    put("phone", JsonPrimitive(phone))
                    put("age", JsonPrimitive(age))
                }
            }

            return Result.failure(Exception("Registration successful. Please verify your email "))
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<UserModel> {
        return try {
            val response = supabaseAuth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val session = supabaseAuth.currentSessionOrNull()
                ?: return Result.failure(Exception("Session is null"))
            val user = session.user
                ?: return Result.failure(Exception("User not found, please register first!"))

            val userData = storeUserDataIfNotExists(user)

            supabaseDatabase.from("users").update(
                mapOf("isLogin" to true)
            ) {
                filter {
                    eq("user_id", user.id)
                }
            }


            val userModel = UserModel(
                user.id,
                userData.name,
                userData.age,
                email,
                userData.role,
                userData.phone,
                isLogin = true,
                createdAt = userData.createdAt)
            userPreference.saveSession(userModel)

            Result.success(userModel)
        } catch (e: Exception) {
            Log.e("LoginError", "Login failed", e)
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    suspend fun signOut() {
        val user = supabaseAuth.currentUserOrNull() ?: return
        supabaseDatabase.from("users").update(
            mapOf("isLogin" to false)
        ) {
            filter {
                eq("user_id", user.id)
            }
        }
        supabaseAuth.signOut()
        userPreference.logout()
    }

    fun getCurrentUser(): Flow<UserModel> {
        return userPreference.getSession()
    }

    private suspend fun storeUserDataIfNotExists(user: UserInfo): UserModel {
        val existingUsers = supabaseDatabase
            .from("users")
            .select(Columns.list())
            .decodeList<UserModel>()

        val existingUser = existingUsers.find { it.userId == user.id }

        val avatarUrl = user.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull

        if (existingUser != null) {
            if (avatarUrl != null && avatarUrl != existingUser.avatarUrl) {
                supabaseDatabase.from("users").update(
                    mapOf("avatarUrl" to avatarUrl)
                ) {
                    filter { eq("user_id", user.id) }
                }
            }
            return existingUser
        }
        val name = user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull ?: ""
        val age = user.userMetadata?.get("age")?.jsonPrimitive?.contentOrNull ?: ""
        val phone = user.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull ?: ""

        val role = "Staff"
        val created = user.createdAt?.toString() ?: "null"

        val newUser = UserModel(
            userId = user.id,
            name = name,
            age = age,
            email = user.email.orEmpty(),
            phone = phone,
            role = role,
            isLogin = true,
            createdAt = created,
            avatarUrl = avatarUrl // <-- Add this to your insert
        )
        supabaseDatabase.from("users").insert(newUser)
        return newUser
    }
    suspend fun editUserProfile(
        name: String,
        age: String,
        phone: String,
        avatarUrl: String? = null
    ): Result<UserModel> {
        return try {
            val session = supabaseAuth.currentSessionOrNull()
                ?: return Result.failure(Exception("Session not found"))

            val user = session.user
                ?: return Result.failure(Exception("User not found"))

            val userId = user.id
            val firstName = name.split(" ").firstOrNull() ?: name

            // Build new metadata JSON
            val newMetadata = buildJsonObject {
                put("full_name", JsonPrimitive(name))
                put("display_name", JsonPrimitive(firstName))
                put("age", JsonPrimitive(age))
                put("phone", JsonPrimitive(phone))
                avatarUrl?.let { put("avatar_url", JsonPrimitive(it)) } // only update if not null
            }

            // Update Supabase Auth metadata only
            supabaseAuth.updateUser {
                this.data = newMetadata
            }

            // Update user info in database (excluding avatarUrl)
            supabaseDatabase.from("users").update(
                mapOf(
                    "name" to name,
                    "age" to age,
                    "phone" to phone
                )
            ) {
                filter {
                    eq("user_id", userId)
                }
            }

            // Updated session model
            val updatedUser = UserModel(
                userId = userId,
                name = name,
                age = age,
                email = user.email.orEmpty(),
                phone = phone,
                createdAt = user.createdAt?.toString() ?: "",
                avatarUrl = avatarUrl ?: user.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            )

            userPreference.saveSession(updatedUser)

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }

    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            supabaseAuth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}