package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.viewtaskactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.SubmissionWithEvidence
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewTaskViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _submissionWithEvidenceList = MutableStateFlow<List<SubmissionWithEvidence>>(emptyList())
    val submissionWithEvidenceList: StateFlow<List<SubmissionWithEvidence>> = _submissionWithEvidenceList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    private val _usernameMap = mutableMapOf<String, MutableLiveData<String>>()

    fun getUsername(userId: String): LiveData<String> {
        if (!_usernameMap.containsKey(userId)) {
            val liveData = MutableLiveData<String>()
            _usernameMap[userId] = liveData

            viewModelScope.launch {
                val username = try {
                    repository.getUsernameById(userId)
                } catch (e: Exception) {
                    "Unknown User"
                }
                liveData.postValue(username)
            }
        }
        return _usernameMap[userId]!!
    }


    fun fetchSubmissionsWithEvidence(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val submissionsResult = repository.getSubmissionsByTaskId(taskId)
            if (submissionsResult.isSuccess) {
                val submissions = submissionsResult.getOrThrow()
                val combinedList = mutableListOf<SubmissionWithEvidence>()

                submissions.forEach { submission ->
                    val evidenceResult = repository.getEvidenceBySubmissionId(submission.submission_id)
                    val evidenceList = if (evidenceResult.isSuccess) evidenceResult.getOrThrow() else emptyList()
                    combinedList.add(SubmissionWithEvidence(submission, evidenceList))
                }

                _submissionWithEvidenceList.value = combinedList
            }
            _isLoading.value = false
        }
    }
}