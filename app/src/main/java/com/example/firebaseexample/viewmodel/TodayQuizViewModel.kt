package com.example.firebaseexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayQuizViewModel @Inject constructor() : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _todayQuizzes = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val todayQuizzes: StateFlow<List<Map<String, Any>>> = _todayQuizzes

    // 오늘의 퀴즈 생성
    fun generateTodayQuiz() {
        viewModelScope.launch {
            val today = getCurrentDate() // 오늘 날짜
            val todayRef = db.collection("todayQuiz").document(today)

            // 오늘의 퀴즈가 이미 있는지 확인
            todayRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    // 기존 문제 ID를 랜덤으로 10개 선택
                    db.collection("quizzes").get().addOnSuccessListener { result ->
                        val quizIds = result.documents.map { it.id }
                        val randomQuizIds = quizIds.shuffled().take(10)

                        // Firestore에 저장 (문제 ID 배열만 저장)
                        todayRef.set(mapOf("problems" to randomQuizIds))
                    }
                }
            }
        }
    }

    // 오늘의 퀴즈 가져오기 (문제 ID를 기반으로 상세 정보 로드)
    fun fetchTodayQuiz() {
        viewModelScope.launch {
            val today = getCurrentDate()
            val todayRef = db.collection("todayQuiz").document(today)

            todayRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val problemIds = document.get("problems") as List<String>

                    // 문제 ID를 사용하여 문제 상세 정보 가져오기
                    db.collection("quizzes").whereIn("__name__", problemIds).get()
                        .addOnSuccessListener { result ->
                            val quizzes = result.documents.map { doc ->
                                doc.data!!.toMutableMap().apply {
                                    put("id", doc.id) // ID를 포함시켜 저장
                                }
                            }
                            _todayQuizzes.value = quizzes
                        }
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(year, month, day) // YYYY-MM-DD 형식
    }
}
