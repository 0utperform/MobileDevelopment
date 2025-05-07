package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.addstaffteam

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.UserWithAssignment
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStaffTeamViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<UserWithAssignment>>()
    val users: LiveData<List<UserWithAssignment>> get() = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadUsersWithTeamStatusFilteredByOutlet(teamId: String, outletId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teamResult = repository.fetchUsersWithTeamStatus(teamId)
                val outletResult = repository.fetchUsersWithAssignmentStatus(outletId)

                if (teamResult.isSuccess && outletResult.isSuccess) {
                    val outletAssignedUsers = outletResult.getOrNull()
                        ?.filter { it.isAssigned }
                        ?.map { it.user.userId }
                        ?.toSet() ?: emptySet()

                    val filteredTeamUsers = teamResult.getOrNull()
                        ?.filter { outletAssignedUsers.contains(it.user.userId) }
                        ?: emptyList()

                    _users.value = filteredTeamUsers
                } else {
                    Log.e("ViewModel", "Failed to load users filtered by outlet and team")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error filtering users by outlet and team", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUserToTeam(userId: String, teamId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addUserToTeam(userId, teamId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to add user to team", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeUserFromTeam(userId: String, teamId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.removeUserFromTeam(userId, teamId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to remove user from team", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
