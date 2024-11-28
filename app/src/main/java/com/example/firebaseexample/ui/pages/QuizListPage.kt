package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseexample.data.model.QuizCategory
import com.example.firebaseexample.viewmodel.QuizListViewModel

@Composable
fun QuizListPage(
    onCategoryClick: (String) -> Unit
) {
    val viewModel: QuizListViewModel = viewModel()
    val quizCategories by viewModel.quizCategories.collectAsState()

    // 디버깅 로그
    println("Fetched quiz categories: $quizCategories")

    if (quizCategories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "퀴즈 데이터를 불러올 수 없습니다.")
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp), // 상단 고정 여백
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                quizCategories.forEach { (categoryId, category) ->
                    item {
                        QuizCategoryCard(
                            title = category.title,
                            score = category.problems.size,
                            prob_num = category.problems.size,
                            onClick = { onCategoryClick(categoryId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizCategoryCard(
    title: String,
    score: Int,
    prob_num: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }, // 클릭 이벤트 처리
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
            // 왼쪽: 카테고리 제목
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            // 오른쪽: 점수 및 총 문제 수
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${score} / ${prob_num}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
