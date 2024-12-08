package com.example.firebaseexample.ui.pages

import com.example.firebaseexample.data.model.QuizViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebaseexample.ui.components.CircularChart
import com.example.firebaseexample.data.repository.QuizRepository
import com.example.firebaseexample.ui.theme.Typography
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultPage(
    score: Int,                // 사용자 점수
    totalQuestions: Int,       // 전체 문제 수
    onRestartQuiz: () -> Unit, // 다시 퀴즈 풀기 버튼 콜백
    onGoToMainPage: () -> Unit, // 메인 페이지로 이동 버튼 콜백
    viewModel: QuizViewModel,
    navController: NavController // NavController 추가
) {
    val repository = QuizRepository()
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid

    // ViewModel에서 저장된 결과를 가져옴
    val savedResults by viewModel.savedResults.collectAsState()
    val problemDetailsVisibility = remember { mutableStateMapOf<String, Boolean>() } // 문제 상세 보기 상태

    // 저장된 결과를 로그에 출력
    println("Saved Results: $savedResults")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "퀴즈 결과",
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
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 결과 표시
//            Text(
//                text = "퀴즈 완료!",
//                style = MaterialTheme.typography.titleLarge
//            )
//            Text(
//                text = "점수: $score / $totalQuestions",
//                style = MaterialTheme.typography.titleMedium
//            )
                    // 차트
                    CircularChart(
                        progress = score.toFloat() / totalQuestions.toFloat(),
                        score = score,
                        totalQuestions = totalQuestions
                    )

                    // 저장된 결과 출력
                    Text(
                        text = "저장된 결과:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 문제 결과 불러오는 영역
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedResults) { result ->
                            val quizId = result["quizId"] as? String ?: "Unknown"
                            val categoryName = result["categoryName"] as? String ?: "Unknown"
                            val isCorrect = result["isCorrect"] as? Boolean ?: false
                            val status = if (isCorrect) "정답" else "오답"

                            val problemDetailsFlow = viewModel.getProblemDetailsFlow(quizId)
                            val problemDetails by problemDetailsFlow.collectAsState()

                            Column {
                                Text(
                                    text = "문제: $quizId - $status",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(onClick = {
                                    val isVisible = problemDetailsVisibility[quizId] ?: false
                                    problemDetailsVisibility[quizId] = !isVisible

                                    if (!isVisible) {
                                        viewModel.loadProblemDetails(categoryName, quizId)
                                    }
                                }) {
                                    Text(
                                        text = if (problemDetailsVisibility[quizId] == true) "닫기" else "문제 보기",
                                        style = MaterialTheme.typography.bodySmall)
                                }

                                if (problemDetailsVisibility[quizId] == true) {
                                    if (problemDetails != null) {
                                        Text(
                                            text = "질문: ${problemDetails!!["question"] ?: "질문 없음"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                        Text(
                                            text = "정답: ${problemDetails!!["answer"] ?: "정답 없음"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "코멘트: ${problemDetails!!["comment"] ?: "코멘트 없음"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "선택지:",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        (problemDetails!!["options"] as? List<*>)?.forEachIndexed { index, option ->
                                            Text(
                                                text = "$index: $option",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "로딩 중...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 다시 퀴즈 풀기 버튼
                    Button(
                        onClick = {
                            viewModel.resetSavedResults()
                            viewModel.resetProblemDetails() // 문제 상태 초기화
                            viewModel.resetQuizResults()
                            onRestartQuiz()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "다시 풀기",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // 메인 페이지로 이동 버튼
                    Button(
                        onClick = {
                            viewModel.resetSavedResults()
                            viewModel.resetProblemDetails() // 문제 상태 초기화
                            viewModel.resetQuizResults()
                            onGoToMainPage()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "메인 페이지로",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    )
    viewModel.resetQuizState()

}
