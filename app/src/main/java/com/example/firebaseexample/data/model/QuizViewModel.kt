package com.example.firebaseexample.data.model

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseexample.data.repository.QuizRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.Locale

class QuizViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // 뷰모델에 결과페이지에서 전송하는 정보를 업데이트한다.
    private val _savedResults = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val savedResults: StateFlow<List<Map<String, Any>>> = _savedResults
    private val _solvedDaysMap = MutableStateFlow<Map<String, Set<Int>>>(emptyMap())
    val solvedDaysMap: StateFlow<Map<String, Set<Int>>> = _solvedDaysMap
    val solvedData = mutableListOf<Pair<String, Int>>() // 날짜와 fieldCount 저장
    var totalSolvedQuizzes = mutableIntStateOf(0)
    // 일주일 동안의 데이터를 가져오는 함수
    fun fetchWeeklySolvedCounts(year: Int, month: Int) {
        viewModelScope.launch {
            val solvedCountsMap = mutableMapOf<String, Set<Int>>()

            for (day in 1..31) {
                val dateString = String.format("%04d-%02d-%02d", year, month + 1, day)

                // 날짜가 유효한지 확인
                if (isValidDate(year, month, day)) {
                    try {
                        val solvedDocuments = db.collection("users")
                            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                            .collection("solved")
                            .document(dateString)
                            .get()
                            .await()

                        if (solvedDocuments.exists()) {
                            solvedCountsMap[dateString] = solvedDocuments.data?.keys?.map { it.toInt() }?.toSet()
                                ?: emptySet()
                        }
                    } catch (e: Exception) {
                        println("Error fetching solved counts for $dateString: ${e.message}")
                    }
                }
            }

            // ViewModel 상태 업데이트
            _solvedDaysMap.value = solvedCountsMap
        }
    }

    // Helper: 날짜 유효성 확인 함수
    private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        return try {
            val calendar = java.util.Calendar.getInstance()
            calendar.setLenient(false)
            calendar.set(year, month, day)
            calendar.time // 시간 객체로 변환하여 유효성 확인
            true
        } catch (e: Exception) {
            false
        }
    }
    fun setSavedResults(results: List<Map<String, Any>>) {
        println("이미 저장되어 있다고?: ${_savedResults.value}")
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

    fun updateDB(categoryName: String, quizId: String, isCorrect: Boolean){
        val result = mapOf(
            "categoryName" to categoryName,
            "quizId" to quizId,
            "isCorrect" to isCorrect
        )
        quizResults.add(result)
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

    /*
    private fun fetchTodayQuizzes() {
        val today = getCurrentDate()
        println(today)
        db.collection("todayQuiz").document(today).get()
            .addOnSuccessListener { document ->
                val fetchedQuizzes = document.get("problems") as? List<Map<String, Any>> ?: emptyList()
                println(fetchedQuizzes)
                quizzes.value = fetchedQuizzes
                totalQuestions.value = fetchedQuizzes.size
            }
    }
    */

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

                    // 랜덤으로 5개 선택
                    val randomQuizzes = fetchedQuizzes.shuffled().take(quizNum)
                    println("Random Quizzes: $randomQuizzes")

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

    fun fetchSolvedCountForDate(userId: String, dateString: String, onResult: (Int) -> Unit) {
        val db: FirebaseFirestore = Firebase.firestore
        val dateCollectionRef = db.collection("users").document(userId).collection("date")

        dateCollectionRef.document(dateString).collection("solved")
            .get()
            .addOnSuccessListener { solvedDocuments ->
                // 해당 날짜에 풀린 문제의 개수 반환
                onResult(solvedDocuments.size())
            }
            .addOnFailureListener { exception ->
                println("Error fetching solved count for date $dateString: ${exception.message}")
                onResult(0) // 오류 발생 시 0 반환
            }
    }
    fun fetchLast7DaysSolvedCounts(userId: String, onResult: (Map<String, Int>) -> Unit) {
        val db: FirebaseFirestore = Firebase.firestore
        val solvedCounts = mutableMapOf<String, Int>()

        // 오늘 날짜부터 7일 전까지의 날짜 계산
        val calendar = Calendar.getInstance()
        for (i in 0 until 7) {
            val dateString = String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Firestore에서 해당 날짜의 풀린 문제 개수를 가져옴
            db.collection("users")
                .document(userId)
                .collection("date")
                .document(dateString)
                .collection("solved")
                .get()
                .addOnSuccessListener { solvedDocuments ->
                    solvedCounts[dateString] = solvedDocuments.size()
                    if (solvedCounts.size == 7) {
                        // 모든 데이터를 가져온 후 콜백 호출
                        onResult(solvedCounts)
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching data for $dateString: ${exception.message}")
                    solvedCounts[dateString] = 0 // 오류 발생 시 0으로 처리
                    if (solvedCounts.size == 7) {
                        onResult(solvedCounts)
                    }
                }

            // 날짜를 하루 전으로 이동
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
    }
    fun fetchLast7DaysSolvedCounts(onResult: (List<Pair<String, Int>>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        val solvedData = mutableListOf<Pair<String, Int>>() // 날짜와 fieldCount 저장


        for (i in 0 until 7) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            println("Processing date: $date")

            db.collection("users")
                .document(userId)
                .collection("date")
                .document(date)
                .collection("solved")
                .get()
                .addOnSuccessListener { solvedDocuments ->
                    var fieldCount = 0
                    for (document in solvedDocuments.documents) {
                        fieldCount += document.data?.size ?: 0 // 문서의 필드 개수를 더함
                    }

                    // 데이터 저장
                    solvedData.add(date to fieldCount)

                    // 모두 처리한 경우 결과를 반환하거나 사용할 수 있음
                    if (solvedData.size == 7) {
                        solvedData.sortBy { it.first } // 날짜 순서로 정렬 (필요 시)
                        println("Final solvedData: $solvedData")
                        onResult(solvedData) // 최종 결과 콜백 호출
                    }
                }
                .addOnFailureListener {
                    // 실패 시 fieldCount를 0으로 저장
                    solvedData.add(date to 0)
                    if (solvedData.size == 7) {
                        solvedData.sortBy { it.first } // 날짜 순서로 정렬 (필요 시)
                        println("Final solvedData: $solvedData")
                        onResult(solvedData) // 최종 결과 콜백 호출
                    }
                }

            // 이전 날짜로 이동
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

    }

}

// 퀴즈 소스 타입 정의
enum class QuizSource {
    TODAY,
    CATEGORY,
    BRUSHUP
}
