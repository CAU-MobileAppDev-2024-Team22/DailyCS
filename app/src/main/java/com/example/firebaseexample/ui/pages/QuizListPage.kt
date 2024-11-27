package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListPage(
    goToMainPage: ()->Unit
) {
    // Raw 데이터를 가져옴
    val categories = getQuizCategories()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("유형별 문제 풀기", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { goToMainPage() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp) // 화면 여백
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp) // 항목 간 간격
        ) {
            // 데이터를 순회하여 카드 컴포넌트 생성
            categories.forEach { category ->
                item {
                    QuizCategoryCard(
                        title = category.title,
                        score = category.score,
                        prob_num = category.prob_num,
                    )
                }
            }
        }
    }
}

// Raw 데이터를 제공하는 함수
fun getQuizCategories(): List<QuizCategory> {
    return listOf(
        QuizCategory("알고리즘", 0, 35),
        QuizCategory("컴퓨터구조", 0, 35),
        QuizCategory("자료구조", 0, 35),
        QuizCategory("운영체제", 0, 35),
        QuizCategory("네트워크", 0, 35),
        QuizCategory("쿠버네티스", 0, 35)
    )
}

// 데이터 클래스
data class QuizCategory(val title: String, val score: Int, val prob_num: Int)

@Composable
fun QuizCategoryCard(title: String, score: Int, prob_num: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // 카드 높이
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6F5ACD)), // 카드 배경색
        shape = MaterialTheme.shapes.medium // 모서리 둥글게
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // 내부 여백
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 카테고리 이름
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            // 점수
            Text(
                text = "${score} / ${prob_num}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.End
            )
        }
    }
}
