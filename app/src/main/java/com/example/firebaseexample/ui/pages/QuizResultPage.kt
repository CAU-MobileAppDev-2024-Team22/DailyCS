package com.example.firebaseexample.ui.pages

import com.example.firebaseexample.data.model.QuizViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebaseexample.ui.components.CircularChart
import com.example.firebaseexample.data.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun QuizResultPage(
    score: Int,                // 사용자 점수
    totalQuestions: Int,       // 전체 문제 수
    onRestartQuiz: () -> Unit, // 다시 퀴즈 풀기 버튼 콜백
    onGoToMainPage: () -> Unit, // 메인 페이지로 이동 버튼 콜백
    viewModel: QuizViewModel
) {
    val repository = QuizRepository()
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid

    // ViewModel에서 저장된 결과를 가져옴
    val savedResults by viewModel.savedResults.collectAsState()
    val problemQuestion by viewModel.problemQuestion.collectAsState()


    // 저장된 결과를 로그에 출력
    println("Saved Results: $savedResults")
    Box(
        modifier = Modifier.fillMaxSize(),
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
            Text(
                text = "퀴즈 완료!",
                style = MaterialTheme.typography.headlineMedium
            )
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

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedResults) { result ->
                    val quizId = result["quizId"] as? String ?: "Unknown"
                    val categoryName = result["categoryName"] as? String ?: "Unknown"
                    val isCorrect = result["isCorrect"] as? Boolean ?: false
                    val status = if (isCorrect) "정답" else "오답"

                    Column {
                        Text(
                            text = "문제: $quizId - $status",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = {
                            viewModel.loadProblemQuestion(categoryName, quizId)
                        }) {
                            Text("문제 보기")
                        }
                    }
                }
            }

            // 문제 조회 결과 표시
            problemQuestion?.let { question ->
                Text(
                    text = "문제 내용: $question",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // 다시 퀴즈 풀기 버튼
            Button(
                onClick = onRestartQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다시 풀기")
            }

            // 메인 페이지로 이동 버튼
            Button(
                onClick = onGoToMainPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("메인 페이지로")
            }
        }
    }
    viewModel.resetQuizState()

}
