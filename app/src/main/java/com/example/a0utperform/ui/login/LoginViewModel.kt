package com.example.a0utperform.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.datastore.UserPreference
import com.example.a0utperform.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.a0utperform.data.datastore.UserModel


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userPreference: UserPreference // Inject your DataStore helper
) : ViewModel() {

    private val _loginState = MutableLiveData<Result<FirebaseUser?>>()
    val loginState: LiveData<Result<FirebaseUser?>> get() = _loginState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            if (result.isSuccess) {
                result.getOrNull()?.let { firebaseUser ->
                    // Convert FirebaseUser to your UserModel.
                    val userModel = UserModel(
                        userId = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        phone = firebaseUser.phoneNumber?:"",
                        isLogin = true
                    )
                    // Save session locally using DataStore.
                    userPreference.saveSession(userModel)
                }
            }
            _loginState.value = result
        }
    }
}