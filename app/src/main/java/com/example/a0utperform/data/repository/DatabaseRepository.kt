package com.example.a0utperform.data.repository

import android.util.Log
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.OutletData
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TeamData
import com.example.a0utperform.data.model.TeamDetail
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
    fun getUserImgUrl(): Result<String?> {
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
    suspend fun getUserPayroll(userId: String): Result<Double?> {
        return try {
            val response = supabaseDatabase
                .from("users")
                .select(Columns.list("payroll")) {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<Map<String, Double?>>()

            val payroll = response?.get("payroll")
            Result.success(payroll)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching payroll", e)
            Result.failure(e)
        }
    }

    suspend fun getAllOutlets(): Result<List<OutletDetail>> {
        return try {
            val outletList = supabaseDatabase
                .from("outlet")
                .select(Columns.list()) // or specify columns as needed
                .decodeList<OutletDetail>()

            Result.success(outletList)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching all outlets", e)
            Result.failure(e)
        }
    }

    suspend fun getTeamsByOutletId(outletId: String): Result<List<TeamDetail>> {
        return try {
            val teamList = supabaseDatabase
                .from("teams")
                .select(Columns.list()) {
                    filter { eq("outlet_id", outletId) }
                }
                .decodeList<TeamDetail>()

            Result.success(teamList)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching teams by outlet ID", e)
            Result.failure(e)
        }
    }

    suspend fun getStaffByOutlet(outletId: String): Result<List<StaffData>> {
        return try {
            Log.d("getStaffByOutlet", "Fetching staff for outletId: $outletId")

            // Step 1: Get all user_ids from user_outlet for this outlet
            val userOutletResponse = supabaseDatabase
                .from("user_outlet")
                .select(Columns.list()) {
                    filter { eq("outlet_id", outletId) }
                }
                .decodeList<Map<String, String>>()

            Log.d("getStaffByOutlet", "userOutletResponse: $userOutletResponse")

            val userIds = userOutletResponse.mapNotNull { it["user_id"] }

            Log.d("getStaffByOutlet", "Mapped user IDs: $userIds")

            if (userIds.isEmpty()) {
                Log.w("getStaffByOutlet", "No users found for outletId: $outletId")
                return Result.success(emptyList())
            }

            // Step 2: Fetch users using the list of IDs
            val usersResponse = supabaseDatabase
                .from("users")
                .select(Columns.list()) {
                    filter {
                    or {
                        userIds.forEach { userId ->
                            eq("user_id", userId)
                        }
                    }
                }
                }
                .decodeList<StaffData>()

            Log.d("getStaffByOutlet", "Fetched staff data: $usersResponse")

            Result.success(usersResponse)

        } catch (e: Exception) {
            Log.e("getStaffByOutlet", "Error fetching staff for outletId: $outletId", e)
            Result.failure(e)
        }
    }

    suspend fun getStaffByTeam(teamId: String): Result<List<StaffData>> {
        return try {
            val userTeamResponse = supabaseDatabase
                .from("user_team")
                .select(Columns.list()) {
                    filter { eq("team_id", teamId) }
                }
                .decodeList<Map<String, String>>()

            val userIds = userTeamResponse.mapNotNull { it["user_id"] }

            if (userIds.isEmpty()) return Result.success(emptyList())

            val usersResponse = supabaseDatabase
                .from("users")
                .select(Columns.list()) {
                    filter {
                        or {
                            userIds.forEach { userId ->
                                eq("user_id", userId)
                            }
                        }
                    }
                }
                .decodeList<StaffData>()

            Result.success(usersResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTasksByTeamId(teamId: String): Result<List<TaskData>> {
        return try {
            val tasks = supabaseDatabase
                .from("task")
                .select(Columns.list()) {
                    filter { eq("team_id", teamId) }
                }
                .decodeList<TaskData>()

            Result.success(tasks)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching tasks", e)
            Result.failure(e)
        }
    }

}