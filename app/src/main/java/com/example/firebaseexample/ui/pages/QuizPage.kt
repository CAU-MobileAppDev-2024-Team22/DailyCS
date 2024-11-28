package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebaseexample.data.model.Problem
import com.example.firebaseexample.data.repository.QuizRepository

@Composable
fun QuizPage(
    categoryId: String,
    onFinishQuiz: () -> Unit // 퀴즈가 끝났을 때 호출할 콜백
) {
    val repository = QuizRepository()
    var problems by remember { mutableStateOf<List<Problem>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }

    // Firestore에서 문제 가져오기
    LaunchedEffect(Unit) {
        repository.fetchQuizByCategory(
            categoryId = categoryId,
            onSuccess = { quizCategory ->
                problems = quizCategory?.problems ?: emptyList()
            },
            onError = { exception ->
                println("Error fetching problems: ${exception.message}")
            }
        )
    }

    if (problems.isNotEmpty()) {
        val currentProblem = problems[currentIndex]

        // 문제 표시
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "문제 ${currentIndex + 1}/${problems.size}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = currentProblem.question,
                style = MaterialTheme.typography.bodyLarge
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currentProblem.options.forEach { option ->
                    Button(
                        onClick = {
                            // 정답 확인
                            if (option == currentProblem.answer) {
                                score++
                            }
                            if (currentIndex < problems.size - 1) {
                                currentIndex++
                            } else {
                                onFinishQuiz()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = option)
                    }
                }
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
