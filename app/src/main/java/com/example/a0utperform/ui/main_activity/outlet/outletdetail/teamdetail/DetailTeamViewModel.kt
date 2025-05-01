package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTeamViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {
    fun getUserRole(): Flow<String?> = userPreference.getSession().map { it.role }
    private val _taskList = MutableLiveData<List<TaskData>?>()
    val taskList: LiveData<List<TaskData>?> = _taskList

    private val _team = MutableLiveData<TeamDetail>()
    val team: LiveData<TeamDetail> = _team

    private val _staffList = MutableLiveData<List<StaffData>?>()
    val staffList: LiveData<List<StaffData>?> = _staffList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error



    fun setTeamDetail(team: TeamDetail) {
        _team.value = team
        team.team_id?.let { fetchStaffByTeam(it) }
        team.team_id?.let { fetchTasksForTeam(it) }
    }

    private fun fetchStaffByTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = databaseRepository.getStaffByTeam(teamId)
                if (result.isSuccess) {
                    _staffList.postValue(result.getOrNull())
                } else {
                    _error.postValue(result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    private fun fetchTasksForTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = databaseRepository.getTasksByTeamId(teamId)
                if (result.isSuccess) {
                    _taskList.postValue(result.getOrNull())
                } else {
                    _error.postValue(result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
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
                            if (target != null) {
                                totalTargetSubmissions = target
                            }
                        }
                    }
                    _taskList.postValue(updatedTasks)
                } else {
                    _error.postValue(tasksResult.exceptionOrNull()?.message ?: "Error loading data")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}