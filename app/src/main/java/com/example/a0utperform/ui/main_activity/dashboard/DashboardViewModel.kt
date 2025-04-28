package com.example.a0utperform.ui.main_activity.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
): ViewModel() {

    private val _avatarUrl = MutableLiveData<String?>()
    val avatarUrl: LiveData<String?> = _avatarUrl

    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()

    private val _taskList = MutableLiveData<List<TaskData>?>()
    val taskList: LiveData<List<TaskData>?> = _taskList

    fun getUserRole(): Flow<String?> = userPreference.getSession().map { it.role }


    private val _teamAssignment = MutableLiveData<List<TeamDetail>>()
    val teamAssignment: LiveData<List<TeamDetail>> = _teamAssignment


    fun fetchTasksWithProgress(teamId: String) {
        viewModelScope.launch {
            //_isLoading.postValue(true)
            try {
                val tasksResult = databaseRepository.getTasksByTeamId(teamId)
                val submissionsResult = databaseRepository.getSubmissionsByTeamId(teamId)

                if (tasksResult.isSuccess && submissionsResult.isSuccess) {
                    val tasks = tasksResult.getOrNull() ?: emptyList()
                    val submissions = submissionsResult.getOrNull() ?: emptyList()
                    val taskMap = submissions.groupBy { it.task_id }

                    // Update tasks with completed and target submissions
                    val updatedTasks = tasks.map { task ->
                        val completed = taskMap[task.task_id]?.size ?: 0
                        val target = task.submission_per_day
                        task.apply {
                            completedSubmissions = completed
                            totalTargetSubmissions = target ?: 0
                        }
                    }
                    _taskList.postValue(updatedTasks)
                }
            } catch (e: Exception) {
               // _error.postValue(e.message)
            } finally {
                //_isLoading.postValue(false)
            }
        }
    }

    fun fetchAvatarUrl() {
        viewModelScope.launch {
            val result = databaseRepository.getUserImgUrl()
            _avatarUrl.value = result.getOrNull()
        }
    }

    fun fetchTeamAssignment() {
        viewModelScope.launch {
            userSession.value?.let { session ->
                val result = databaseRepository.getAssignedTeamDetails(session.userId)
                _teamAssignment.value = result.getOrNull() ?: emptyList()
            }
        }
    }
}