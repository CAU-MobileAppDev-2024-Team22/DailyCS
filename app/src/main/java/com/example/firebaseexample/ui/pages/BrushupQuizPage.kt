import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseexample.data.model.QuizSource
import com.example.firebaseexample.data.model.QuizViewModel
import com.example.firebaseexample.ui.components.QuizScreen
import com.example.firebaseexample.viewmodel.TodayQuizViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun BrushupQuizPage(
    viewModel: QuizViewModel,
    onFinishQuiz: (Int) -> Unit,
    onBackPressed: () -> Unit,
    onTimeout: () -> Unit,
) {
    val quizzes by viewModel.quizzes

    // 상태를 관찰하며 처리
    LaunchedEffect(Unit) {
        // brushUpCategory가 비어 있다면 기본값 설정
        if (viewModel.brushUpCategory.value.isEmpty()) {
            viewModel.brushUpCategory.value = "defaultCategoryId" // 기본 카테고리 ID
        }

        viewModel.fetchQuizzes(
            source = QuizSource.BRUSHUP,
            categoryId = viewModel.brushUpCategory.value // brushUpCategory 사용
        )

        snapshotFlow { quizzes }
            .takeWhile { it.isEmpty() } // quizzes가 비어 있는 동안만 처리
            .timeout(5000.milliseconds) // 최대 5초 대기
            .onEach {
                // quizzes가 업데이트되면 자동으로 종료
                if (it.isNotEmpty()) return@onEach
            }
            .catch { // 5초 타임아웃 시 처리
                onTimeout()
            }
            .collect()
    }

    QuizScreen(
        viewModel = viewModel,
        onFinishQuiz = onFinishQuiz,
        onBackPressed = onBackPressed,
        categoryName = viewModel.brushUpCategory.value
    )
}
