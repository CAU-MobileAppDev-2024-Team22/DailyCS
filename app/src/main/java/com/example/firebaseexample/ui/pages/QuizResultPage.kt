package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.firebaseexample.data.model.QuizViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebaseexample.ui.components.CircularChart
import com.example.firebaseexample.data.repository.QuizRepository
import com.example.firebaseexample.ui.theme.ButtonGray
import com.example.firebaseexample.ui.theme.LineColor
import com.example.firebaseexample.ui.theme.ThemeBlue
import com.example.firebaseexample.ui.theme.ThemeGray
import com.example.firebaseexample.ui.theme.ThemeGreen
import com.example.firebaseexample.ui.theme.ThemeLightGray
import com.example.firebaseexample.ui.theme.ThemePurple
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
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetSavedResults()
                        viewModel.resetProblemDetails()
                        viewModel.resetQuizResults()
                        navController.navigate("main")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(color = Color.White)
                    .verticalScroll(rememberScrollState()) // 스크롤 가능하게 설정
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 차트
                CircularChart(
                    progress = score.toFloat() / totalQuestions.toFloat(),
                    score = score,
                    totalQuestions = totalQuestions
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 문제별 결과
                savedResults.forEachIndexed { index, result ->
                    val quizId = result["quizId"] as? String ?: "Unknown"
                    val categoryName = result["categoryName"] as? String ?: "Unknown"
                    val isCorrect = result["isCorrect"] as? Boolean ?: false
                    val status = if (isCorrect) "정답" else "오답"

                    val problemDetailsFlow = viewModel.getProblemDetailsFlow(quizId)
                    val problemDetails by problemDetailsFlow.collectAsState()

                    val quizIdx = index + 1

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(1.dp, color = LineColor, shape = RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Q$quizIdx.$status",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .background(
                                        color = if (isCorrect) ThemeBlue else ThemeGray,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White
                            )
                            Button(
                                onClick = {
                                    val isVisible = problemDetailsVisibility[quizId] ?: false
                                    problemDetailsVisibility[quizId] = !isVisible

                                    if (!isVisible) {
                                    viewModel.loadProblemDetails(categoryName, quizId) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ThemeBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (problemDetailsVisibility[quizId] == true) "닫기" else "정답 보기",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (problemDetailsVisibility[quizId] == true) {
                            if (problemDetails != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text = "${problemDetails!!["question"] ?: "질문 없음"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(32.dp)
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val correctAnswer = problemDetails!!["answer"] as? String

                                        (problemDetails!!["options"] as? List<*>)?.forEachIndexed { index, option ->
                                            val isCorrectOption = correctAnswer == option
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isCorrectOption) ThemeBlue else ButtonGray
                                                ),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = "$option",
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                                        color = if (isCorrectOption) Color.White else Color.Black
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "[해설] ${problemDetails!!["comment"] ?: "해설 없음"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                                        )
                                    }
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

                // 버튼
                Button(
                    onClick = {
                        viewModel.resetSavedResults()
                        viewModel.resetProblemDetails()
                        viewModel.resetQuizResults()
                        onRestartQuiz()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeBlue,
                        contentColor = Color.White
                    )
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "메인 페이지로",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
    viewModel.resetQuizState()
}