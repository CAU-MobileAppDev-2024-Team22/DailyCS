package com.example.firebaseexample.data.repository

import com.example.firebaseexample.data.model.QuizViewModel
import android.icu.text.SimpleDateFormat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.firebaseexample.data.model.QuizCategory
import com.example.firebaseexample.viewmodel.NickNameViewModel
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

    suspend fun checkSolvedAnswers(
        viewModel: QuizViewModel
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val subjects = listOf("운영체제", "네트워크", "컴퓨터구조", "자료구조", "알고리즘", "데이터베이스")

        // 각 카테고리별 푼 문제 수를 저장할 맵
        val solvedCounts = mutableMapOf<String, Int>()

        println("User ID: $userId")
        for (subject in subjects) {
            val categoryDocument = userId?.let {
                db.collection("users")
                    .document(it)
                    .collection("solved")
                    .document(subject)
                    .get()
                    .await() // 비동기 호출
            }

            if (categoryDocument != null) {
                if (categoryDocument.exists()) {
                    // 필드 수를 세기
                    val solvedAnswersCount = categoryDocument.data?.size ?: 0

                    // 카테고리와 푼 문제 수 저장
                    solvedCounts[subject] = solvedAnswersCount
                    println("Total solved answers for $subject: $solvedAnswersCount")
                } else {
                    println("Document for '$subject' does not exist.")
                }
            }
        }

        // 뷰모델에 카테고리별 푼 문제 수 저장
        viewModel.solvedCounts.value = solvedCounts // 뷰모델에 저장
        println("Solved counts per category: $solvedCounts")
    }

    suspend fun checkWrongAnswers(
        viewModel: QuizViewModel
    ): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val subjects = listOf("운영체제", "네트워크", "컴퓨터구조", "자료구조", "알고리즘", "데이터베이스")
        var totalWrongAnswers = 0
        var maxWrongCategory = 0
        println(userId)
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
                    val wrongAnswers = documentSnapshot.data?.size ?: 0

                    if(maxWrongCategory < wrongAnswers){
                        maxWrongCategory = wrongAnswers
                        viewModel.brushUpCategory.value = subject
                    }
                    totalWrongAnswers += wrongAnswers
                    println("total wrong: $totalWrongAnswers")
                } else {
                    println("문서 '$subject'가 존재하지 않습니다.")
                }
            }

        }
        println(viewModel.brushUpCategory.value)
        println(maxWrongCategory)
        return totalWrongAnswers >= 5 // wrong 문제가 5개 이상인지 여부 반환
    }

    // FireStore 에서 quizzes/{categoryName}/problems 까지 불러옴
    fun fetchProblemDetails(
        categoryName: String,
        quizId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val documentRef = db.collection("quizzes").document(categoryName)

        documentRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val problems = document.get("problems") as? List<Map<String, Any>>
                    val problem = problems?.get(quizId.toIntOrNull() ?: -1)
                    onSuccess(problem) // 문제 데이터를 그대로 반환
                } else {
                    onSuccess(null) // 문서가 존재하지 않는 경우
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // 닉네임 설정 후 닉네임 저장 로직
    fun saveNickname(
        userId: String,
        nickname: String,
        nickNameViewModel: NickNameViewModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection("users").document(userId)

        // 닉네임을 Firestore에 저장
        userRef.set(
            mapOf("nickname" to nickname),
            SetOptions.merge() // 기존 데이터와 병합
        ).addOnSuccessListener {
            println("Nickname saved successfully!")
            // Firestore 저장 성공 시 뷰모델에 닉네임 업데이트
            nickNameViewModel.setNickname(nickname)
            onSuccess()
        }.addOnFailureListener { exception ->
            println("Error saving nickname: ${exception.message}")
            onError(exception)
        }
    }

    // 로그인 시 닉네임 불러오기
    fun fetchNickname(
        userId: String,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname")
                    onSuccess(nickname) // 닉네임 반환
                } else {
                    onSuccess(null) // 닉네임이 없으면 null 반환
                }
            }
            .addOnFailureListener { exception ->
                onError(exception) // 에러 처리
            }
    }
}
