package com.example.firebaseexample.ui

import CalendarPage
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(onLogout: () -> Unit) {
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
                onClick = { /* 버튼 클릭 이벤트 */ }
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


@Composable
fun ButtonCard(
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { /* Home 클릭 이벤트 */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
            label = { Text("Analytics") },
            selected = false,
            onClick = { /* Analytics 클릭 이벤트 */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { /* Profile 클릭 이벤트 */ }
        )
    }
}