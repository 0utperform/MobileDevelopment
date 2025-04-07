package com.example.a0utperform.ui.dashboard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.datastore.UserModel
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

    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            userPreference.logout()
            _signOutState.postValue(true)
        }
    }
    fun linkGoogleAccount(onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val uri = repository.getGoogleLinkUri()
                onResult(uri)
            } catch (e: Exception) {
                Log.e("LinkGoogle", "Error creating link URI", e)
                onResult(null)
            }
        }
    }
}