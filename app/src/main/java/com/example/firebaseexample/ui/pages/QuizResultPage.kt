package com.example.firebaseexample.ui.pages

import QuizViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            Text(
                text = "점수: $score / $totalQuestions",
                style = MaterialTheme.typography.titleMedium
            )

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
