package com.example.a0utperform.ui.main_activity.outlet.outletdetail.addteam

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTeamViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _teamCreationStatus = MutableLiveData<Result<Unit>>()
    val teamCreationStatus: LiveData<Result<Unit>> = _teamCreationStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun createTeam(name: String, desc: String, outletId: String, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createNewTeam(name, desc, outletId, imageUri)
            _teamCreationStatus.value = result
            _isLoading.value = false
        }
    }
}