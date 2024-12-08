import androidx.compose.runtime.*
import com.example.firebaseexample.data.model.QuizSource
import com.example.firebaseexample.data.model.QuizViewModel
import com.example.firebaseexample.ui.components.QuizScreen
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun TodayQuizPage(
    viewModel: QuizViewModel,
    onFinishQuiz: (Int) -> Unit,
    onBackPressed: () -> Unit,
    onTimeout: () -> Unit,

    ) {
    val quizzes by viewModel.quizzes

    // 상태를 관찰하며 처리
    LaunchedEffect(Unit) {
        viewModel.fetchQuizzes(QuizSource.TODAY)

        snapshotFlow { quizzes }
            .takeWhile { it.isEmpty() } // quizzes가 비어 있는 동안만 처리
            .timeout(5000.milliseconds) // 최대 3초 대기
            .onEach {
                // quizzes가 업데이트되면 자동으로 종료
                if (it.isNotEmpty()) return@onEach
            }
            .catch { // 3초 타임아웃 시 처리
                onTimeout()
            }
            .collect()
    }

    QuizScreen(
        viewModel = viewModel,
        onFinishQuiz = onFinishQuiz,
        onBackPressed = onBackPressed,
        categoryName = "오늘의 퀴즈"
    )
}
