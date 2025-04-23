package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTeamViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    val currentUser: LiveData<UserModel> = userPreference.getSession().asLiveData()

    private val _submissionMap = MutableLiveData<Map<String, Int>>()
    val submissionMap: LiveData<Map<String, Int>> = _submissionMap

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
        fetchStaffByTeam(team.team_id)
        fetchTasksForTeam(team.team_id)
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
    fun fetchTasksForTeam(teamId: String) {
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTeamTasks(teamId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val tasksResult = databaseRepository.getTasksByTeamId(teamId)
            val submissionResult = databaseRepository.getTodaySubmissions(userId, teamId)

            if (tasksResult.isSuccess && submissionResult.isSuccess) {
                val tasks = tasksResult.getOrNull() ?: emptyList()
                val submissionMap = submissionResult.getOrNull() ?: emptyMap()

                _taskList.value = tasks
                _submissionMap.value = submissionMap
            } else {
                _error.value = tasksResult.exceptionOrNull()?.message
                    ?: submissionResult.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }
}