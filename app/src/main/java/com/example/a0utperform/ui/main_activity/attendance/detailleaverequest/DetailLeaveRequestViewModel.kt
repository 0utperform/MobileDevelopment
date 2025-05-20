package com.example.a0utperform.ui.main_activity.attendance.detailleaverequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailLeaveRequestViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    fun fetchUsername(userId: String) {
        viewModelScope.launch {
            val name = repository.getUsernameById(userId)
            _username.value = name
        }
    }
}