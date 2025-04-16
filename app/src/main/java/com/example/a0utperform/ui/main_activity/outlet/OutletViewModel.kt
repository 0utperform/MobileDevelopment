package com.example.a0utperform.ui.main_activity.outlet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutletViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    // LiveData or StateFlow to expose the list of outlets
    private val _outlets = MutableLiveData<List<OutletDetail>>()
    val outlets: LiveData<List<OutletDetail>> = _outlets

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun fetchOutlets() {
        viewModelScope.launch {
            // 1) Get the current user
            userPreference.getSession().collect { user ->
                val userId = user.userId
                val role = user.role // e.g., "Staff" or "Manager"

                if (userId.isBlank() || role.isNullOrBlank()) {
                    _error.postValue("Invalid user session or role.")
                    return@collect
                }

                try {
                    if (role.equals("Staff", ignoreCase = true)) {
                        // For staff, fetch a single assigned outlet
                        val result = databaseRepository.getAssignedOutletDetails(userId)
                        if (result.isSuccess) {
                            result.getOrNull()?.let { assignedOutlet ->
                                _outlets.postValue(listOf(assignedOutlet))
                            } ?: run {
                                _outlets.postValue(emptyList())
                            }
                        } else {
                            _error.postValue(result.exceptionOrNull()?.message)
                            _outlets.postValue(emptyList())
                        }
                    } else if (role.equals("Manager", ignoreCase = true)) {
                        val result = databaseRepository.getAllOutlets()
                        if (result.isSuccess) {
                            _outlets.postValue(result.getOrNull().orEmpty())
                        } else {
                            _error.postValue(result.exceptionOrNull()?.message)
                            _outlets.postValue(emptyList())
                        }

                    } else {
                        // fallback if some other role
                        _error.postValue("Unknown role: $role")
                        _outlets.postValue(emptyList())
                    }
                } catch (e: Exception) {
                    _error.postValue(e.message)
                    _outlets.postValue(emptyList())
                }
            }
        }
    }
}