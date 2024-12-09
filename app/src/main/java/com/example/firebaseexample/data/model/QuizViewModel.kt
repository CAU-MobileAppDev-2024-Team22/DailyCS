package com.example.firebaseexample.data.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseexample.data.repository.QuizRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QuizViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // 뷰모델에 결과페이지에서 전송하는 정보를 업데이트한다.
    private val _savedResults = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val savedResults: StateFlow<List<Map<String, Any>>> = _savedResults

    fun setSavedResults(results: List<Map<String, Any>>) {
        _savedResults.value = results
        println("Saved results to ViewModel: $results")
    }

    // 뷰모델에 저장해둔 값을 기준으로 FireStore에서 문제 정보를 조회
    private val repository = QuizRepository()

    // 문제별 상태 관리 (문제 세부사항)
    private val _problemDetails = mutableMapOf<String, MutableStateFlow<Map<String, Any>?>>()

    // 문제별 상태 Flow 반환
    fun getProblemDetailsFlow(quizId: String): StateFlow<Map<String, Any>?> {
        if (!_problemDetails.containsKey(quizId)) {
            _problemDetails[quizId] = MutableStateFlow(null) // 초기화
        }
        return _problemDetails[quizId]!!
    }

    // quizzes/{categoryName}/problems 하고 넘겨받은 데이터
    fun loadProblemDetails(categoryName: String, quizId: String) {
        repository.fetchProblemDetails(
            categoryName = categoryName,
            quizId = quizId,
            onSuccess = { details ->
                _problemDetails[quizId]?.value = details // 해당 문제 상태 업데이트
            },
            onError = { exception ->
                println("Error loading problem details for $quizId: ${exception.message}")
                _problemDetails[quizId]?.value = null
            }
        )
    }

    fun resetSavedResults() {
        _savedResults.value = emptyList() // 저장된 결과 초기화
        println("Saved results cleared: ${_savedResults.value}")
    }

    fun resetProblemDetails() {
        _problemDetails.clear() // 문제 상태 초기화
    }

    fun resetQuizResults() {
        quizResults.clear() // 문제 상태 초기화
    }

    // 여기까지

    var quizzes = mutableStateOf<List<Map<String, Any>>>(emptyList())
        private set

    var score = mutableStateOf(0)
        private set

    var solvedQuizzesNum = mutableStateOf(0)
        private set

    var totalQuestions = mutableStateOf(0)
        private set

    var isButtonEnabled = mutableStateOf(false)
        private set

    var brushUpCategory = mutableStateOf("")
        private set

    var todayCategory = mutableStateOf("")
        private set

    var solvedCounts = mutableStateOf<Map<String, Int>>(emptyMap())
        private set
  
    private val quizResults = mutableListOf<Map<String, Any>>()

    fun checkWrongAnswers() {
        viewModelScope.launch {
            isButtonEnabled.value = repository.checkWrongAnswers(this@QuizViewModel)
        }
    }

    fun checkSolvedAnswers() {
        viewModelScope.launch {
            repository.checkSolvedAnswers(this@QuizViewModel)
        }
    }

    fun updateDB(categoryName: String, quizId: String, isCorrect: Boolean) {
        val randomQuiz = quizzes.value.find { it["quizId"] == quizId } ?: emptyMap<String, Any>()
        val fixedQuizID = randomQuiz["quizId"]?.toString() ?: quizId

        val result = mapOf(
            "categoryName" to categoryName,
            "quizId" to fixedQuizID, // 랜덤하게 재설정된 ID 사용
            "isCorrect" to isCorrect
        )
        quizResults.add(result)
        println("Updated DB with Result: $result")
    }

    fun getAllResults() : List<Map<String, Any>>{
        return quizResults.toList()
    }
    // 데이터 소스 설정 (오늘의 퀴즈 또는 카테고리별 퀴즈)
    fun fetchQuizzes(source: QuizSource, categoryId: String? = null) {
        when (source) {
            QuizSource.TODAY -> fetchRandomQuizzes(categoryId ?: "", quizNum = 10)
            QuizSource.CATEGORY -> fetchCategoryQuizzes(categoryId ?: "")
            QuizSource.BRUSHUP -> fetchRandomQuizzes(categoryId ?: "", quizNum = 5)
        }
    }

    fun fetchTodayCategory() {
        val today = getCurrentDate()
        println("Fetching today's quizzes for date: $today")
        db.collection("todayQuiz").document(today).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // `problems` 배열 필드와 카테고리 가져오기
                    val fetchedQuizzes = document.get("problems") as? List<Map<String, Any>> ?: emptyList()
                    val categoryId = document.getString("category") // 카테고리 가져오기
                    println("Fetched Quizzes: $fetchedQuizzes")
                    quizzes.value = fetchedQuizzes
                    totalQuestions.value = fetchedQuizzes.size

                    // 카테고리가 null이 아니면 랜덤 문제 가져오기
                    if (categoryId != null) {
                        todayCategory.value = categoryId
                    } else {
                        println("Category ID is null.")
                    }
                } else {
                    println("Document does not exist.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching today's quizzes: ${exception.message}")
            }
    }

    private fun fetchCategoryQuizzes(categoryId: String) {
        println("Category ID: $categoryId")
        db.collection("quizzes").document(categoryId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // `problems` 배열 필드 가져오기
                    val fetchedQuizzes = document.get("problems") as? List<Map<String, Any>> ?: emptyList()
                    println("Fetched Quizzes: $fetchedQuizzes")

                    // 상태 업데이트
                    quizzes.value = fetchedQuizzes
                    totalQuestions.value = fetchedQuizzes.size
                } else {
                    println("Document does not exist.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching quizzes: ${exception.message}")
            }
    }

    fun fetchRandomQuizzes(categoryId: String, quizNum: Int) {
        println("Category ID for Random: $categoryId")
        db.collection("quizzes").document(categoryId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // `problems` 배열 필드 가져오기
                    val fetchedQuizzes = document.get("problems") as? List<Map<String, Any>> ?: emptyList()
                    println("Fetched Quizzes for Random: $fetchedQuizzes")

                    // 각 퀴즈에 원본 인덱스 추가
                    val indexedQuizzes = fetchedQuizzes.mapIndexed { index, quiz ->
                        quiz.toMutableMap().apply {
                            put("quizId", index.toString()) // 랜덤 ID를 설정
                        }
                    }

                    // 랜덤으로 선택
                    val randomQuizzes = indexedQuizzes.shuffled().take(quizNum)
                    println("Random Quizzes with Updated IDs: $randomQuizzes")

                    // 상태 업데이트
                    quizzes.value = randomQuizzes
                    totalQuestions.value = randomQuizzes.size
                } else {
                    println("Document does not exist.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching quizzes: ${exception.message}")
            }
    }

    fun updateSolvedQuzzesNum(){
        solvedQuizzesNum.value++
    }

    fun updateScore(isCorrect: Boolean) {
        if (isCorrect) {
            score.value++
        }
    }

    fun resetQuizState() {
        quizzes.value = emptyList()
        score.value = 0
        totalQuestions.value = 0
        solvedQuizzesNum.value = 0
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$year-$month-$day" // YYYY-MM-DD 형식
    }
}

// 퀴즈 소스 타입 정의
enum class QuizSource {
    TODAY,
    CATEGORY,
    BRUSHUP
}
