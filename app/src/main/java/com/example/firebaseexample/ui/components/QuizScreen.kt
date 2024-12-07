package com.example.firebaseexample.ui.components

import LoadingAnimation
import QuizViewModel
import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onFinishQuiz: (Int) -> Unit,
    onBackPressed: () -> Unit,
    onTimeout: () -> Unit = {}
) {
    val quizzes = viewModel.quizzes.value
    val score = viewModel.score.value
    val totalQuestions = viewModel.totalQuestions.value
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    if (quizzes.isEmpty()) {
        LaunchedEffect(Unit) {
            delay(3000)
            if (quizzes.isEmpty()) {
                onTimeout()
            }
        }
        LoadingAnimation()
        return
    }

    val currentQuiz = quizzes[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("퀴즈", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        QuizCard(
            currentQuiz = currentQuiz,
            currentIndex = currentIndex,
            totalSize = totalQuestions,
            selectedOption = selectedOption,
            onOptionClick = { selectedOption = it },
            onSubmit = {
                if (selectedOption == currentQuiz["answer"]) {
                    viewModel.updateScore(true)
                    println("Correct!")
                }
                if (currentIndex < quizzes.size - 1) {
                    currentIndex++
                    selectedOption = null
                    println(currentIndex)
                } else {
                    println(viewModel.score.value)
                    onFinishQuiz(viewModel.score.value)
                    viewModel.resetQuizState()
                }
            }
        )
    }
}
