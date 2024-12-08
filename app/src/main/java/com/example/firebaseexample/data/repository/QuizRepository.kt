package com.example.firebaseexample.data.repository

import com.example.firebaseexample.data.model.QuizViewModel
import android.icu.text.SimpleDateFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseexample.data.model.QuizCategory
import com.google.firebase.firestore.SetOptions
import java.util.Date
import java.util.Locale

class QuizRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getCategoryTitle(categoryId: String, onComplete: (String?) -> Unit) {
        db.collection("categories").document(categoryId).get()
            .addOnSuccessListener { document ->
                val title = document.getString("title")
                onComplete(title)
            }
            .addOnFailureListener {
                onComplete(null) // 에러 시 null 반환
            }
    }
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

    // 뷰모델 한 번에 저장
    fun saveAllQuizResults(
        userId: String,
        categoryName: String,
        results: List<Map<String, Any>>,
        viewModel: QuizViewModel
    ) {
        val db = FirebaseFirestore.getInstance()

        // 현재 날짜를 생성
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 결과를 정답과 오답으로 분리
        val solvedQuizzes = results.filter { it["isCorrect"] == true }
        val wrongQuizzes = results.filter { it["isCorrect"] == false }

        // Firestore에 저장하는 함수
        fun saveToFirestore(
            collectionName: String,
            quizzes: List<Map<String, Any>>,
            categoryName: String,
            addDatePath: Boolean
        ) {
            if (quizzes.isNotEmpty()) {
                val documentData = quizzes.associate { quiz ->
                    quiz["quizId"].toString() to mapOf(
                        "isCorrect" to quiz["isCorrect"],
                        "categoryName" to quiz["categoryName"],
                        "date" to currentDate // 각 데이터에 날짜 추가
                    )
                }

                val targetCollectionRef = if (addDatePath) {
                    db.collection("users")
                        .document(userId)
                        .collection("date")
                        .document(currentDate)
                        .collection(collectionName)
                        .document(categoryName) // 날짜/정답 또는 오답/카테고리
                } else {
                    db.collection("users")
                        .document(userId)
                        .collection(collectionName)
                        .document(categoryName) // 정답 또는 오답/카테고리
                }

                targetCollectionRef.set(documentData, SetOptions.merge()) // 병합 옵션으로 저장
                    .addOnSuccessListener {
                        val location = if (addDatePath) "date/$currentDate/$collectionName/$categoryName" else "$collectionName/$categoryName"
                        println("Saved to $location successfully!")
                    }
                    .addOnFailureListener { exception ->
                        println("Error saving to $collectionName/$categoryName: ${exception.message}")
                    }
            }
        }
        // ViewModel에 저장 결과 업데이트
        if (results.isNotEmpty()) {
            viewModel.setSavedResults(results)
        } else {
            println("No results to save to ViewModel.")
        }
        // 각각의 컬렉션에 저장
        saveToFirestore("solved", solvedQuizzes, categoryName, addDatePath = false) // 정답 저장
        saveToFirestore("wrong", wrongQuizzes, categoryName, addDatePath = false)  // 오답 저장

        // 날짜별 저장
        saveToFirestore("solved", solvedQuizzes, categoryName, addDatePath = true) // 날짜별 정답 저장
        saveToFirestore("wrong", wrongQuizzes, categoryName, addDatePath = true)  // 날짜별 오답 저장
    }
}
