package com.example.firebaseexample.ui.pages

import CalendarPage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import com.example.firebaseexample.ui.components.BottomNavigationBar
import com.example.firebaseexample.ui.theme.ThemeDarkGreen
import com.example.firebaseexample.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    onLogout: () -> Unit,
    goToQuizListPage: () -> Unit,
    goToTodayQuizPage: () -> Unit // 오늘의 퀴즈 페이지로 이동하는 콜백
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "푸앙", style = Typography.titleLarge) },
                actions = {
                    IconButton(onClick = { /* 프로필 클릭 이벤트 */ }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp) // 동일한 padding 적용
        ) {
            // 달력 섹션
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp), // 내부 패딩 제거
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                CalendarPage() // 달력을 카드 안에 포함
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 오늘의 퀴즈 카드
            QuizCard(
                title = "오늘의 퀴즈",
                subtitle = "10문제",
                tag = "운영체제",
                time = "3 min",
                backgroundColor = ThemeDarkGreen,
                onClick = { goToTodayQuizPage() } // 클릭 시 오늘의 퀴즈로 이동
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 복습 추천 문제 카드
            QuizCard(
                title = "복습 추천 문제",
                subtitle = "5문제",
                tag = "알고리즘",
                time = "2 min",
                backgroundColor = Color(0xFF5D5D5D),
                onClick = { /* 다른 작업 추가 가능 */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 유형별 문제 풀기 버튼
            ButtonCard(
                title = "유형별 문제 풀기",
                backgroundColor = Color(0xFF9084FF),
                onClick = { goToQuizListPage() } // 클릭 시 카테고리 퀴즈로 이동
            )
        }
    }
}

@Composable
fun QuizCard(
    title: String,
    subtitle: String,
    tag: String,
    time: String,
    backgroundColor: Color,
    onClick: () -> Unit // 클릭 이벤트 추가
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() }, // 클릭 가능하도록 설정
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = time,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 타이틀
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            // 서브타이틀
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun ButtonCard(
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }
    }
}

