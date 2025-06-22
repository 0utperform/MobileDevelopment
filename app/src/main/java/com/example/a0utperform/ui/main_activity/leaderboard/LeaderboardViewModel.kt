package com.example.a0utperform.ui.main_activity.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.TopStaffItem
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _topOutlets = MutableLiveData<Result<List<OutletDetail>>>()
    val topOutlets: LiveData<Result<List<OutletDetail>>> = _topOutlets

    private val _topTeams = MutableLiveData<Result<List<TeamDetail>>>()
    val topTeams: LiveData<Result<List<TeamDetail>>> = _topTeams

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _topStaff = MutableLiveData<Result<List<TopStaffItem>>>()
    val topStaff: LiveData<Result<List<TopStaffItem>>> = _topStaff

    fun fetchTopStaff() {
        viewModelScope.launch {
            _topStaff.value = repository.getTopStaff()
        }
    }

    fun fetchTopOutlets() {
        viewModelScope.launch {
            _isLoading.value = true
            _topOutlets.value = repository.getTopOutletsByRevenue()
            _isLoading.value = false
        }
    }

    fun fetchTopTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            _topTeams.value = repository.getTopTeamsByCompletion()
            _isLoading.value = false
        }
    }
}
