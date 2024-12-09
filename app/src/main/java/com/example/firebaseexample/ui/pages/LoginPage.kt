package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun LoginPage(
    goToRegisterPage: () -> Unit,
    onLoginSuccess: () -> Unit,
    nickNameViewModel: NickNameViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "", // Login Page
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        )
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
                text = "매일CS",
                fontWeight = FontWeight.Bold,
                style = Typography.titleLarge.copy(fontSize = 28.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "매일 늘려가는 CS 지식 한 걸음",
                style = Typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        text = "E-mail",
                        style = Typography.titleSmall,
                        color = ThemeGray
                    )},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeBlue,
                    unfocusedBorderColor = TextFieldBorder
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = "Password",
                        style = Typography.titleSmall,
                        color = ThemeGray
                    )},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeBlue,
                    unfocusedBorderColor = TextFieldBorder
                ),

            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                onClick = {
                    loginUser(email, password, onLoginSuccess, nickNameViewModel)
                }
            ) {
                Text(
                    text = "로그인",
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp),
                    color = LineColor
                )
                Text(
                    text = "OR",
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = ThemeGray,
                    style = Typography.bodySmall
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp),
                    color = LineColor
                )
            }
            TextButton(
                onClick = goToRegisterPage
            ) {
                Text(
                    text = "회원가입",
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = ThemeGray,
                )
            }
        }
    }
}

fun loginUser(email: String, password: String, onLoginSuccess: () -> Unit, nickNameViewModel: NickNameViewModel) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val quizRepository = QuizRepository()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (userId != null) {
                    // 로그인 성공 시 닉네임 가져오기
                    quizRepository.fetchNickname(
                        userId = userId,
                        onSuccess = { nickname ->
                            if (nickname != null) {
                                nickNameViewModel.setNickname(nickname) // 닉네임을 뷰모델에 저장
                                println("로그인 성공: 닉네임 [$nickname] 저장 완료")
                            } else {
                                println("닉네임이 설정되지 않았습니다.")
                            }
                            onLoginSuccess() // 로그인 성공 콜백 실행
                        },
                        onError = { exception ->
                            println("닉네임 가져오기 실패: ${exception.message}")
                        }
                    )
                } else {
                    println("사용자 ID를 찾을 수 없습니다.")
                }
            }
        }
}

