package com.example.firebaseexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodayQuizViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _todayQuizzes = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val todayQuizzes: StateFlow<List<Map<String, Any>>> = _todayQuizzes

    // 오늘의 퀴즈 가져오기
    fun fetchTodayQuiz() {
        viewModelScope.launch {
            val today = getCurrentDate() // 오늘 날짜
            val todayRef = db.collection("todayQuiz").document(today)

            todayRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // 안전하게 데이터 가져오기
                    val quizzes = document.get("problems") as? List<Map<String, Any>>
                    if (quizzes != null) {
                        _todayQuizzes.value = quizzes
                    } else {
                        _todayQuizzes.value = emptyList() // null일 경우 빈 리스트로 설정
                    }
                } else {
                    _todayQuizzes.value = emptyList() // 문서가 없을 경우 빈 리스트로 설정
                }
            }.addOnFailureListener { exception ->
                _todayQuizzes.value = emptyList() // 에러 발생 시 빈 리스트로 설정
                println("Error fetching todayQuiz: ${exception.message}")
            }
        }
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$year-$month-$day" // YYYY-MM-DD 형식
    }
}
