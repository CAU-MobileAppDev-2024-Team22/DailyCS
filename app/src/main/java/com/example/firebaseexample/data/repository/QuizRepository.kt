package com.example.firebaseexample.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseexample.data.model.Problem
import com.example.firebaseexample.data.model.QuizCategory

class QuizRepository {

    private val db = FirebaseFirestore.getInstance()

    // 모든 퀴즈 카테고리 가져오기
    fun fetchAllQuizzes(
        onSuccess: (List<Pair<String, QuizCategory>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("quizzes").get()
            .addOnSuccessListener { documents ->
                // Firestore에서 문서를 성공적으로 가져온 경우 로그 추가
                println("Firestore Success: ${documents.documents}")

                val quizzes = documents.map { document ->
                    val categoryId = document.id
                    val category = document.toObject(QuizCategory::class.java)
                    println("Mapped category: $categoryId -> $category") // 데이터 매핑 확인
                    categoryId to category
                }
                onSuccess(quizzes)
            }
            .addOnFailureListener { exception ->
                // Firestore에서 실패한 경우 로그 추가
                println("Firestore Error: ${exception.message}")
                onError(exception)
            }
    }


    // 특정 카테고리 가져오기
    fun fetchQuizByCategory(
        categoryId: String,
        onSuccess: (QuizCategory?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("quizzes").document(categoryId).get()
            .addOnSuccessListener { document ->
                val category = document.toObject(QuizCategory::class.java)
                onSuccess(category)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
