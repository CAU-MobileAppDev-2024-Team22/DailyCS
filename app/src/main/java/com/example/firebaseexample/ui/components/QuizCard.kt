package com.example.firebaseexample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuizCard(
    currentQuiz: Map<String, Any>,
    currentIndex: Int,
    totalSize: Int,
    selectedOption: String?,
    onOptionClick: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 문제 번호 및 총 문제 수 표시
        Text(
            text = "문제 ${currentIndex + 1} / $totalSize",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        // 문제 내용
        Text(
            text = currentQuiz["question"] as String,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )

        // 옵션 리스트
        val options = currentQuiz["options"] as List<String>
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Text(
                        text = option,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        // 제출 버튼
        Button(
            onClick = { onSubmit() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D8A74))
        ) {
            Text(
                text = if (currentIndex + 1 < totalSize) "다음 문제" else "결과 보기",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}
