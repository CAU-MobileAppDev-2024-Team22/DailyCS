package com.example.firebaseexample.ui.components

import LoadingAnimation
import QuizViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebaseexample.data.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onFinishQuiz: (Int) -> Unit,
    onBackPressed: () -> Unit,
    onTimeout: () -> Unit = {},
    categoryName: String,
) {
    val repository = QuizRepository()
    val quizzes = viewModel.quizzes.value
    val score = viewModel.score.value
    val totalQuestions = viewModel.totalQuestions.value
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var ans = true
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid

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
                title = { Text(categoryName, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.solvedQuizzesNum.value == 0){
                            onBackPressed()
                        }
                        else{
                            onFinishQuiz(viewModel.score.value)
                            repository.saveAllQuizResults(
                                userId = currentUser ?: "",
                                categoryName = categoryName,
                                results = viewModel.getAllResults()
                            )
                            viewModel.resetQuizState()
                        }
                         }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // TopAppBar의 높이만큼 패딩 적용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // TopAppBar와 겹치지 않도록 패딩 추가
                .padding(16.dp), // 추가적인 내부 여백
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuizCard(
                currentQuiz = currentQuiz,
                currentIndex = currentIndex,
                totalSize = totalQuestions,
                selectedOption = selectedOption,
                onOptionClick = { selectedOption = it },
                onSubmit = {
                    viewModel.updateSolvedQuzzesNum()
                    if (selectedOption == currentQuiz["answer"]) {
                        viewModel.updateScore(true)
                        ans = true
                    } else {
                        ans = false
                    }
                    viewModel.updateDB(categoryName, currentIndex.toString(), ans)
                    if (currentIndex < quizzes.size - 1) {
                        currentIndex++
                        selectedOption = null
                    } else {
                        onFinishQuiz(viewModel.score.value)
                        repository.saveAllQuizResults(
                            userId = currentUser ?: "",
                            categoryName = categoryName,
                            results = viewModel.getAllResults()
                        )
                        viewModel.resetQuizState()
                    }
                }
            )
        }
    }
}
