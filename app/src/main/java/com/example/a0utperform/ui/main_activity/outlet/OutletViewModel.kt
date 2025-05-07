package com.example.a0utperform.ui.main_activity.outlet

import android.util.Log
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
                if (user == null || user.userId.isBlank() || user.role.isNullOrBlank()) {
                    _error.postValue("Invalid user session or role.")
                    _isLoading.postValue(false)
                    return@collect
                }

                val userId = user.userId
                val role = user.role

                Log.d("OutletViewModel", "Fetching outlets for userId=$userId, role=$role")

                try {
                    val result = when {
                        role.equals("Staff", ignoreCase = true) -> {
                            databaseRepository.getAssignedOutletDetails(userId)
                        }

                        role.equals("Manager", ignoreCase = true) -> {
                            databaseRepository.getAllOutlets()
                        }

                        else -> {
                            _error.postValue("Unknown role: $role")
                            _outlets.postValue(emptyList())
                            return@collect
                        }
                    }

                    if (result.isSuccess) {
                        val list = result.getOrNull().orEmpty()
                        Log.d("OutletViewModel", "Fetched ${list.size} outlets")
                        _outlets.postValue(list)
                    } else {
                        val message = result.exceptionOrNull()?.message
                        Log.e("OutletViewModel", "Error fetching outlets: $message")
                        _error.postValue(message)
                        _outlets.postValue(emptyList())
                    }
                } catch (e: Exception) {
                    Log.e("OutletViewModel", "Exception: ${e.message}", e)
                    _error.postValue(e.message)
                    _outlets.postValue(emptyList())
                } finally {
                    _isLoading.postValue(false)
                }
            }
        }
    }
}