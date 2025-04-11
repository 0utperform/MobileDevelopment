package com.example.a0utperform.data.repository

import android.util.Log
import com.example.a0utperform.data.local.datastore.UserPreference
import com.example.a0utperform.data.model.OutletData
import com.example.a0utperform.data.model.TeamData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
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

}