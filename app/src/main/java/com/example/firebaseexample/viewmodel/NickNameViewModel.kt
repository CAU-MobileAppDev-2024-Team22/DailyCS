package com.example.firebaseexample.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NickNameViewModel : ViewModel() {
    private val _nickname = MutableStateFlow<String?>("로딩중...")
    val nickname: StateFlow<String?> get() = _nickname

    fun setNickname(nickname: String?) {
        _nickname.value = nickname
        println("[닉네임 뷰모델 저장 완료! ${_nickname.value}]")
    }
}