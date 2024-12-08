package com.example.firebaseexample.data.repository

import QuizViewModel
import android.icu.text.SimpleDateFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseexample.data.model.Problem
import com.example.firebaseexample.data.model.QuizCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
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
        results: List<Map<String, Any>>
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

        // 각각의 컬렉션에 저장
        saveToFirestore("solved", solvedQuizzes, categoryName, addDatePath = false) // 정답 저장
        saveToFirestore("wrong", wrongQuizzes, categoryName, addDatePath = false)  // 오답 저장

        // 날짜별 저장
        saveToFirestore("solved", solvedQuizzes, categoryName, addDatePath = true) // 날짜별 정답 저장
        saveToFirestore("wrong", wrongQuizzes, categoryName, addDatePath = true)  // 날짜별 오답 저장
    }

    suspend fun checkWrongAnswers(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val subjects = listOf("운영체제", "네트워크", "컴퓨터구조", "자료구조", "알고리즘", "데이터베이스")
        var totalWrongAnswers = 0

        for (subject in subjects) {
            val documentSnapshot = userId?.let {
                db.collection("users")
                    .document(it)
                    .collection("wrong")
                    .document(subject)
                    .get()
                    .await()
            } // 비동기 호출

            if (documentSnapshot != null) {
                if (documentSnapshot.exists()) {
                    val wrongAnswers = documentSnapshot.data?.values?.sumOf { map ->
                        if (map is Map<*, *>) {
                            // 맵의 개수 반환
                            map.size
                        } else {
                            0
                        }
                    } ?: 0
                    totalWrongAnswers += wrongAnswers

                } else {
                    println("문서 '$subject'가 존재하지 않습니다.")
                }
            }
        }

        return totalWrongAnswers >= 15 // 5*3 이상인지 여부 반환
    }
}
