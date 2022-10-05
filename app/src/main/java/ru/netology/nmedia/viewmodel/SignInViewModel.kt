package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.UserAuthResult
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {
    val login: MutableLiveData<String> = MutableLiveData<String>()
    val pass: MutableLiveData<String> = MutableLiveData<String>()


    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState>
        get() = _authState
    private val _userAuthResult = SingleLiveEvent<UserAuthResult>()
    val userAuthResult: LiveData<UserAuthResult>
        get() = _userAuthResult

    fun signIn() = viewModelScope.launch{
        try {
            val authResult = updateUser(login.value!!.toString().trim(), pass.value!!.toString().trim())
            _authState.value = authResult
            _userAuthResult.value = UserAuthResult()
        } catch (e: Exception) {
            _userAuthResult.value = UserAuthResult(error = true)
        }
    }

    suspend fun updateUser(login: String, pass: String): AuthState {
        try {

            val response = apiService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}