package com.example.firebaseexample.ui.pages

import CalendarPage
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.Alignment
import com.example.firebaseexample.ui.components.BottomNavigationBar
import com.example.firebaseexample.ui.components.QuizCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    onLogout: () -> Unit,
    goToQuizListPage: ()->Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "푸앙", style = MaterialTheme.typography.titleLarge) },
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
                    .fillMaxWidth() // 가로 크기를 화면에 맞춤
                    .padding(0.dp), // 내부 패딩 제거
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                CalendarPage() // 달력을 카드 안에 포함
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 카드 섹션
            QuizCard(
                title = "오늘의 퀴즈",
                subtitle = "10문제",
                tag = "운영체제",
                time = "3 min",
                backgroundColor = Color(0xFF3D8A74)
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuizCard(
                title = "복습 추천 문제",
                subtitle = "5문제",
                tag = "알고리즘",
                time = "2 min",
                backgroundColor = Color(0xFF5D5D5D)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ButtonCard(
                title = "유형별 문제 풀기",
                backgroundColor = Color(0xFF9084FF),
                onClick = { goToQuizListPage() }
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
            .height(80.dp), // 버튼 높이를 80dp로 설정
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp) // 모서리 둥글기를 12dp로 설정
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp), // 텍스트와 버튼 경계 사이의 여백
            verticalAlignment = Alignment.CenterVertically // 수직 가운데 정렬
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically) // 수직 가운데 정렬
            )
        }
    }
}
