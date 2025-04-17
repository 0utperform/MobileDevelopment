package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutletViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _teams = MutableLiveData<List<TeamDetail>>()
    val teams: LiveData<List<TeamDetail>> = _teams

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchTeamsByOutlet(outletId: String) {
        viewModelScope.launch {
            userPreference.getSession().collect { user ->
                val userId = user.userId
                val role = user.role

                if (userId.isBlank() || role.isNullOrBlank()) {
                    _error.postValue("Invalid user session or role.")
                    return@collect
                }

                try {
                    val teamResult = databaseRepository.getTeamsByOutletId(outletId)
                    if (teamResult.isSuccess) {
                        val allTeams = teamResult.getOrNull().orEmpty()

                        if (role.equals("manager", ignoreCase = true)) {
                            _teams.postValue(allTeams)
                        } else if (role.equals("staff", ignoreCase = true)) {
                            val assignedTeamResult = databaseRepository.getAssignedTeamDetails(userId)
                            if (assignedTeamResult.isSuccess) {
                                val assignedTeam = assignedTeamResult.getOrNull()
                                val filtered = allTeams.filter { it.team_id == assignedTeam?.team_id }
                                _teams.postValue(filtered)
                            } else {
                                _error.postValue(assignedTeamResult.exceptionOrNull()?.message)
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
                }
            }
        }
    }
}
