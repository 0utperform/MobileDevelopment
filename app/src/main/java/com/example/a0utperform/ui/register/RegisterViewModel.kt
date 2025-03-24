package com.example.a0utperform.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.datastore.UserModel
import com.example.a0utperform.data.datastore.UserPreference
import com.example.a0utperform.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPreference: UserPreference  // Inject DataStore helper
) : ViewModel() {

    private val _registerState = MutableLiveData<Result<FirebaseUser?>>()
    val registerState: LiveData<Result<FirebaseUser?>> get() = _registerState

    fun registerUser(email: String, password: String, phone: String) {
        viewModelScope.launch {
            // Register the user using Firebase Auth
            val result = repository.registerUser(email, password, phone)
            if (result.isSuccess) {
                result.getOrNull()?.let { firebaseUser ->
                    // Convert FirebaseUser to your local UserModel.
                    val userModel = UserModel(
                        userId = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Default Name",
                        email = firebaseUser.email ?: "",
                        phone = firebaseUser.phoneNumber?:"",
                        isLogin = true
                    )
                    // Save session locally using DataStore.
                    userPreference.saveSession(userModel)
                }
            }
            _registerState.value = result
        }
    }
}