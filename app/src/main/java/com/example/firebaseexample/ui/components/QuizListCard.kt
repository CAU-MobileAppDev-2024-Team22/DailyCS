package com.example.firebaseexample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QuizCard(
    title: String,
    subtitle: String,
    tag: String,
    time: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp), // 카드 높이를 사진과 유사하게 조정
//            .padding(4.dp), // 외부 여백
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = MaterialTheme.shapes.medium // 모서리 둥글게
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // 내부 여백
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 태그와 시간 섹션
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = tag,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall, // 태그 텍스트 크기
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = MaterialTheme.shapes.small // 둥근 배경
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp) // 태그 내부 여백
                )
                Text(
                    text = time,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall, // 시간 텍스트 크기
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = MaterialTheme.shapes.small // 둥근 배경
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp) // 시간 내부 여백
                )
            }

            // 타이틀 섹션
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White // 타이틀은 흰색 텍스트
            )

            // 하단 서브타이틀 섹션
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White // 서브타이틀은 흰색 텍스트
            )
        }
    }
}