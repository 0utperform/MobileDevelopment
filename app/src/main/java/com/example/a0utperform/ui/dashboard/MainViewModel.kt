package com.example.a0utperform.ui.dashboard

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.datastore.UserPreference
import com.example.a0utperform.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPreference: UserPreference // Inject DataStore helper
) : ViewModel() {

    private val _signOutState = MutableLiveData<Boolean>()
    val signOutState: LiveData<Boolean> get() = _signOutState

    fun signOut(context: Context) {
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)
            repository.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            userPreference.logout()
            _signOutState.postValue(true)
        }
    }
}