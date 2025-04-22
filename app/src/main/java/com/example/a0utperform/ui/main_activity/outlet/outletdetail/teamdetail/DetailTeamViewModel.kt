package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTeamViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

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
}