package com.example.firebaseexample

import BrushupQuizPage
import QuizListPage
import com.example.firebaseexample.data.model.QuizViewModel
import TodayQuizPage
import CategoryQuizPage
import MyPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseexample.ui.pages.*
import com.example.firebaseexample.ui.theme.FirebaseExampleTheme
import com.example.firebaseexample.viewmodel.AuthViewModel
import com.example.firebaseexample.viewmodel.NickNameViewModel
import com.example.firebaseexample.viewmodel.QuizListViewModel
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
                val quizListviewModel: QuizListViewModel = viewModel()
                val quizViewModel: QuizViewModel = viewModel()
                val nickNameViewModel: NickNameViewModel = viewModel()
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
                            },
                            nickNameViewModel = nickNameViewModel,
                        )
                    }

                    // 회원가입 페이지
                    composable(route = "register") {
                        RegisterPage(
                            backToLoginPage = { navController.navigateUp() }
                        )
                    }

                    // 닉네임 페이지
                    composable(route = "nickname") {
                        NicknamePage(
                            backToMainPage = { navController.navigateUp() },
                            nicknameViewModel = nickNameViewModel,
                            onNicknameRegistered = { navController.navigate("main") } // 메인 페이지로 이동
                        )
                    }

                    // 메인 페이지
                    composable(route = "main") {
                        MainPage(
                            viewModel = quizViewModel,
                            nicknameViewModel = nickNameViewModel,
                            onLogout = {
                                authViewModel.setLoggedIn(false)
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            goToQuizListPage = { navController.navigate("quizList") },
                            goToTodayQuizPage = { navController.navigate("todayQuiz") },
                            goToBrushQuizPage = { navController.navigate("brushupQuiz")},
                            goToNicknamePage = { navController.navigate("nickname") },
                            goToMyPage = { navController.navigate("myPage") }
                        )
                    }

                    // 퀴즈 목록 페이지
                    composable(route = "quizList") {
                        QuizListPage(
                            onCategoryClick = { categoryId ->
                                navController.navigate("quizPage/$categoryId")
                            },
                            navController = navController,
                            viewModel = quizListviewModel
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
                            onFinishQuiz = { finalScore,->
                                navController.navigate("quizResult/$finalScore/${quizViewModel.solvedQuizzesNum.value}") {
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
                            onGoToMainPage = { navController.navigate("main") },
                            viewModel = quizViewModel,
                            navController = navController
                        )
                    }

                    // 오늘의 퀴즈 페이지
                    composable(route = "todayQuiz") {
                        TodayQuizPage(
                            viewModel = quizViewModel,
                            onFinishQuiz = { finalScore ->
                                navController.navigate("quizResult/$finalScore/${quizViewModel.solvedQuizzesNum.value}") {
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

                    // 복습 추천 문제 페이지
                    composable(route = "brushupQuiz") {
                        BrushupQuizPage(
                            viewModel = quizViewModel,
                            onFinishQuiz = { finalScore ->
                                navController.navigate("quizResult/$finalScore/${quizViewModel.solvedQuizzesNum.value}") {
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
                    composable(route = "myPage") {

                            // QuizViewModel 인스턴스 생성 (필요에 따라 SharedViewModel 사용)

                            // MyPage로 이동 시 뒤로 가기 버튼을 처리하기 위한 네비게이션 컨트롤러 필요
                            MyPage(
                                quizViewModel = quizViewModel,
                                onBackPressed = {
                                    // 네비게이션 뒤로 가기
                                    navController.popBackStack()
                                },
                                navController = navController,
                                quizListViewModel = quizListviewModel
                            )

                    }
                }
            }
        }
    }
}
