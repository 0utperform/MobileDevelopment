package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.edittask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _task = MutableLiveData<TaskData?>()
    val task: LiveData<TaskData?> get() = _task

    fun fetchTaskById(taskId: String) {
        viewModelScope.launch {
            val result = repository.getTaskById(taskId)
            if (result.isSuccess) {
                _task.value = result.getOrNull()
            } else {
                // handle error (log or show UI feedback)
            }
        }
    }
}