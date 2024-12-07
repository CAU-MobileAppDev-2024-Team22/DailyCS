package com.example.firebaseexample

import QuizListPage
import QuizPage
import QuizViewModel
import TodayQuizPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseexample.ui.pages.*
import com.example.firebaseexample.ui.theme.FirebaseExampleTheme
import com.example.firebaseexample.viewmodel.AuthViewModel
import com.example.firebaseexample.viewmodel.TodayQuizViewModel
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                val todayQuizViewModel: TodayQuizViewModel = hiltViewModel()
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
                            goToQuizListPage = { navController.navigate(route = "quizList") },
                            goToTodayQuizPage = { navController.navigate(route = "todayQuiz") }
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
                                navController.navigate("quizResult/$finalScore/$totalQuestions") {
                                    popUpTo("quizList") { inclusive = true } // 퀴즈 목록으로 돌아갈 때 히스토리를 제거
                                }
                            }
                        )
                    }

                    // 퀴즈 결과 페이지
                    composable(route = "quizResult/{score}/{totalQuestions}") { backStackEntry ->
                        val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                        val totalQuestions = backStackEntry.arguments?.getString("totalQuestions")?.toIntOrNull() ?: 10
                        QuizResultPage(
                            score = score,
                            totalQuestions = totalQuestions,
                            onRestartQuiz = { navController.navigate("quizList") },
                            onGoToMainPage = { navController.navigate("main") }
                        )
                    }

                    // 오늘의 퀴즈 페이지
                    composable(route = "todayQuiz") {
                        TodayQuizPage(
                            todayQuizViewModel,
                            onFinishQuiz = { finalScore ->
                                navController.navigate("quizResult/$finalScore")
                            },
                            onTimeout = {
                                navController.navigate("errorPage") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                }
            }
        }
    }
}
