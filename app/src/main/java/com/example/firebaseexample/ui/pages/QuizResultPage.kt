package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.background
import com.example.firebaseexample.data.model.QuizViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebaseexample.ui.components.CircularChart
import com.example.firebaseexample.data.repository.QuizRepository
import com.example.firebaseexample.ui.theme.ThemeBlue
import com.example.firebaseexample.ui.theme.ThemeGray
import com.example.firebaseexample.ui.theme.ThemeLightGray
import com.example.firebaseexample.ui.theme.Typography
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultPage(
    score: Int,
    totalQuestions: Int,
    onRestartQuiz: () -> Unit,
    onGoToMainPage: () -> Unit,
    viewModel: QuizViewModel,
    navController: NavController
) {
    val repository = QuizRepository()
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid

    val savedResults by viewModel.savedResults.collectAsState()
    val problemDetailsVisibility = remember { mutableStateMapOf<String, Boolean>() }

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
                        viewModel.resetSavedResults()
                        viewModel.resetProblemDetails()
                        viewModel.resetQuizResults()
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
                    CircularChart(
                        progress = score.toFloat() / totalQuestions.toFloat(),
                        score = score,
                        totalQuestions = totalQuestions
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(savedResults) { result ->
                            val quizId = result["quizId"] as? String ?: "Unknown"
                            val categoryName = result["categoryName"] as? String ?: "Unknown"
                            val isCorrect = result["isCorrect"] as? Boolean ?: false
                            val status = if (isCorrect) "정답" else "오답"

                            val problemDetailsFlow = viewModel.getProblemDetailsFlow(quizId)
                            val problemDetails by problemDetailsFlow.collectAsState()

                            val quizIdx = quizId.toInt() + 1

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .background(ThemeGray)
                            ) {
                                HorizontalDivider(
                                    color = ThemeLightGray,
                                    thickness = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                            ) {
                                Text(
                                    text = "Q$quizIdx.",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .background(
                                            color = if (isCorrect) ThemeBlue else ThemeGray,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = Color.White,
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
                    Button(
                        onClick = {
                            viewModel.resetSavedResults()
                            viewModel.resetProblemDetails()
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

                    Button(
                        onClick = {
                            viewModel.resetSavedResults()
                            viewModel.resetProblemDetails()
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
