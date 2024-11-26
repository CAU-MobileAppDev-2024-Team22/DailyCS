package com.example.firebaseexample.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    // FirebaseAuth에서 현재 사용자가 있는지 확인
    val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    fun setLoggedIn(loggedIn: Boolean) {
        if (!loggedIn) {
            firebaseAuth.signOut()
        }
    }
}
