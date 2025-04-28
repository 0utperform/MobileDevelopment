package com.example.a0utperform.ui.main_activity.profile

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
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _teamDetail = MutableLiveData<List<TeamDetail?>>()
    val teamDetail: LiveData<List<TeamDetail?>> = _teamDetail

    private val _outletDetail = MutableLiveData<OutletDetail?>()
    val outletDetail: LiveData<OutletDetail?> = _outletDetail

    private val _avatarUrl = MutableLiveData<String?>()
    val avatarUrl: LiveData<String?> = _avatarUrl

    private val _payroll = MutableLiveData<Double?>()
    val payroll: LiveData<Double?> = _payroll



    fun fetchAvatarUrl() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = databaseRepository.getUserImgUrl()
            _avatarUrl.value = result.getOrNull()
            _isLoading.value = false
        }
    }

    // Fetch Payroll
    fun fetchPayroll(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = databaseRepository.getUserPayroll(userId)
            if (result.isSuccess) {
                _payroll.value = result.getOrNull()
            } else {
                _payroll.value = null
            }
            _isLoading.value = false
        }
    }

    // Fetch User Team Assignments
    fun fetchUserTeamAssignments(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val teamResult = databaseRepository.getAssignedTeamDetails(userId)
            if (teamResult.isSuccess) {
                _teamDetail.value = teamResult.getOrNull() ?: emptyList()
            } else {
                _teamDetail.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    // Fetch User Outlet Assignments
    fun fetchUserOutletAssignments(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val outletResult = databaseRepository.getAssignedOutletDetails(userId)
            if (outletResult.isSuccess) {
                _outletDetail.value = outletResult.getOrNull()
            } else {
                _outletDetail.value = null
            }
            _isLoading.value = false
        }
    }
}