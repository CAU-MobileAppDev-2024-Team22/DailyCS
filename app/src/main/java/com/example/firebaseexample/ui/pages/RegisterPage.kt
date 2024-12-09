package com.example.firebaseexample.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseexample.ui.theme.LineColor
import com.example.firebaseexample.ui.theme.TextFieldBorder
import com.example.firebaseexample.ui.theme.ThemeBlue
import com.example.firebaseexample.ui.theme.ThemeGray
import com.example.firebaseexample.ui.theme.Typography
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(
    backToLoginPage: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope() // 코루틴 스코프 생성

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    //Text(text = "회원가입")
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // SnackbarHost 추가
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
                text = "계정 만들기",
                fontWeight = FontWeight.Bold,
                style = Typography.titleLarge.copy(fontSize = 28.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "계정으로 등록할 이메일을 하단에 입력해주세요.",
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
                        style = Typography.bodyMedium,
                        color = ThemeGray
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ThemeBlue,
                    unfocusedBorderColor = TextFieldBorder
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = "Password",
                        style = Typography.bodyMedium,
                        color = ThemeGray
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    contentColor = Color.White,
                ),
                onClick = {
                    registerUser(email, password) { success, message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }
            ) {
                Text(
                    text = "회원가입",
                    style = Typography.bodyMedium
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
                    textAlign = TextAlign.Center,
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
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    backToLoginPage()
                }
            ) {
                Text(
                    text = "로그인 페이지로 돌아가기",
                    textAlign = TextAlign.Center,
                    color = ThemeGray,
                    style = Typography.bodyMedium,
                )
            }
        }
    }
}

fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "회원가입 성공")
            } else {
                val errorMessage = task.exception?.message
                if (errorMessage?.contains("already in use") == true) {
                    onResult(false, "이미 가입된 회원입니다")
                } else {
                    onResult(false, errorMessage ?: "회원가입 실패")
                }
            }
        }
}