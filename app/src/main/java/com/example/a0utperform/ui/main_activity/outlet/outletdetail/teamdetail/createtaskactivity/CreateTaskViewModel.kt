package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _createResult = MutableLiveData<Boolean>()
    val createResult: LiveData<Boolean> = _createResult

    fun createTask(imageUri: Uri?, task: TaskData) {
        viewModelScope.launch {
            val result = databaseRepository.createTask(task, imageUri)
            _createResult.postValue(result.isSuccess)
        }
    }
}