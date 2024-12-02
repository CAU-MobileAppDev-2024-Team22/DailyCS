package com.example.firebaseexample.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseexample.data.model.Problem
import com.example.firebaseexample.data.model.QuizCategory
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions

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

    // 퀴즈 결과 저장하기
    fun saveQuizResult(
        userId: String,
        categoryName: String,
        quizId: String,
        isCorrect: Boolean
    ) {
        val db = FirebaseFirestore.getInstance()
        val collectionName = if (isCorrect) "solvedQuizzes" else "wrongQuizzes"
        // 저장할 데이터 구성
        val quizResult = mapOf(
            "isCorrect" to isCorrect
        )

        // Firestore 경로 설정
        val categoryRef = db.collection("users")
            .document(userId)
            .collection(collectionName)
            .document(categoryName) // 카테고리 이름이 문서 ID

        // quizId를 카테고리 문서의 하위 필드로 저장
        categoryRef.update(mapOf(quizId to quizResult))
            .addOnSuccessListener {
                println("Quiz result saved successfully under category $categoryName with ID: $quizId")
            }
            .addOnFailureListener { exception ->
                // 문서가 없는 경우 새로 생성
                if (exception.message?.contains("No document to update") == true) {
                    categoryRef.set(mapOf(quizId to quizResult)) // 새 문서 생성
                        .addOnSuccessListener {
                            println("New category created and quiz result saved under category $categoryName with ID: $quizId")
                        }
                        .addOnFailureListener { setException ->
                            println("Error creating category and saving quiz result: ${setException.message}")
                        }
                } else {
                    println("Error saving quiz result: ${exception.message}")
                }
            }
    }
}
