package com.example.a0utperform.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.a0utperform.BuildConfig
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.Attendance
import com.example.a0utperform.data.model.AttendanceStats
import com.example.a0utperform.data.model.LeaveRequest
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
import com.example.a0utperform.utils.formatToSupabaseTimestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Objects.isNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.*
import java.time.YearMonth
import java.util.Locale

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
                        filter {eq("team_id", userTeam.team_id) }

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
    suspend fun getAssignedOutletDetails(userId: String): Result<List<OutletDetail>> {
        return try {
            Log.d("DatabaseRepository", "Fetching outlet assignments for user: $userId")

            val outletAssignments = supabaseDatabase
                .from("user_outlet")
                .select(Columns.list()) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<OutletData>()

            if (outletAssignments.isEmpty()) {
                return Result.failure(Exception("User has no Outlet assignment"))
            }

            Log.d("DatabaseRepository", "Fetching Outlet details for ${outletAssignments.size} outlets")

            // Parallel fetch
            val outletDetails = coroutineScope {
                outletAssignments.map { assignment ->
                    async {
                        try {
                            supabaseDatabase
                                .from("outlet")
                                .select(Columns.list()) {
                                    filter { eq("outlet_id", assignment.outlet_id) }
                                }
                                .decodeSingleOrNull<OutletDetail>()
                        } catch (e: Exception) {
                            Log.e("DatabaseRepository", "Error fetching outlet_id=${assignment.outlet_id}", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            if (outletDetails.isEmpty()) {
                return Result.failure(Exception("No outlet details found for the user"))
            }

            Result.success(outletDetails)
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
        try {
            // Add user to user_outlet
            supabaseClient.from("user_outlet").insert(
                mapOf("user_id" to userId, "outlet_id" to outletId)
            )

            // Fetch current outlet detail
            val outlet = supabaseClient.from("outlet")
                .select { filter { eq("outlet_id", outletId) } }
                .decodeSingle<OutletDetail>()  // Assuming you have this data class

            val currentStaffSize = outlet.staff_size
            val newStaffSize = currentStaffSize + 1

            // Update staff_size in outlets
            supabaseClient.from("outlet").update(
                mapOf("staff_size" to newStaffSize.toString())
            ) {
                filter { eq("outlet_id", outletId) }
            }

        } catch (e: Exception) {
            Log.e("Repository", "Error adding user to outlet", e)
        }
    }


    suspend fun removeUserFromOutlet(userId: String, outletId: String) {
        try {
            // Remove user from user_outlet
            supabaseClient.from("user_outlet")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("outlet_id", outletId)
                    }
                }

            // Fetch current outlet detail
            val outlet = supabaseClient.from("outlet")
                .select { filter { eq("outlet_id", outletId) } }
                .decodeSingle<OutletDetail>()  // Reuse your outlet model

            val currentStaffSize = outlet.staff_size
            val newStaffSize = maxOf(0, currentStaffSize - 1)

            // Update staff_size in outlets
            supabaseClient.from("outlet").update(
                mapOf("staff_size" to newStaffSize.toString())
            ) {
                filter { eq("outlet_id", outletId) }
            }

        } catch (e: Exception) {
            Log.e("Repository", "Error removing user from outlet", e)
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
        try {
            // Add user to user_team
            supabaseClient.from("user_team").insert(
                mapOf("user_id" to userId, "team_id" to teamId)
            )

            // Fetch current team detail
            val team = supabaseClient.from("teams")
                .select { filter { eq("team_id", teamId) } }
                .decodeSingle<TeamDetail>()

            val currentStaffSize = team.staffSize?.toIntOrNull() ?: 0
            val newStaffSize = currentStaffSize + 1

            // Update staff_size
            supabaseClient.from("teams").update(
                mapOf("staff_size" to newStaffSize.toString())
            ) {
                filter { eq("team_id", teamId) }
            }

        } catch (e: Exception) {
            Log.e("Repository", "Error adding user to team", e)
        }
    }
    suspend fun removeUserFromTeam(userId: String, teamId: String) {
        try {
            // Remove user from user_team
            supabaseClient.from("user_team")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("team_id", teamId)}
                }

            // Fetch current team detail
            val team = supabaseClient.from("teams")
                .select {filter { eq("team_id", teamId) }}

                .decodeSingle<TeamDetail>()

            val currentStaffSize = team.staffSize?.toIntOrNull() ?: 0
            val newStaffSize = maxOf(0, currentStaffSize - 1) // Prevent negative

            // Update staff_size
            supabaseClient.from("teams").update(
                mapOf("staff_size" to newStaffSize.toString())
            ) { filter {     eq("team_id", teamId) }
            }

        } catch (e: Exception) {
            Log.e("Repository", "Error removing user from team", e)
        }
    }

    suspend fun getTop3OutletsByRevenue(): Result<List<OutletDetail>> {
        return try {
            val outlets = supabaseDatabase
                .from("outlet")
                .select(Columns.list()) {
                    order("revenue", Order.DESCENDING)
                    limit(3)
                }
                .decodeList<OutletDetail>()
            Result.success(outlets)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching top outlets", e)
            Result.failure(e)
        }
    }

    suspend fun getTop3TeamsByCompletion(): Result<List<TeamDetail>> {
        return try {
            val teams = supabaseDatabase
                .from("teams")
                .select(Columns.list()) {
                    order("completion_rate", Order.DESCENDING)
                    limit(3)
                }
                .decodeList<TeamDetail>()

            Result.success(teams)
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching top teams", e)
            Result.failure(e)
        }
    }

    private val TAG = "AttendanceRepository"

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun clockIn(userId: String): Result<Attendance> = withContext(Dispatchers.IO) {
        try {

            val jakartaZone = ZoneId.of("Asia/Jakarta")
            val currentTime = ZonedDateTime.now(java.time.Clock.system(jakartaZone))
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedTime = currentTime.format(formatter)

            val attendance = Attendance(
                user_id = userId,
                clock_in = formattedTime,
                status = "active" // or whatever status you want to use
            )

            val result = supabaseDatabase
                .from("attendance")
                .insert(attendance) {
                    select(Columns.list("attendance_id","user_id","status","clock_in","created_at"))

                }

                .decodeSingle<Attendance>()


            Log.d(TAG, "Clock-in success: $result")
            return@withContext Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error during clock-in operation", e)
            return@withContext Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun clockOut(userId: String): Result<Attendance> = withContext(Dispatchers.IO) {
        try {
            val jakartaZone = ZoneId.of("Asia/Jakarta")
            val currentTime = ZonedDateTime.now(java.time.Clock.system(jakartaZone))
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedTime = currentTime.format(formatter)

            val latestAttendance = supabaseDatabase
                .from("attendance")
                .select(Columns.list()) {
                    select(Columns.list("attendance_id","user_id","status","clock_in", "clock_out","created_at"))
                    filter {
                        eq("user_id", userId)
                        eq("status", "active")
                        isNull("clock_out")
                    }
                    order("created_at", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<Attendance>() ?: return@withContext Result.failure(Exception("No active attendance record found"))

            // Update the attendance record with clock_out time
            val updatedAttendance = supabaseDatabase
                .from("attendance")
                .update({
                    set("clock_out", formattedTime)
                    set("status", "completed")
                }) {
                    select(Columns.list("attendance_id","user_id","status","clock_in","clock_out","created_at"))
                    filter { eq("attendance_id", latestAttendance.attendance_id!!) }
                }
                .decodeSingle<Attendance>()

            Log.d(TAG, "Clock-out success: $updatedAttendance")
            return@withContext Result.success(updatedAttendance)
        } catch (e: Exception) {
            Log.e(TAG, "Error during clock-out operation", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun getLatestAttendance(userId: String): Result<Attendance?> = withContext(Dispatchers.IO) {
        try {
            val attendance = supabaseDatabase
                .from("attendance")
                .select(Columns.list()) {
                    filter { eq("user_id", userId) }
                    order("created_at", order = Order.DESCENDING )
                    limit(1)
                }
                .decodeSingleOrNull<Attendance>()

            return@withContext Result.success(attendance)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching latest attendance", e)
            return@withContext Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodayAttendance(userId: String): Result<Attendance?> = withContext(Dispatchers.IO) {
        try {
            val jakartaZone = ZoneId.of("Asia/Jakarta")
            val today = LocalDate.now(jakartaZone)
            val startOfDay = today.atStartOfDay(jakartaZone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val endOfDay = today.plusDays(1).atStartOfDay(jakartaZone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val attendance = supabaseDatabase
                .from("attendance")
                .select(Columns.list()) {
                    filter {
                        eq("user_id", userId)
                        gte("created_at", startOfDay)
                        lt("created_at", endOfDay)
                    }
                    order("created_at", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<Attendance>()

            return@withContext Result.success(attendance)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching today's attendance", e)
            return@withContext Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun shouldShowClockIn(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val latestAttendanceResult = getLatestAttendance(userId)
            if (latestAttendanceResult.isFailure) {
                return@withContext Result.success(true) // If error fetching, default to showing clock in
            }

            val latestAttendance = latestAttendanceResult.getOrNull()
            if (latestAttendance == null) {
                return@withContext Result.success(true) // No attendance records, show clock in
            }

            // If there's a record but no clock out, we should show clock out instead
            if (latestAttendance.clock_out == null && latestAttendance.status == "active") {
                return@withContext Result.success(false) // Show clock out button
            }

            // Check if the last attendance record is from a previous day
            val jakartaZone = ZoneId.of("Asia/Jakarta")
            val today = LocalDate.now(jakartaZone)

            try {
                // Parse the created_at timestamp
                val createdAtTime = if (latestAttendance.created_at != null) {
                    ZonedDateTime.parse(latestAttendance.created_at, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } else {
                    // Fallback to clock_in if created_at is null
                    ZonedDateTime.parse(latestAttendance.clock_in, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }

                val createdAtDate = createdAtTime.toLocalDate()

                // If created_at date is before today, show clock in
                return@withContext Result.success(createdAtDate.isBefore(today))
            } catch (e: DateTimeParseException) {
                Log.e(TAG, "Error parsing date", e)
                return@withContext Result.success(true) // Default to showing clock in on parsing error
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error determining clock in status", e)
            return@withContext Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun calculateWorkHours(attendance: Attendance): Result<Double> = withContext(Dispatchers.IO) {
        try {
            if (attendance.clock_in == null || attendance.clock_out == null) {
                return@withContext Result.success(0.0)
            }

            val clockInTime = ZonedDateTime.parse(attendance.clock_in, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val clockOutTime = ZonedDateTime.parse(attendance.clock_out, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val durationInSeconds = java.time.Duration.between(clockInTime, clockOutTime).seconds
            val hours = durationInSeconds / 3600.0

            return@withContext Result.success(hours)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating work hours", e)
            return@withContext Result.failure(e)
        }
    }






    suspend fun getUserAttendanceStats(userId: String): AttendanceStats {
        val response = supabaseDatabase.from("attendance")
            .select() {
                filter { eq("user_id", userId) }
            }

        val root = response.data ?: return AttendanceStats(0, 0, 0, 0.0)

        val json = Json.parseToJsonElement(root.toString())

        if (json !is JsonArray) return AttendanceStats(0, 0, 0, 0.0)

        var total = 0
        var completed = 0
        var absent = 0

        for (item in json) {
            val status = item.jsonObject["status"]?.jsonPrimitive?.contentOrNull?.lowercase()

            if (status != null) {
                total++
                when (status) {
                    "completed" -> completed++
                    "absent" -> absent++
                }
            }
        }

        val percentage = if (total > 0) {
            String.format(Locale.US, "%.1f", (completed.toDouble() / total) * 100).toDouble()
        } else {
            0.0
        }

        return AttendanceStats(completed, absent, total,  percentage)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAttendanceByMonth(userId: String, month: YearMonth): Map<LocalDate, String> {
        val start = month.atDay(1).atStartOfDay(ZoneId.of("Asia/Jakarta"))
        val end = month.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of("Asia/Jakarta"))

        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val response = supabaseDatabase
            .from("attendance")
            .select() {
                filter {
                    eq("user_id", userId)
                    gte("created_at", start.format(formatter))
                    lt("created_at", end.format(formatter))
                }
            }

        val attendanceMap = mutableMapOf<LocalDate, String>()
        val jsonArray = Json.parseToJsonElement(response.data.toString()) as? JsonArray ?: return attendanceMap

        for (item in jsonArray) {
            val createdAt = item.jsonObject["created_at"]?.jsonPrimitive?.content
            val status = item.jsonObject["status"]?.jsonPrimitive?.contentOrNull
            if (createdAt != null && status != null) {
                val date = ZonedDateTime.parse(createdAt).toLocalDate()
                attendanceMap[date] = status
            }
        }

        return attendanceMap
    }

    fun getLeaveRequests(): Flow<List<LeaveRequest>> = flow {
        val role = userPreference.getSession().first().role
        val userId = userPreference.getSession().first().userId

        val query = if (role == "Staff") {
            supabaseDatabase.from("leave_requests")
                .select {
                    filter {   eq("user_id", userId) }
                }

        } else {
            supabaseDatabase.from("leave_requests")
                .select()
        }

        val result = query.decodeList<LeaveRequest>()
        emit(result)
    }

    suspend fun getUsernameById(userId: String): String {
        return try {
            val result = supabaseDatabase.from("users")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeSingle<UserModel>()  // Decode to your data class
            result.name
        } catch (e: Exception) {
            "Unknown User"
        }
    }

    suspend fun updateLeaveRequestStatus(requestId: String, newStatus: String): Boolean {
        return try {
            val result = supabaseDatabase
                .from("leave_requests")
                .update(
                    {
                        set("status", newStatus)
                    }
                ) {
                    select(Columns.list("request_id","user_id","start_date","end_date","reason","status","type","created_at"))
                    filter { eq("request_id", requestId) }
                }
                .decodeSingle<LeaveRequest>()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserRole(): String? {
        return userPreference.getSession().first().role
    }

    suspend fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentSessionOrNull()?.user?.id
    }

}
