package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    val selectedUserIds = mutableSetOf<String>()



    fun createTask(uri: Uri?, task: TaskData) {
        viewModelScope.launch {
            val result = repository.createTask(task, uri)
            if (result.isSuccess) {
                val taskId = result.getOrNull()
                if (taskId != null) {
                    repository.assignUsersToTask(taskId, selectedUserIds.toList())
                    _createResult.postValue(true)
                } else {
                    _createResult.postValue(false)
                }
            } else {
                _createResult.postValue(false)
            }
        }
    }

    private val _createResult = MutableLiveData<Boolean>()
    val createResult: LiveData<Boolean> = _createResult
}
