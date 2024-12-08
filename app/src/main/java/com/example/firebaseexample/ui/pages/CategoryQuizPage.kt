import androidx.compose.runtime.*
import com.example.firebaseexample.data.model.QuizSource
import com.example.firebaseexample.data.model.QuizViewModel
import com.example.firebaseexample.ui.components.QuizScreen


@Composable
fun CategoryQuizPage(
    categoryId: String,
    viewModel: QuizViewModel,
    onFinishQuiz: (Int) -> Unit,
    onBackPressed: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.fetchQuizzes(QuizSource.CATEGORY, categoryId)
    }

    QuizScreen(
        viewModel = viewModel,
        onFinishQuiz = onFinishQuiz,
        onBackPressed = onBackPressed,
        categoryName = categoryId
    )
}
