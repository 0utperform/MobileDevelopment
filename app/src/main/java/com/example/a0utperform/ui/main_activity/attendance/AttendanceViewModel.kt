package com.example.a0utperform.ui.main_activity.attendance

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _attendanceMap = MutableLiveData<Map<LocalDate, String>>()
    val attendanceMap: LiveData<Map<LocalDate, String>> = _attendanceMap

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedMonth = MutableLiveData<YearMonth>(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedMonth: LiveData<YearMonth> = _selectedMonth

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeMonth(offset: Long) {
        _selectedMonth.value = _selectedMonth.value?.plusMonths(offset)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAttendance() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val userId = repository.getCurrentUserId()
            if (userId == null) {
                _isLoading.postValue(false)
                return@launch
            }

            val month = _selectedMonth.value ?: YearMonth.now()
            val data = repository.getAttendanceByMonth(userId, month)
            _attendanceMap.postValue(data)
            _isLoading.postValue(false)
        }
    }
}
