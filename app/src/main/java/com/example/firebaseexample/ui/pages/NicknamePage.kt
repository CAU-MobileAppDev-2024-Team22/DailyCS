package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.firebaseexample.data.repository.QuizRepository
import com.example.firebaseexample.ui.theme.LineColor
import com.example.firebaseexample.ui.theme.TextFieldBorder
import com.example.firebaseexample.ui.theme.ThemeBlue
import com.example.firebaseexample.ui.theme.ThemeGray
import com.example.firebaseexample.ui.theme.Typography
import com.example.firebaseexample.viewmodel.NickNameViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknamePage(
    backToMainPage: () -> Unit,
    nicknameViewModel: NickNameViewModel,
    onNicknameRegistered: () -> Unit // 닉네임 등록 후 콜백
) {
    val repository = QuizRepository()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var nickname by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "닉네임 설정",
                        style = Typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { backToMainPage() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "닉네임 설정",
                fontWeight = FontWeight.Bold,
                style = Typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "사용할 닉네임을 입력해주세요.",
                style = Typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nickname,
                onValueChange = { nickname = it },
                label = {
                    Text(
                        text = "Nickname",
                        style = Typography.bodyMedium,
                        color = ThemeGray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeBlue,
                    unfocusedBorderColor = TextFieldBorder
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeBlue,
                    contentColor = Color.White,
                ),
                onClick = {
                    if (nickname.isNotBlank() && userId != null) {
                        repository.saveNickname(
                            userId = userId,
                            nickname = nickname,
                            nickNameViewModel = nicknameViewModel, // 뷰모델 전달
                            onSuccess = {
                                println("닉네임 등록 성공")
                                onNicknameRegistered() // 닉네임 등록 후 콜백 호출
                            },
                            onError = { exception ->
                                println("닉네임 등록 실패: ${exception.message}")
                            }
                        )
                    } else {
                        println("닉네임 또는 사용자 ID가 유효하지 않습니다.")
                    }
                }
            ) {
                Text(
                    text = "닉네임 등록",
                    style = Typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { backToMainPage() }
            ) {
                Text(
                    text = "로그인 페이지로 돌아가기",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = ThemeGray,
                    style = Typography.bodyMedium,
                )
            }
        }
    }
}