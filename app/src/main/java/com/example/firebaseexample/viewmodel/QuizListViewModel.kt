package com.example.firebaseexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseexample.data.model.QuizCategory
import com.example.firebaseexample.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizListViewModel(
    private val repository: QuizRepository = QuizRepository() // Repository 초기화
) : ViewModel() {

    private val _quizCategories = MutableStateFlow<List<Pair<String, QuizCategory>>>(emptyList())
    val quizCategories: StateFlow<List<Pair<String, QuizCategory>>> = _quizCategories

    init {
        fetchQuizzes()
    }

    private fun fetchQuizzes() {
        viewModelScope.launch {
            repository.fetchAllQuizzes(
                onSuccess = { quizzes -> _quizCategories.value = quizzes },
                onError = { exception -> println("Error: ${exception.message}") }
            )
        }
    }
}
