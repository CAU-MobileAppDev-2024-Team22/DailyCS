package com.example.firebaseexample.data.model

data class Problem(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = ""
)
