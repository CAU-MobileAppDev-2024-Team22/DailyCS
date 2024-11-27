package com.example.firebaseexample.data.model

data class QuizCategory(
    val title: String = "",
    val problems: List<Problem> = emptyList()
)
