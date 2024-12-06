import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.firebaseexample.data.model.Problem
import com.example.firebaseexample.data.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPage(
    categoryId: String,
    onFinishQuiz: (Any?, Any?) -> Unit,
    onBackPressed: () -> Unit // 뒤로 가기 콜백 추가
) {
    val repository = QuizRepository()
    var problems by remember { mutableStateOf<List<Problem>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val quizViewModel: QuizViewModel = viewModel()

    // Firestore에서 문제 가져오기
    LaunchedEffect(Unit) {
        repository.fetchQuizByCategory(
            categoryId = categoryId,
            onSuccess = { quizCategory ->
                title = quizCategory?.title ?: "Unknown"
                problems = quizCategory?.problems ?: emptyList()
            },
            onError = { exception ->
                println("Error fetching problems: ${exception.message}")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "오늘의 퀴즈",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        if (problems.isNotEmpty()) {
            val currentProblem = problems[currentIndex]

            // 문제 표시
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 문제 카드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, bottom = 16.dp) // 위쪽과 아래쪽 패딩 추가
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // 카드 높이를 제한
                            .shadow(8.dp, RoundedCornerShape(12.dp)) // 그림자 효과
                    ) {
                        // 카드 내부 레이아웃
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 10.dp)
                        ) {
                            // 문제 번호 상자
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter) // 카드 상단 중앙에 배치
                                    .background(
                                        color = Color(0xFFEEF5E6),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp) // 내부 여백
                            ) {
                                Text(
                                    text = "문제 ${currentIndex + 1}/${problems.size}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = Color(0xFF6D6D6D),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // 문제 내용
                            Text(
                                text = currentProblem.question,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                ),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(20.dp), // 카드의 중앙에 배치
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                // 옵션 리스트
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentProblem.options.forEach { option ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedOption == option) Color(0xFFABD1C6) else Color(0xFFE7E7E7)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOption = option
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                    color = Color.Black
                                )
                                RadioButton(
                                    selected = selectedOption == option,
                                    onClick = { selectedOption = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF3D8A74),
                                        unselectedColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                // 제출 버튼
                Button(
                    onClick = {
                        val isCorrect = selectedOption == currentProblem.answer

                        if (isCorrect) {
                            score++
                            println("[SCORE]:$score")
                        }
                        // 뷰모델에 데이터 저장
                        quizViewModel.addQuizResult(
                            categoryName = title,
                            quizId = currentIndex.toString(),
                            isCorrect = isCorrect
                        )

                        if (currentIndex < problems.size - 1) {
                            currentIndex++
                            selectedOption = null // 선택 초기화
                        } else {
                            // 모든 문제를 푼 후 Firestore로 데이터 전송
                            repository.saveAllQuizResults(
                                userId = currentUser ?: "",
                                categoryName = title,
                                results = quizViewModel.getAllResults()
                            )
                            quizViewModel.clearResults() // 뷰모델 데이터 초기화
                            onFinishQuiz(score, problems.size)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D8A74))
                ) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                }
            }
        } else {
            // 로딩 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
