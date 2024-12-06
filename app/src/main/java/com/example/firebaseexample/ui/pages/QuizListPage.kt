import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseexample.data.model.QuizCategory
import com.example.firebaseexample.viewmodel.QuizListViewModel
import kotlinx.coroutines.delay

@Composable
fun QuizListPage(
    onCategoryClick: (String) -> Unit,
    navController: NavController // NavController 추가
) {
    val viewModel: QuizListViewModel = viewModel()
    val quizCategories by viewModel.quizCategories.collectAsState()

    // 로딩 상태를 확인하기 위한 추가 플래그
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // 로딩 상태 타이머 (예: 3초 이상 로드되지 않으면 에러 화면 표시)
    LaunchedEffect(Unit) {
        delay(3000) // 3초 대기
        if (quizCategories.isEmpty()) {
            isLoading = false
            hasError = true // 데이터 로딩 실패 처리
            navController.navigate("errorPage") // 에러 페이지로 이동
        } else {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                // 로딩 화면
                LoadingAnimation()
            }
            else -> {
                // 데이터 화면
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    quizCategories.forEach { (categoryId, category) ->
                        item {
                            QuizCategoryCard(
                                title = category.title,
                                score = category.problems.size, // 푼 문제로 수정 필요
                                prob_num = category.problems.size,
                                onClick = { onCategoryClick(categoryId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizCategoryCard(
    title: String,
    score: Int,
    prob_num: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6F5ACD)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${score} / ${prob_num}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

