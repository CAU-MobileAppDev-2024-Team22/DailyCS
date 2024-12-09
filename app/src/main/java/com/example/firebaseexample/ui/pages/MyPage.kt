import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseexample.data.model.QuizCategory
import com.example.firebaseexample.data.model.QuizViewModel
import com.example.firebaseexample.viewmodel.QuizListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPage(
    quizViewModel: QuizViewModel,
    quizListViewModel: QuizListViewModel,
    navController: NavController,
    onBackPressed: () -> Unit
) {
    val solvedCounts = quizViewModel.solvedCounts.value
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var weeklySolvedCounts by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    val quizCategories by quizListViewModel.quizCategories.collectAsState()
    LaunchedEffect(Unit) {
        var elapsedTime = 0
        val maxLoadingTime = 3000 // 3초
        quizViewModel.fetchLast7DaysSolvedCounts { counts ->
            weeklySolvedCounts = counts
        }
        quizViewModel.checkSolvedAnswers()
        println("Weekly Solved Counts: $weeklySolvedCounts")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Page", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            LoadingAnimation()
            println("로딩중!")
        } else if (hasError) {
            // 에러 상태는 별도의 페이지에서 처리하므로 추가 UI가 필요하지 않음
        } else {
            // 정상 UI
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 진척도 섹션
                ProgressSection(quizViewModel.totalSolvedQuizzes.intValue)

                // 유형별 푼 문제 수
                SolvedByCategorySection(quizCategories, quizViewModel)
                Spacer(modifier = Modifier.height(40.dp))
                // 주간 문제풀이 결과
                WeeklySolvedGraphSection(weeklySolvedCounts)
            }
        }
    }
}

@Composable
fun ProgressSection(totalSolved: Int) {
    val rank = when {
        totalSolved < 10 -> "Bronze"
        totalSolved < 20 -> "Silver"
        totalSolved < 30 -> "Gold"
        totalSolved < 40 -> "Platinum"
        else -> "Diamond"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "나의 진척도", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$totalSolved 개 문제", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = rank, style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFA500))
        }
    }
}

@Composable
fun SolvedByCategorySection(solvedCounts: List<Pair<String,QuizCategory>>, quizViewModel: QuizViewModel) {
    println("!!!!!!!!!!!!!!" + solvedCounts)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "유형별 문제 푼 개수",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))


        solvedCounts.forEach { (categoryId, category) ->
            val solvedCount = quizViewModel.solvedCounts.value[categoryId] ?:0
            val categorySize = category.problems.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryId,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1.5f)
                )
                LinearProgressIndicator(
                    progress = {
                        (solvedCount.toFloat() / categorySize.toFloat()) // 최대 35문제로 가정
                    },
                    modifier = Modifier
                        .weight(2.5f)
                        .height(8.dp),
                    color = Color(0xFF3D8A74),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$solvedCount / $categorySize", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun WeeklySolvedGraphSection(weeklySolvedCounts: List<Pair<String, Int>>) {
    val maxSolvedCount = weeklySolvedCounts.maxOfOrNull { it.second } ?: 1 // 최대값 가져오기, 기본값 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "주간 문제풀이 결과",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        // 그래프와 날짜 텍스트 분리
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp), // 그래프 최대 높이 설정
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom // 막대그래프를 바닥에 정렬
        ) {
            weeklySolvedCounts.forEach { (date, count) ->
                val barHeight = if (maxSolvedCount == 0) 10.dp else (count * 130 / maxSolvedCount + 10).dp
                val displayDate = date.substring(5) // "yyyy-MM-dd"에서 "MM-dd" 부분 추출

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 막대 그래프
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(barHeight) // 계산된 높이로 설정
                            .background(Color(0xFF3D8A74), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // 날짜 텍스트
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weeklySolvedCounts.forEach { (date, _) ->
                val displayDate = date.substring(5) // "yyyy-MM-dd"에서 "MM-dd" 부분 추출

                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp) // 텍스트 영역 크기 고정
                )
            }
        }
    }
}
