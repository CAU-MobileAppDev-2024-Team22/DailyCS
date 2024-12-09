import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseexample.data.model.QuizCategory
import com.example.firebaseexample.data.model.QuizViewModel
import com.example.firebaseexample.ui.theme.Purple40
import com.example.firebaseexample.ui.theme.ThemeBlue
import com.example.firebaseexample.ui.theme.ThemeGreen
import com.example.firebaseexample.ui.theme.ThemeRed
import com.example.firebaseexample.ui.theme.Typography
import com.example.firebaseexample.viewmodel.QuizListViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QuizListPage(
    onCategoryClick: (String) -> Unit,
    navController: NavController // NavController 추가
) {
    val viewModel: QuizListViewModel = viewModel()
    val quizViewModel: QuizViewModel = viewModel()
    val quizCategories by viewModel.quizCategories.collectAsState()

    // 로딩 상태를 확인하기 위한 추가 플래그
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // 로딩 상태 타이머 (예: 3초 이상 로드되지 않으면 에러 화면 표시)
    LaunchedEffect(Unit) {

        while (isLoading) {
            if (quizCategories.isNotEmpty()) {
                isLoading = false
                break
            }
            delay(200) // 데이터 확인 주기
        }

        // 3초 이상 데이터가 로드되지 않으면 에러 처리
        if (isLoading) {
            delay(3000 - (200 * (3000 / 200))) // 남은 시간 기다림
            if (quizCategories.isEmpty()) {
                isLoading = false
                hasError = true
                navController.navigate("errorPage") // 에러 페이지로 이동
            }
        }

        quizViewModel.checkSolvedAnswers()
//        delay(3000) // 3초 대기
//        if (quizCategories.isEmpty()) {
//            isLoading = false
//            hasError = true // 데이터 로딩 실패 처리
//            navController.navigate("errorPage") // 에러 페이지로 이동
//        } else {
//            isLoading = false
//        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "유형별 문제 풀기",
                        style = Typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("main")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
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
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        quizCategories.forEach { (categoryId, category) ->
                            val solvedCount = quizViewModel.solvedCounts.value[categoryId] ?:0

                            item {
                                QuizCategoryCard(
                                    title = category.title,
                                    score = solvedCount, // 푼 문제로 수정 필요
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
            .clickable { onClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
        ,
        colors = CardDefaults.cardColors(containerColor = Purple40),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp // 기본 그림자 크기
        )
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
                    style = Typography.titleLarge,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${score} / ${prob_num}",
                    style = Typography.bodyLarge,
                    color = ThemeRed,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

