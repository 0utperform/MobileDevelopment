package com.example.a0utperform.ui.main_activity.outlet.outletdetail

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
class CreateOutletViewModel @Inject constructor(
    private val repository: DatabaseRepository
) : ViewModel() {

    private val _outletCreationStatus = MutableLiveData<Result<Unit>>()
    val outletCreationStatus: LiveData<Result<Unit>> = _outletCreationStatus

    fun createOutlet(name: String, location: String, imageUri: Uri) {
        viewModelScope.launch {
            val result = repository.createNewOutlet(name, location, imageUri)
            _outletCreationStatus.value = result
        }
    }
}