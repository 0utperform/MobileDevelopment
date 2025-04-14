package com.example.a0utperform.data.repository

import android.util.Log
import com.example.a0utperform.data.local.datastore.UserPreference
import com.example.a0utperform.data.model.OutletData
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.TeamData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
    private val supabaseAuth: Auth,
    private val supabaseDatabase: Postgrest,
    private val userPreference: UserPreference
) {

    suspend fun getAssignedTeamDetails(userId: String): Result<TeamDetail?> {
        return try {
            Log.d("DatabaseRepository", "Fetching team assignment for user: $userId")

            val teamAssignments = supabaseDatabase
                .from("user_team")
                .select(Columns.list()) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<TeamData>()

            val userTeam = teamAssignments.find { it.user_id == userId }
            if (userTeam == null) {
                return Result.failure(Exception("User has no team assignment"))
            }

            Log.d("DatabaseRepository", "Fetching team detail for team_id: ${userTeam.team_id}")

            val teamDetail = supabaseDatabase
                .from("teams")
                .select(Columns.list()) {
                    filter { eq("team_id", userTeam.team_id) }
                }
                .decodeSingleOrNull<TeamDetail>()

            Result.success(teamDetail)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching assigned team details", e)
            Result.failure(e)
        }
    }
    suspend fun getAssignedOutletDetails(userId: String): Result<OutletDetail?> {
        return try {
            Log.d("DatabaseRepository", "Fetching outlet assignment for user: $userId")

            val outletAssignments = supabaseDatabase
                .from("user_outlet")
                .select(Columns.list()) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<OutletData>()

            val userOutlet = outletAssignments.find { it.user_id == userId }
            if (userOutlet == null) {
                return Result.failure(Exception("User has no Outlet assignment"))
            }

            Log.d("DatabaseRepository", "Fetching Outlet detail for outlet_id: ${userOutlet.outlet_id}")

            val outletDetail = supabaseDatabase
                .from("outlet")
                .select(Columns.list()) {
                    filter { eq("outlet_id", userOutlet.outlet_id) }
                }
                .decodeSingleOrNull<OutletDetail>()

            Result.success(outletDetail)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching assigned outlet details", e)
            Result.failure(e)
        }
    }
    suspend fun getUserImgUrl(): Result<String?> {
        return try {
            val session = supabaseAuth.currentSessionOrNull()
                ?: return Result.failure(Exception("Session is null"))

            val user = session.user
                ?: return Result.failure(Exception("User not found, please register first!"))

            val avatarUrl = user.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull

            Result.success(avatarUrl)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching user avatar URL", e)
            Result.failure(e)
        }
    }
}