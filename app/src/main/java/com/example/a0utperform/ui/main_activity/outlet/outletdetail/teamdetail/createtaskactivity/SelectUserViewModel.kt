package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectUserViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _staffList = MutableLiveData<Result<List<StaffData>>>()
    val staffList: LiveData<Result<List<StaffData>>> get() = _staffList

    fun getStaffByTeam(teamId: String) {
        viewModelScope.launch {
            val result = repository.getStaffByTeam(teamId)
            _staffList.postValue(result)
        }
    }
}