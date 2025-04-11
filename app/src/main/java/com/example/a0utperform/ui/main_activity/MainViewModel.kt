package com.example.a0utperform.ui.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.local.datastore.UserPreference
import com.example.a0utperform.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPreference: UserPreference
) : ViewModel() {


    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()
    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            userPreference.logout()
        }
    }


}