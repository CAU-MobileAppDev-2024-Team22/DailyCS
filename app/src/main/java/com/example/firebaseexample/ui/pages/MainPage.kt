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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseexample.data.model.QuizViewModel
import com.example.firebaseexample.data.repository.QuizRepository
import com.example.firebaseexample.ui.components.BottomNavigationBar
import com.example.firebaseexample.ui.theme.ThemeDarkGreen
import com.example.firebaseexample.ui.theme.Typography
import com.example.firebaseexample.viewmodel.NickNameViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    viewModel: QuizViewModel,
    nicknameViewModel: NickNameViewModel,
    onLogout: () -> Unit,
    goToQuizListPage: () -> Unit,
    goToTodayQuizPage: () -> Unit, // 오늘의 퀴즈 페이지로 이동하는 콜백
    goToBrushQuizPage: () -> Unit, // 복습 추천 문제 페이지로 이동하는 콜백
    goToNicknamePage: () -> Unit // 닉네임 페이지로 이동하는 콜백
) {
    var showDialog by remember { mutableStateOf(false) } // 팝업창 상태 관리
    val quizRepository = QuizRepository()
    val nickname by nicknameViewModel.nickname.collectAsState(initial = "로딩중")

    // 틀린 문제 수 체크
    LaunchedEffect(Unit) {
        loadUserNickname(quizRepository, nicknameViewModel)
        viewModel.checkWrongAnswers()
        viewModel.fetchTodayCategory()
        // nickname 을 파이어 스토어에서 읽어온 후, viewmodel에 저장하는 함수 추가
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    nickname?.let {
                        Text(
                            text = it,
                            style = Typography.titleLarge,
                            modifier = Modifier.clickable { goToNicknamePage() } // 클릭 시 닉네임 페이지로 이동
                        )
                    }
                },
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
                tag = viewModel.todayCategory.value,
                time = "3 min",
                backgroundColor = ThemeDarkGreen,
                onClick = { goToTodayQuizPage() } // 클릭 시 오늘의 퀴즈로 이동
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 복습 추천 문제 카드
            QuizCard(
                title = if (viewModel.isButtonEnabled.value) "복습 추천 문제" else "복습 추천 문제(비활성화)",
                subtitle = "5문제",
                tag = viewModel.brushUpCategory.value,
                time = "2 min",
                backgroundColor = Color(0xFF5D5D5D),
                onClick = {
                    if (viewModel.isButtonEnabled.value) {
                        goToBrushQuizPage()
                    } else {
                        showDialog = true // 팝업창 띄우기
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 유형별 문제 풀기 버튼
            ButtonCard(
                title = "유형별 문제 풀기",
                backgroundColor = Color(0xFF9084FF),
                onClick = { goToQuizListPage() } // 클릭 시 카테고리 퀴즈로 이동
            )
        }

        // 경고 팝업
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("확인")
                    }
                },
                title = { Text("알림") },
                text = { Text("복습 추천 문제를 활성화하려면 조건을 충족해야 합니다.") }
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

fun loadUserNickname(
    quizRepository: QuizRepository,
    nicknameViewModel: NickNameViewModel
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        quizRepository.fetchNickname(
            userId = userId,
            onSuccess = { fetchedNickname ->
                fetchedNickname?.let {
                    nicknameViewModel.setNickname(it) // 닉네임 뷰모델 업데이트
                    println("닉네임 업데이트: $it")
                } ?: run {
                    println("닉네임이 설정되지 않았습니다.")
                }
            },
            onError = { exception ->
                println("닉네임 불러오기 실패: ${exception.message}")
            }
        )
    } else {
        println("사용자 ID를 찾을 수 없습니다. 로그아웃 처리 필요.")
    }
}

