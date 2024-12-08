package com.example.firebaseexample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebaseexample.ui.theme.ThemeBlue
import com.example.firebaseexample.ui.theme.ThemeGray
import com.example.firebaseexample.ui.theme.ThemeGreen
import com.example.firebaseexample.ui.theme.ThemeLightGray
import com.example.firebaseexample.ui.theme.Typography
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer

@Composable
fun CircularChart(
    progress: Float,
    score: Int,
    totalQuestions: Int
) {
    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            pieChartData = PieChartData(
                listOf(
                    PieChartData.Slice(value = progress, color = ThemeLightGray), // 녹색 진행도
                    PieChartData.Slice(value = 1f - progress, color = ThemeBlue) // 파란색 배경
                )
            ),
            modifier = Modifier.fillMaxSize(),
            animation = simpleChartAnimation(),
            sliceDrawer = SimpleSliceDrawer(sliceThickness=48f) // 두께 조정
        )

        // 중앙 텍스트 (점수 표시)
        Text(
            text = "$score / $totalQuestions",
            style = Typography.titleLarge
        )
    }
}
