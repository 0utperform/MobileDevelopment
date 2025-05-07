package com.example.a0utperform.ui.main_activity.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.TeamDetail
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

    fun fetchTopOutlets() {
        viewModelScope.launch {
            _topOutlets.value = repository.getTop3OutletsByRevenue()
        }
    }

    fun fetchTopTeams() {
        viewModelScope.launch {
            _topTeams.value = repository.getTop3TeamsByCompletion()
        }
    }
}