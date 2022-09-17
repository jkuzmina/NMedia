package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.UserAuthResult
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException

class SignUpViewModel: ViewModel() {
    val name: MutableLiveData<String> = MutableLiveData<String>()
    val login: MutableLiveData<String> = MutableLiveData<String>()
    val pass: MutableLiveData<String> = MutableLiveData<String>()


    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState>
        get() = _authState

    private val _userAuthResult = SingleLiveEvent<UserAuthResult>()
    val userAuthResult: LiveData<UserAuthResult>
        get() = _userAuthResult

    fun signUp() = viewModelScope.launch{
        try {
            val authResult = registerUser(name.value!!.toString(), login.value!!.toString().trim(), pass.value!!.toString().trim())
            if(authResult != null){
                _authState.value = authResult
                _userAuthResult.value = UserAuthResult()
            }
        } catch (e: Exception) {
            _userAuthResult.value = UserAuthResult(error = true)
        }
    }

    suspend fun registerUser(name:String, login: String, pass: String): AuthState {
        try {

            val response = PostsApi.retrofitService.registerUser(login, pass, name)
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