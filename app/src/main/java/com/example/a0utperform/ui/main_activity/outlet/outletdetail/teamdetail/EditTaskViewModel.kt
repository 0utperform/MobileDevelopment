package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.SubmitStatus
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val repository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    // UI state for submission status
    private val _submitStatus = MutableLiveData<SubmitStatus>()
    val submitStatus: LiveData<SubmitStatus> = _submitStatus

    // UserId as StateFlow for better state management
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    init {
        viewModelScope.launch {
            userPreference.getSession().collect { user ->
                _userId.value = user.userId // Safely assign userId to StateFlow
            }
        }
    }

    fun submitTaskEvidence(task: TaskData, imageUris: List<Uri>, description: String) {
        if (_userId.value == null) {
            _submitStatus.value = SubmitStatus.Error("User not logged in")
            return
        }

        _submitStatus.value = SubmitStatus.Loading

        viewModelScope.launch {
            try {
                val userId = _userId.value ?: throw IllegalStateException("User ID is null")

                repository.submitTaskEvidence(task, imageUris, description, userId)

                _submitStatus.value = SubmitStatus.Success
            } catch (e: Exception) {

                _submitStatus.value = SubmitStatus.Error(e.message ?: "Unknown error")
            }
        }
    }
}

