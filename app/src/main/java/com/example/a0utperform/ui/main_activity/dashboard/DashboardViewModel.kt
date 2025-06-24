package com.example.a0utperform.ui.main_activity.dashboard

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.Attendance
import com.example.a0utperform.data.model.AttendanceStats
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.repository.DatabaseRepository
import com.example.a0utperform.utils.formatHoursToDigitalTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
): ViewModel() {
    private val TAG = "AttendanceViewModel"

    private val _avatarUrl = MutableLiveData<String?>()
    val avatarUrl: LiveData<String?> = _avatarUrl

    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()

    private val _taskList = MutableLiveData<List<TaskData>?>()
    val taskList: LiveData<List<TaskData>?> = _taskList

    fun getUserRole(): Flow<String?> = userPreference.getSession().map { it.role }

    private val _teamAssignment = MutableLiveData<List<TeamDetail>>()
    val teamAssignment: LiveData<List<TeamDetail>> = _teamAssignment

    private val _clockInState = MutableLiveData<Boolean?>()
    val clockInState: LiveData<Boolean?> = _clockInState

    private val _clockInTime = MutableLiveData<String>()
    val clockInTime: LiveData<String> = _clockInTime

    private val _clockOutTime = MutableLiveData<String>()
    val clockOutTime: LiveData<String> = _clockOutTime

    private val _totalHours = MutableLiveData<String>()
    val totalHours: LiveData<String> = _totalHours

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _attendanceStats = MutableLiveData<AttendanceStats>()
    val attendanceStats: LiveData<AttendanceStats> = _attendanceStats

    private val _currentAttendance = MutableLiveData<Attendance?>()

    init {
        checkInitialClockInState()
    }

    fun fetchAttendanceStats(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val stats = databaseRepository.getUserAttendanceStats(userId)
                _attendanceStats.value = stats
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkInitialClockInState() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userPreference.getSession().first().userId
                val todayAttendanceResult = databaseRepository.getTodayAttendance(userId)

                if (todayAttendanceResult.isSuccess) {
                    val attendance = todayAttendanceResult.getOrNull()
                    _currentAttendance.value = attendance

                    if (attendance == null) {
                        _clockInState.value = true
                    } else if (attendance.clock_in == null && attendance.clock_out == null) {
                        // Absent → hide both buttons
                        _clockInState.value = null
                    } else if (attendance.clock_out == null) {
                        _clockInState.value = false
                        updateAttendanceDisplay(attendance)
                    } else {
                        _clockInState.value = null
                        updateAttendanceDisplay(attendance)
                    }

                } else {
                    _error.value = "Failed to check attendance: ${todayAttendanceResult.exceptionOrNull()?.message}"
                    _clockInState.value = true // Default to showing clock in on error
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in checkInitialClockInState", e)
                _error.value = "Failed to check attendance: ${e.message}"
                _clockInState.value = true // Default to showing clock in on error
            } finally {
                _isLoading.value = false
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun clockIn() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = userPreference.getSession().first().userId
                val result = databaseRepository.clockIn(userId)

                if (result.isSuccess) {
                    val attendance = result.getOrNull()
                    if (attendance != null) {
                        _currentAttendance.value = attendance
                        _clockInState.value = false // Show clock out button
                        updateAttendanceDisplay(attendance)
                    } else {
                        _error.value = "Clock-in successful but no data returned"
                    }
                } else {
                    _error.value = "Failed to clock in: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in clockIn", e)
                _error.value = "Failed to clock in: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun clockOut() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = userPreference.getSession().first().userId
                val result = databaseRepository.clockOut(userId)

                if (result.isSuccess) {
                    val attendance = result.getOrNull()
                    if (attendance != null) {
                        _currentAttendance.value = attendance
                        _clockInState.value = true // Show clock in button for tomorrow
                        updateAttendanceDisplay(attendance)
                    } else {
                        _error.value = "Clock-out successful but no data returned"
                    }
                } else {
                    _error.value = "Failed to clock out: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in clockOut", e)
                _error.value = "Failed to clock out: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAttendanceDisplay(attendance: Attendance) {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val jakartaZone = ZoneId.of("Asia/Jakarta")

                // Format clock in time
                if (attendance.clock_in != null) {
                    val clockInDateTime = ZonedDateTime.parse(attendance.clock_in, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    _clockInTime.value = clockInDateTime.format(formatter)
                } else {
                    _clockInTime.value = ""
                }

                // Format clock out time
                if (attendance.clock_out != null) {
                    val clockOutDateTime = ZonedDateTime.parse(attendance.clock_out, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    _clockOutTime.value = clockOutDateTime.format(formatter)

                    // Calculate and format total hours
                    val hoursResult = databaseRepository.calculateWorkHours(attendance)
                    if (hoursResult.isSuccess) {
                        val hours = hoursResult.getOrNull() ?: 0.0
                        val formattedTime = formatHoursToDigitalTime(hours)
                        _totalHours.value = formattedTime
                    } else {
                        _totalHours.value = "0.00"
                    }
                } else {
                    _clockOutTime.value = ""
                    _totalHours.value = "0.00"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting attendance times", e)
                _error.value = "Error formatting attendance times: ${e.message}"
            }
        }
    }

    // Refresh attendance data (useful after configuration changes or returning to the app)
    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshAttendanceData() {
        checkInitialClockInState()
    }

    fun fetchTasksWithProgress(teamId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val tasksResult = databaseRepository.getTasksByTeamId(teamId)
                val submissionsResult = databaseRepository.getSubmissionsByTeamId(teamId)

                if (tasksResult.isSuccess && submissionsResult.isSuccess) {
                    val tasks = tasksResult.getOrNull() ?: emptyList()
                    val submissions = submissionsResult.getOrNull() ?: emptyList()
                    val taskMap = submissions.groupBy { it.task_id }

                    val updatedTasks = tasks.map { task ->
                        val completed = taskMap[task.task_id]?.size ?: 0
                        val target = task.submission_per_day
                        task.apply {
                            completedSubmissions = completed
                            totalTargetSubmissions = target ?: 0
                        }
                    }
                    _taskList.postValue(updatedTasks)
                } else {
                    _error.postValue("Failed to fetch tasks or submissions.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun fetchAvatarUrl() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = databaseRepository.getUserImgUrl()
                _avatarUrl.value = result.getOrNull()
            } catch (e: Exception) {
                _error.postValue("Failed to fetch avatar: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun fetchTeamAssignment() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                userSession.value?.let { session ->
                    val result = databaseRepository.getAssignedTeamDetails(session.userId)
                    _teamAssignment.value = result.getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                _error.postValue("Failed to fetch team assignment: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

}