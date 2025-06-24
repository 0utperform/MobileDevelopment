package com.example.a0utperform.ui.main_activity.attendance.create_leave_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateLeaveRequestViewModel @Inject constructor(
    private val repository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _submitStatus = MutableLiveData<Boolean>()
    val submitStatus: LiveData<Boolean> = _submitStatus

    fun submitLeave(
        startTime: String,
        endTime: String,
        reason: String,
        type: String
    ) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            val result = repository.submitLeaveRequest(userId, startTime, endTime, reason, type)
            _submitStatus.value = result
        }
    }
}