import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    var score = mutableIntStateOf(0)
    var totalQuestions = mutableIntStateOf(0)

    fun updateScore(finalScore: Any?, total: Any?) {
        score.intValue = finalScore as Int
        totalQuestions.intValue = total as Int
    }
}