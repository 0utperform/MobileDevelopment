package com.example.a0utperform.ui.main_activity.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _teamAssignment = MutableLiveData<List<TeamDetail>>()
    val teamAssignment: LiveData<List<TeamDetail>> = _teamAssignment

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