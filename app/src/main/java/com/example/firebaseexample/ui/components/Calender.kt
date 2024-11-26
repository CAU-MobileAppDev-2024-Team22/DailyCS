import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun CalendarPage() {
    // 현재 날짜 상태 관리
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val daysInMonth = getDaysInMonth(year, month)
    val firstDayOfWeek = getFirstDayOfWeek(year, month)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 이전/다음 달 이동 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar = moveToPreviousMonth(calendar)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // 왼쪽 화살표 아이콘으로 변경 가능
                    contentDescription = "Previous Month"
                )
            }
            Text(
                text = "${year}.${month + 1}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                calendar = moveToNextMonth(calendar)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward, // 오른쪽 화살표 아이콘으로 변경 가능
                    contentDescription = "Next Month"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 요일 헤더
        DaysOfWeekHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜 표시
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // 세로 간격 조정
        ) {
            // 빈 칸 추가 (해당 월의 첫 번째 요일 앞의 공백)
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier.size(40.dp))
            }

            // 날짜 배치
            items(daysInMonth.size) { index ->
                val dayNumber = daysInMonth[index]
                val isToday = dayNumber == today && month == Calendar.getInstance().get(Calendar.MONTH) &&
                        year == Calendar.getInstance().get(Calendar.YEAR)

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber.toString(),
                        color = if (isToday) Color.Red else Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            }
        }

    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// 날짜 계산 헬퍼 함수
fun getDaysInMonth(year: Int, month: Int): List<Int> {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    return (1..daysInMonth).toList()
}

fun getFirstDayOfWeek(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.get(Calendar.DAY_OF_WEEK) - 1 // 1(Sunday) ~ 7(Saturday)
}

// 이전 달로 이동
fun moveToPreviousMonth(calendar: Calendar): Calendar {
    val newCalendar = calendar.clone() as Calendar
    newCalendar.add(Calendar.MONTH, -1)
    return newCalendar
}

// 다음 달로 이동
fun moveToNextMonth(calendar: Calendar): Calendar {
    val newCalendar = calendar.clone() as Calendar
    newCalendar.add(Calendar.MONTH, 1)
    return newCalendar
}
