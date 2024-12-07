import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.firebaseexample.ui.pages.ErrorPage
import com.example.firebaseexample.viewmodel.TodayQuizViewModel
import kotlinx.coroutines.delay

@Composable
fun TodayQuizPage(
    viewModel: TodayQuizViewModel = hiltViewModel(),
    onFinishQuiz: (Int) -> Unit, // 점수를 전달
    onTimeout: () -> Unit // 에러 페이지로 이동할 콜백
) {
    val quizzes by viewModel.todayQuizzes.collectAsState()
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isTimeout by remember { mutableStateOf(false) } // 타임아웃 상태

    // 로딩 시간 초과 처리
    LaunchedEffect(key1 = quizzes.isEmpty()) {
        if (quizzes.isEmpty()) {
            delay(5000) // 5초 대기
            if (quizzes.isEmpty()) {
                isTimeout = true
                onTimeout() // 에러 페이지로 이동
            }
        }
    }

    if (isTimeout) {
        // 타임아웃이 발생했을 때 ErrorPage 표시
        ErrorPage(
            onRetry = { viewModel.fetchTodayQuiz() },
            onGoToMain = onTimeout // 메인 페이지로 이동
        )
    } else if (quizzes.isEmpty()) {
        // 로딩 중일 때 LoadingAnimation 표시
        LoadingAnimation()
    } else {
        // 퀴즈 화면 표시
        val currentQuiz = quizzes[currentIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 문제 번호 및 질문 표시
            Text(
                text = "문제 ${currentIndex + 1} / ${quizzes.size}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = currentQuiz["question"] as String,
                style = MaterialTheme.typography.bodyLarge
            )

            // 선택지 표시
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val options = currentQuiz["options"] as List<String>
                options.forEach { option ->
                    Button(
                        onClick = { selectedOption = option },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOption == option) Color.Gray else Color.LightGray
                        )
                    ) {
                        Text(text = option)
                    }
                }
            }

            // 제출 버튼
            Button(
                onClick = {
                    if (selectedOption == currentQuiz["answer"]) score++
                    if (currentIndex < quizzes.size - 1) {
                        currentIndex++
                        selectedOption = null
                    } else {
                        onFinishQuiz(score) // 퀴즈 종료
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "다음 문제")
            }
        }
    }
}
