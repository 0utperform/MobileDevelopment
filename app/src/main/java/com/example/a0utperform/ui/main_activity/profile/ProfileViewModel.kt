package com.example.a0utperform.ui.main_activity.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.datastore.UserPreference
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()

    private val _teamDetail = MutableLiveData<TeamDetail?>()
    val teamDetail: LiveData<TeamDetail?> = _teamDetail

    private val _outletDetail = MutableLiveData<OutletDetail?>()
    val outletDetail: LiveData<OutletDetail?> = _outletDetail

    fun fetchUserTeamAssignments(userId: String) {
        viewModelScope.launch {
            // Fetch team detail
            val teamResult = databaseRepository.getAssignedTeamDetails(userId)
            if (teamResult.isSuccess) {
                _teamDetail.value = teamResult.getOrNull()
            } else {
                _teamDetail.value = null
            }
        }
    }

    fun fetchUserOutletAssignments(userId: String) {
        viewModelScope.launch {
            val outletResult = databaseRepository.getAssignedOutletDetails(userId)
            if (outletResult.isSuccess) {
                _outletDetail.value = outletResult.getOrNull()
            } else {
                _outletDetail.value = null
            }
        }
    }
}