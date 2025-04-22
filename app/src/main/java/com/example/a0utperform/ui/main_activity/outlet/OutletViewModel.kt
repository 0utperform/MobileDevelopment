package com.example.a0utperform.ui.main_activity.outlet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutletViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _outlets = MutableLiveData<List<OutletDetail>>()
    val outlets: LiveData<List<OutletDetail>> = _outlets

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getUserRole(): Flow<String?> = userPreference.getSession().map { it.role }

    fun fetchOutlets() {
        viewModelScope.launch {
            _isLoading.postValue(true)

            userPreference.getSession().collect { user ->
                val userId = user.userId
                val role = user.role

                if (userId.isBlank() || role.isNullOrBlank()) {
                    _error.postValue("Invalid user session or role.")
                    _isLoading.postValue(false)
                    return@collect
                }

                try {
                    when {
                        role.equals("Staff", ignoreCase = true) -> {
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
                        }

                        role.equals("Manager", ignoreCase = true) -> {
                            val result = databaseRepository.getAllOutlets()
                            if (result.isSuccess) {
                                _outlets.postValue(result.getOrNull().orEmpty())
                            } else {
                                _error.postValue(result.exceptionOrNull()?.message)
                                _outlets.postValue(emptyList())
                            }
                        }

                        else -> {
                            _error.postValue("Unknown role: $role")
                            _outlets.postValue(emptyList())
                        }
                    }
                } catch (e: Exception) {
                    _error.postValue(e.message)
                    _outlets.postValue(emptyList())
                } finally {
                    _isLoading.postValue(false)
                }
            }
        }
    }
}