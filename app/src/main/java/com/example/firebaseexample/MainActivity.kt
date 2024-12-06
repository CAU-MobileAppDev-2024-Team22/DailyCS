package com.example.firebaseexample

import QuizListPage
import QuizPage
import QuizViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseexample.ui.pages.*
import com.example.firebaseexample.ui.theme.FirebaseExampleTheme
import com.example.firebaseexample.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Firebase 초기화
        enableEdgeToEdge()
        setContent {
            FirebaseExampleTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                // 로그인 상태 관찰
                val isLoggedIn = authViewModel.isLoggedIn

                val quizViewModel: QuizViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) "main" else "login",
                ) {
                    // 로그인 페이지
                    composable(route = "login") {
                        LoginPage(
                            goToRegisterPage = { navController.navigate(route = "register") },
                            onLoginSuccess = {
                                authViewModel.setLoggedIn(true) // 로그인 성공 처리
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 회원가입 페이지
                    composable(route = "register") {
                        RegisterPage(
                            backToLoginPage = { navController.navigateUp() }
                        )
                    }

                    // 메인 페이지
                    composable(route = "main") {
                        MainPage(
                            onLogout = {
                                authViewModel.setLoggedIn(false) // 로그아웃 처리
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            goToQuizListPage = { navController.navigate(route = "quizList") }
                        )
                    }

                    // 퀴즈 목록 페이지
                    composable(route = "quizList") {
                        QuizListPage(
                            onCategoryClick = { categoryId ->
                                navController.navigate("quizPage/$categoryId")
                            },
                            navController = navController // NavController 전달
                        )
                    }

                    composable(route = "errorPage") {
                        ErrorPage(
                            onRetry = {
                                navController.navigate("quizList") { popUpTo("quizList") { inclusive = true } }
                            },
                            onGoToMain = {
                                navController.navigate("main") { popUpTo("main") { inclusive = true } }
                            }
                        )
                    }


                    // 퀴즈 페이지
                    composable(route = "quizPage/{categoryId}") { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                        QuizPage(
                            categoryId = categoryId,
                            onBackPressed = {
                                navController.navigateUp() // 뒤로 가기 동작 처리
                            },
                            onFinishQuiz = { finalScore, totalQuestions ->
                                quizViewModel.updateScore(finalScore, totalQuestions) // 뷰모델 업데이트
                                navController.navigate("quizResult") {
                                    popUpTo("quizList") { inclusive = true } // 퀴즈 목록으로 돌아갈 때 히스토리를 제거
                                }
                            }
                        )
                    }

                    // 퀴즈 결과 페이지
                    composable(route = "quizResult") {
                        QuizResultPage(
                            score = quizViewModel.score.intValue,
                            totalQuestions = quizViewModel.totalQuestions.intValue,
                            onRestartQuiz = { navController.navigate("quizList") },
                            onGoToMainPage = { navController.navigate("main") }
                        )
                    }
                }
            }
        }
    }
}
