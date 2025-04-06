package com.example.a0utperform.data.repository

import android.util.Log
import androidx.credentials.CredentialManager
import com.example.a0utperform.data.datastore.UserData
import com.example.a0utperform.data.datastore.UserModel
import com.example.a0utperform.data.datastore.UserPreference
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AuthRepository @Inject constructor(
    private val supabaseAuth: Auth,
    private val supabaseDatabase: Postgrest,
    private val userPreference: UserPreference
) {

    suspend fun registerUser(name: String, email: String, password: String, phone:String): Result<UserModel> {
        return try {
            val response = supabaseAuth.signUpWith(Email) {
                this.email = email
                this.password = password

                this.data = buildJsonObject {
                    put("display_name", JsonPrimitive(name))
                    put("phone", JsonPrimitive(phone))
                }

            }

            val session = supabaseAuth.currentSessionOrNull() ?: return Result.failure(Exception("Session is null"))
            val user = session.user ?: return Result.failure(Exception("User is null"))

            // Save user details in Supabase database
            val userData = UserData(
                user_id = user.id,
                name = name,
                email = email,
                phone = phone,
                created_at = Clock.System.now().toString()

            )
            supabaseDatabase.from("users").insert(userData)

            val userModel = UserModel(user.id, email, name, phone, isLogin = true)

            userPreference.saveSession(userModel)
            Result.success(userModel)
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
                ?: return Result.failure(Exception("User is not found, please register first!"))

            val usersList = supabaseDatabase
                .from("users")
                .select(Columns.list())
                .decodeList<UserData>()

            val userData = usersList.find { it.user_id == user.id }
                ?: return Result.failure(Exception("User not found"))

            val userModel = UserModel(user.id, email, userData.name, userData.phone, isLogin = true)


            userPreference.saveSession(userModel)

            Result.success(userModel)
        } catch (e: Exception) {
            Log.e("LoginError", "Login failed", e)
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    suspend fun signOut() {
        supabaseAuth.signOut()

        userPreference.logout()
    }

    fun getCurrentUser(): Flow<UserModel> {
        return userPreference.getSession()
    }
}