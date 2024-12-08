package com.example.firebaseexample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseexample.ui.theme.Typography

@Composable
fun QuizCard(
    currentQuiz: Map<String, Any>,
    currentIndex: Int,
    totalSize: Int,
    selectedOption: String?,
    onOptionClick: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 메인 컨텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // 버튼 영역을 고려한 패딩 추가
            verticalArrangement = Arrangement.SpaceBetween, // 위아래 간격 균형 유지
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 문제 내용 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(12.dp)), // 그림자 추가
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp) // 카드 내부 패딩
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 문제 Index 상자
                        Box(
                            modifier = Modifier
                                .align(Alignment.End) // 오른쪽 상단 배치
                                .background(Color(0xFFE5F4F0), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp) // 내부 패딩
                        ) {
                            Text(
                                text = "${currentIndex + 1} / $totalSize",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF6D6D6D)
                            )
                        }

                        // 문제 텍스트
                        Text(
                            text = currentQuiz["question"] as String,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Normal, fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 옵션 리스트
            val options = currentQuiz["options"] as List<String>
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { option ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionClick(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == option) Color(0xFFABD1C6) else Color(0xFFE7E7E7)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { onOptionClick(option) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF3D8A74),
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }

        // "다음 문제" 버튼 (화면 하단 고정)
        Button(
            onClick = { onSubmit() },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // 항상 하단 고정
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D8A74)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (currentIndex + 1 < totalSize) "다음 문제" else "결과 보기",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}
