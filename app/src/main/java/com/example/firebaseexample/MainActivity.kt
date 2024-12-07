package com.example.firebaseexample

import QuizListPage
import QuizViewModel
import TodayQuizPage
import CategoryQuizPage
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
                            goToRegisterPage = { navController.navigate("register") },
                            onLoginSuccess = {
                                authViewModel.setLoggedIn(true)
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
                                authViewModel.setLoggedIn(false)
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            goToQuizListPage = { navController.navigate("quizList") },
                            goToTodayQuizPage = { navController.navigate("todayQuiz") }
                        )
                    }

                    // 퀴즈 목록 페이지
                    composable(route = "quizList") {
                        QuizListPage(
                            onCategoryClick = { categoryId ->
                                navController.navigate("quizPage/$categoryId")
                            },
                            navController = navController
                        )
                    }

                    // 에러 페이지
                    composable(route = "errorPage") {
                        ErrorPage(
                            onRetry = {
                                navController.navigateUp()
                            },
                            onGoToMain = {
                                navController.navigate("main") { popUpTo("main") { inclusive = true } }
                            }
                        )
                    }

                    // 카테고리 퀴즈 페이지
                    composable(route = "quizPage/{categoryId}") { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                        CategoryQuizPage(
                            categoryId = categoryId,
                            viewModel = quizViewModel,
                            onBackPressed = { navController.navigateUp() },
                            onFinishQuiz = { finalScore ->
                                navController.navigate("quizResult/$finalScore/${quizViewModel.totalQuestions.value}") {
                                    popUpTo("quizList") { inclusive = true }
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
                            onRestartQuiz = { navController.navigateUp()},
                            onGoToMainPage = { navController.navigate("main") }
                        )
                    }

                    // 오늘의 퀴즈 페이지
                    composable(route = "todayQuiz") {
                        TodayQuizPage(
                            viewModel = quizViewModel,
                            onFinishQuiz = { finalScore ->
                                navController.navigate("quizResult/$finalScore/${quizViewModel.totalQuestions.value}") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            onBackPressed = { navController.navigateUp() },
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
