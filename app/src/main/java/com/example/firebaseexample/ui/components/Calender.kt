import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


@Composable
fun CalendarPage() {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var solvedDaysMap by remember { mutableStateOf<Map<String, Set<Int>>>(emptyMap()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // 오늘 날짜 기준으로 이전 6개월과 이후 6개월의 날짜를 구함
    val today = Calendar.getInstance()
    val sixMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }
    val sixMonthsLater = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }

    // Firestore 데이터 가져오기
    fun fetchData() {
        if (userId != null) {
            val monthsRange = -6..6 // -6개월에서 +6개월까지
            monthsRange.forEach { monthOffset ->
                val calendarForMonth = Calendar.getInstance().apply {
                    time = today.time
                    add(Calendar.MONTH, monthOffset)
                }
                val year = calendarForMonth.get(Calendar.YEAR)
                val month = calendarForMonth.get(Calendar.MONTH)

                fetchSolvedDays(userId, year, month) { days ->
                    val key = "$year-${month + 1}" // "YYYY-M" 형태의 키
                    solvedDaysMap = solvedDaysMap + (key to days)
                }
            }
        }
    }

    // 초기 데이터 로딩
    LaunchedEffect(Unit) {
        fetchData()
    }

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
                if (calendar.after(sixMonthsAgo)) {
                    calendar = moveToPreviousMonth(calendar)
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month"
                )
            }
            Text(
                text = "${calendar.get(Calendar.YEAR)}.${calendar.get(Calendar.MONTH) + 1}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                if (calendar.before(sixMonthsLater)) {
                    calendar = moveToNextMonth(calendar)
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 빈 칸 추가
            items(getFirstDayOfWeek(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))) {
                Spacer(modifier = Modifier.size(40.dp))
            }

            // 날짜 배치
            val daysInMonth = getDaysInMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            items(daysInMonth.size) { index ->
                val dayNumber = daysInMonth[index]
                val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
                val hasSolved = solvedDaysMap[key]?.contains(dayNumber) == true

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 링 형태 표시
                    if (hasSolved) {
                        // 가운데가 비어 있는 링 그리기
                        Canvas(modifier = Modifier.size(40.dp)) {
                            drawArc(
                                color = Color(0xFFFF7F00),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8f) // 링 두께 조절
                            )
                        }
                    }
                    // 날짜 텍스트
                    Text(
                        text = dayNumber.toString(),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

fun fetchSolvedDays(userId: String, year: Int, month: Int, onResult: (Set<Int>) -> Unit) {
    val db: FirebaseFirestore = Firebase.firestore
    val solvedDays = mutableSetOf<Int>()

    // 시작과 끝 날짜 설정
    val startDate = Calendar.getInstance().apply {
        set(year, month, 1)
    }.time

    val endDate = Calendar.getInstance().apply {
        set(year, month + 1, 1)
        add(Calendar.DAY_OF_MONTH, -1)
    }.time

    // Firestore에서 날짜별 저장된 정답 데이터 가져오기
    val dateCollectionRef = db.collection("users").document(userId).collection("date")

    // 해당 월의 모든 날짜를 반복
    for (day in 1..31) {
        val dateString = String.format("%04d-%02d-%02d", year, month + 1, day)

        // 날짜가 유효한지 확인
        if (isValidDate(year, month, day)) {
            dateCollectionRef.document(dateString).collection("solved")
                .get()
                .addOnSuccessListener { solvedDocuments ->
                    // 해당 날짜의 solved 컬렉션이 비어있지 않으면 추가
                    if (solvedDocuments.size() > 0) {
                        solvedDays.add(day)
                    }
                    // 모든 날짜에 대해 Fetch가 끝난 후 결과 반환
                    if (day == 31) {
                        onResult(solvedDays)
                    }
                }.addOnFailureListener { exception ->
                    println("Error fetching solved days: ${exception.message}")
                }
        }
    }
}
// 날짜가 유효한지 확인하는 함수
fun isValidDate(year: Int, month: Int, day: Int): Boolean {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)
    return calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.DAY_OF_MONTH) == day
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
