package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutletDetailViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _teams = MutableLiveData<List<TeamDetail>>()
    val teams: LiveData<List<TeamDetail>> = _teams

    private val _staffList = MutableLiveData<List<StaffData>?>()
    val staffList: LiveData<List<StaffData>?> = _staffList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchTeamsByOutlet(outletId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            userPreference.getSession().collect { user ->
                val userId = user.userId
                val role = user.role

                if (userId.isBlank() || role.isNullOrBlank()) {
                    _error.postValue("Invalid user session or role.")
                    _isLoading.postValue(false)
                    return@collect
                }

                try {
                    val teamResult = databaseRepository.getTeamsByOutletId(outletId)
                    if (teamResult.isSuccess) {
                        val allTeams = teamResult.getOrNull().orEmpty()

                        if (role.equals("manager", ignoreCase = true)) {
                            // For managers, show all teams.
                            _teams.postValue(allTeams)
                        } else if (role.equals("staff", ignoreCase = true)) {
                            // For staff, filter based on the teams they are assigned to.
                            val assignedTeamsResult = databaseRepository.getAssignedTeamDetails(userId)
                            if (assignedTeamsResult.isSuccess) {
                                val assignedTeams = assignedTeamsResult.getOrNull().orEmpty()
                                val filteredTeams = allTeams.filter { team ->
                                    assignedTeams.any { assignedTeam -> assignedTeam.team_id == team.team_id }
                                }
                                _teams.postValue(filteredTeams)
                            } else {
                                _error.postValue(assignedTeamsResult.exceptionOrNull()?.message)
                                _teams.postValue(emptyList())
                            }
                        } else {
                            _error.postValue("Unknown role: $role")
                            _teams.postValue(emptyList())
                        }
                    } else {
                        _error.postValue(teamResult.exceptionOrNull()?.message)
                        _teams.postValue(emptyList())
                    }
                } catch (e: Exception) {
                    _error.postValue(e.message)
                    _teams.postValue(emptyList())
                } finally {
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun getUserRole() {
        viewModelScope.launch {
            userPreference.getSession().collect { session ->
                _userRole.postValue(session.role)
            }
        }
    }

    fun fetchStaffByOutlet(outletId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = databaseRepository.getStaffByOutlet(outletId)
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
}
