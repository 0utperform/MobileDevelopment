package com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.UserWithAssignment
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStaffViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<UserWithAssignment>>()
    val users: LiveData<List<UserWithAssignment>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadUsers(outletId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchUsersWithAssignmentStatus(outletId)
            _isLoading.value = false
            result.onSuccess {
                _users.value = it
            }.onFailure {
                Log.e("ViewModel", "Failed to load users", it)
            }
        }
    }

    fun addUserToOutlet(userId: String, outletId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addUserToOutlet(userId, outletId)
            _isLoading.value = false
            onComplete()
        }
    }

    fun removeUserFromOutlet(userId: String, outletId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.removeUserFromOutlet(userId, outletId)
            _isLoading.value = false
            onComplete()
        }
    }
}