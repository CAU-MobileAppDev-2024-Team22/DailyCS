import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    // 결과 저장
    var score = mutableIntStateOf(0)
    var totalQuestions = mutableIntStateOf(0)

    fun updateScore(finalScore: Any?, total: Any?) {
        score.intValue = finalScore as Int
        totalQuestions.intValue = total as Int
    }

    // 퀴즈 카테고리, 인덱스 번호 저장
    private val quizResults = mutableListOf<Map<String, Any>>()

    fun addQuizResult(categoryName: String, quizId: String, isCorrect: Boolean) {
        val result = mapOf(
            "categoryName" to categoryName,
            "quizId" to quizId,
            "isCorrect" to isCorrect
        )
        quizResults.add(result)
    }

    fun getAllResults(): List<Map<String, Any>> {
        return quizResults.toList() // 불변 리스트로 반환
    }

    fun clearResults() {
        quizResults.clear()
    }
}