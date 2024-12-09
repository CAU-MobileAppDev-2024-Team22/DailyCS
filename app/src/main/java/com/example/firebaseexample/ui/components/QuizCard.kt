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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseexample.ui.theme.ButtonDarkGreen
import com.example.firebaseexample.ui.theme.ButtonGray
import com.example.firebaseexample.ui.theme.ThemeLightGray
import com.example.firebaseexample.ui.theme.ThemeLightGreen
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
            .padding(12.dp),
    ) {
        // 메인 컨텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // 버튼 영역을 고려한 패딩 추가
            verticalArrangement = Arrangement.Center, // 위아래 간격 균형 유지
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 문제 내용 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)), // 그림자 추가
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp) // 카드 내부 패딩
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
                            style = Typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Start,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 옵션 리스트
            val options = currentQuiz["options"] as List<String>
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                options.forEach { option ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionClick(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == option) ThemeLightGreen else ButtonGray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 옵션 텍스트
                            Text(
                                text = option,
                                style = if (selectedOption == option)
                                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
                                else
                                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                                color = Color.Black,
                                modifier = Modifier
                                    .weight(1f) // 텍스트에 가용 공간 부여
                                    .padding(end = 8.dp), // 라디오 버튼과의 간격 추가
                                maxLines = Int.MAX_VALUE, // 필요한 만큼 줄바꿈
                                overflow = TextOverflow.Clip // 넘치는 텍스트 처리
                            )

                            // 라디오 버튼 (고정된 위치)
                            Box(
                                modifier = Modifier
                                    .size(24.dp) // 라디오 버튼 크기 고정
                                    .align(Alignment.CenterVertically) // 수직 정렬
                            ) {
                                RadioButton(
                                    selected = selectedOption == option,
                                    onClick = { onOptionClick(option) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ButtonDarkGreen,
                                        unselectedColor = Color.Gray
                                    )
                                )
                            }
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
            colors = ButtonDefaults.buttonColors(containerColor = ButtonDarkGreen),// 0xFF3D8A74
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = if (currentIndex + 1 < totalSize) "다음 문제" else "결과 보기",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}
