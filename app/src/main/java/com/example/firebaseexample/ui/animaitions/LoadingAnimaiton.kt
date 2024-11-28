import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoadingAnimation() {
    // 애니메이션 값들
    val offsets = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    val coroutineScope = rememberCoroutineScope()

    // 모든 글자의 애니메이션 설정
    LaunchedEffect(Unit) {
        offsets.forEachIndexed { index, animatable ->
            coroutineScope.launch {
                delay(index * 200L) // 각 글자의 시작 시간만 다르게 설정
                animatable.animateTo(
                    targetValue = -30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }
    }

    // 화면 중앙에 배치
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "매"
        Text(
            text = "매",
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = offsets[0].value.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))

        // "일"
        Text(
            text = "일",
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = offsets[1].value.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))

        // "C"
        Text(
            text = "C",
            color = Color(0xFF3D8A74), // 연두색
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = offsets[2].value.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))

        // "S"
        Text(
            text = "S",
            color = Color(0xFF3D8A74), // 연두색
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = offsets[3].value.dp)
        )
    }
}
