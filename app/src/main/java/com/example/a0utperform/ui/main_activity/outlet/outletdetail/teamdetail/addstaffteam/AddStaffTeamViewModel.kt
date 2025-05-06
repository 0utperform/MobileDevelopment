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

    fun loadUsers(teamId: String) {
        viewModelScope.launch {
            val result = repository.fetchUsersWithTeamStatus(teamId)
            result.onSuccess {
                _users.value = it
            }.onFailure {
                Log.e("ViewModel", "Error loading team users", it)
            }
        }
    }

    fun addUserToTeam(userId: String, teamId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.addUserToTeam(userId, teamId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to add user to team", e)
            }
        }
    }

    fun removeUserFromTeam(userId: String, teamId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.removeUserFromTeam(userId, teamId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to remove user from team", e)
            }
        }
    }
}
