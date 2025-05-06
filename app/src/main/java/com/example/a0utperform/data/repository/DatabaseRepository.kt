package com.example.a0utperform.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.a0utperform.BuildConfig
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.OutletData
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TaskEvidence
import com.example.a0utperform.data.model.TaskSubmission
import com.example.a0utperform.data.model.TeamData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.model.UserWithAssignment
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseAuth: Auth,
    private val supabaseDatabase: Postgrest,
    private val userPreference: UserPreference,
    private val supabaseClient: SupabaseClient
) {

    suspend fun getAssignedTeamDetails(userId: String): Result<List<TeamDetail>> {
        return try {
            Log.d("DatabaseRepository", "Fetching team assignments for user: $userId")

            // Fetch all team assignments for the user
            val teamAssignments = supabaseDatabase
                .from("user_team")
                .select(Columns.list()) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<TeamData>()

            if (teamAssignments.isEmpty()) {
                return Result.failure(Exception("User has no team assignments"))
            }

            Log.d("DatabaseRepository", "Fetching team details for ${teamAssignments.size} teams")

            // Fetch the details for all the teams the user is assigned to
            val teamDetails = teamAssignments.mapNotNull { userTeam ->
                supabaseDatabase
                    .from("teams")
                    .select(Columns.list()) {
                        filter { eq("team_id", userTeam.team_id) }
                    }
                    .decodeSingleOrNull<TeamDetail>()
            }

            if (teamDetails.isEmpty()) {
                return Result.failure(Exception("No team details found for the user"))
            }

            Result.success(teamDetails)
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
    suspend fun getSubmissionsByTeamId(teamId: String): Result<List<TaskSubmission>> {
        return try {
            val submissions = supabaseDatabase
                .from("task_submissions")
                .select(Columns.list()) {
                    filter { eq("team_id", teamId) }
                }
                .decodeList<TaskSubmission>()

            Result.success(submissions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSubmissionsByTaskId(taskId: String): Result<List<TaskSubmission>> {
        return try {
            val submissions = supabaseDatabase
                .from("task_submissions")
                .select(Columns.list()) {
                    filter { eq("task_id", taskId) }
                }
                .decodeList<TaskSubmission>()
            Result.success(submissions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvidenceBySubmissionId(submissionId: String): Result<List<TaskEvidence>> {
        return try {
            val evidenceList = supabaseDatabase
                .from("task_evidence")
                .select(Columns.list()) {
                    filter { eq("submission_id", submissionId) }
                }
                .decodeList<TaskEvidence>()
            Result.success(evidenceList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitTaskEvidence(
        task: TaskData,
        imageUris: List<Uri>,
        description: String,
        userId: String
    ) {
        val submissionId = UUID.randomUUID().toString()
        val now = Clock.System.now().toString()

        // Upload images to Supabase Storage
        val evidenceUrls = imageUris.mapIndexed { index, uri ->
            val filename = "evidence/$submissionId/${System.currentTimeMillis()}_$index.jpg"
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val byteArray = inputStream.readBytes()

            supabaseClient.storage
                .from("evidence")
                .upload(filename, byteArray) {
                    upsert = true
                    contentType = ContentType.Image.JPEG // Corrected content type
                }

            "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/evidence/$filename"
        }
        // Insert into task_submissions
        supabaseClient.from("task_submissions").insert(
            mapOf(
                "submission_id" to submissionId,
                "task_id" to task.task_id,
                "user_id" to userId,
                "team_id" to task.team_id,
                "submitted_at" to now,
                "description" to description
            )
        )

        // Insert each evidence
        val evidenceInserts = evidenceUrls.map {
            mapOf(
                "evidence_id" to UUID.randomUUID().toString(),
                "submission_id" to submissionId,
                "file_url" to it
            )
        }

        supabaseClient.from("task_evidence").insert(evidenceInserts)
    }

    suspend fun createNewOutlet(
        name: String,
        location: String,
        imageUri: Uri
    ): Result<Unit> {
        return try {
            val session = supabaseAuth.currentSessionOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            val user = session.user
            val managerId = user?.id
            val managerName = user?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
                ?: "Unknown Manager"

            // Upload image to Supabase Storage
            val outletId = UUID.randomUUID().toString()
            val filename = "outlets/$outletId.jpg"
            val inputStream = context.contentResolver.openInputStream(imageUri)!!
            val byteArray = inputStream.readBytes()
            withContext(Dispatchers.IO) {
                inputStream.close()
            }

            supabaseClient.storage
                .from("outlet")
                .upload(filename, byteArray) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }

            val imageUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/outlet/$filename"
            val now = Clock.System.now().toString()

            val outlet = OutletDetail(
                outlet_id = outletId,
                name = name,
                location = location,
                created_at = now,
                image_url = imageUrl,
                manager_id = managerId ?: "",
                manager_name = managerName,
                staff_size = 1
            )


            supabaseClient.from("outlets").insert(outlet)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error creating outlet", e)
            Result.failure(e)
        }
    }

    suspend fun createNewTeam(
        teamName: String,
        description: String,
        outletId: String,
        imageUri: Uri?,
    ): Result<Unit> {
        return try {
            // Generate a unique ID for the team
            val teamId = UUID.randomUUID().toString()
            val filename = "teams/$teamId.jpg"

            // Upload image to Supabase Storage
            val inputStream = imageUri?.let { context.contentResolver.openInputStream(it) }!!
            val byteArray = inputStream.readBytes()
            withContext(Dispatchers.IO) {
                inputStream.close()
            }

            supabaseClient.storage
                .from("team")
                .upload(filename, byteArray) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }

            val imageUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/team/$filename"

            // Create TeamDetail object
            val teamDetail = TeamDetail(
                name = teamName,
                description = description,
                outlet_id = outletId,
                img_url = imageUrl,
                staffSize = "0"
            )


            supabaseClient.from("teams").insert(teamDetail)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TeamRepository", "Error creating team", e)
            Result.failure(e)
        }
    }

    suspend fun createTask(
        task: TaskData,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            // Generate a unique ID for the task
            val taskId = UUID.randomUUID().toString()
            val filename = "tasks/$taskId.jpg"

            // Upload image if exists
            val imageUrl = if (imageUri != null) {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Cannot open image URI")
                val byteArray = inputStream.readBytes()
                withContext(Dispatchers.IO) {
                    inputStream.close()
                }

                supabaseClient.storage
                    .from("image")
                    .upload(filename, byteArray) {
                        upsert = true
                        contentType = ContentType.Image.JPEG
                    }

                "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/image/$filename"
            } else {
                null
            }

            // Create final task object with generated ID and image URL
            val finalTask = task.copy(
                task_id = taskId,
                img_url = imageUrl
            )

            // Insert task into "task" table
            supabaseClient
                .from("task")
                .insert(finalTask)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error creating task", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUsersWithAssignmentStatus(outletId: String): Result<List<UserWithAssignment>> {
        return try {
            val users = supabaseClient.from("users")
                .select()
                .decodeList<UserModel>()

            val userOutlets = supabaseClient.from("user_outlet")
                .select()
                .decodeList<OutletData>()

            val assignedUserIds = userOutlets.filter { it.outlet_id == outletId }.map { it.user_id }.toSet()

            val userWithStatus = users.map { user ->
                UserWithAssignment(user, assignedUserIds.contains(user.userId))
            }

            Result.success(userWithStatus)
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching users", e)
            Result.failure(e)
        }
    }
    suspend fun addUserToOutlet(userId: String, outletId: String) {
        supabaseClient.from("user_outlet").insert(
            mapOf("user_id" to userId, "outlet_id" to outletId)
        )
    }

    suspend fun removeUserFromOutlet(userId: String, outletId: String) {
        supabaseClient.from("user_outlet")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("outlet_id", outletId)
                }
            }
    }

    suspend fun fetchUsersWithTeamStatus(teamId: String): Result<List<UserWithAssignment>> {
        return try {
            val users = supabaseClient.from("users")
                .select()
                .decodeList<UserModel>()

            val userTeams = supabaseClient.from("user_team")
                .select()
                .decodeList<TeamData>() // Assume TeamData has fields: user_id, team_id

            val assignedUserIds = userTeams.filter { it.team_id == teamId }.map { it.user_id }.toSet()

            val userWithStatus = users.map { user ->
                UserWithAssignment(user, assignedUserIds.contains(user.userId))
            }

            Result.success(userWithStatus)
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching users for team", e)
            Result.failure(e)
        }
    }

    suspend fun addUserToTeam(userId: String, teamId: String) {
        supabaseClient.from("user_team").insert(
            mapOf("user_id" to userId, "team_id" to teamId)
        )
    }

    suspend fun removeUserFromTeam(userId: String, teamId: String) {
        supabaseClient.from("user_team")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("team_id", teamId)
                }
            }
    }

}