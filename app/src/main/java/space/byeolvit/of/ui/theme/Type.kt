package space.byeolvit.of.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import space.byeolvit.of.R

val PretendardVariable = FontFamily(
    Font(R.font.pretendard_variable)
)

// word-break: keep-all — CJK 문자 사이에서 줄바꿈하지 않고 단어/어절 단위로만 줄바꿈
private val keepAllLineBreak = LineBreak.Simple.copy(wordBreak = LineBreak.WordBreak.Phrase)

val OrbitalForestTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        lineBreak = keepAllLineBreak
    ),
    displayMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        lineBreak = keepAllLineBreak
    ),
    displaySmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        lineBreak = keepAllLineBreak
    ),
    headlineLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        lineBreak = keepAllLineBreak
    ),
    headlineMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        lineBreak = keepAllLineBreak
    ),
    headlineSmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        lineBreak = keepAllLineBreak
    ),
    titleLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        lineBreak = keepAllLineBreak
    ),
    titleMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        lineBreak = keepAllLineBreak
    ),
    titleSmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineBreak = keepAllLineBreak
    ),
    bodyLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        lineBreak = keepAllLineBreak
    ),
    bodyMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        lineBreak = keepAllLineBreak
    ),
    bodySmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        lineBreak = keepAllLineBreak
    ),
    labelLarge = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineBreak = keepAllLineBreak
    ),
    labelMedium = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineBreak = keepAllLineBreak
    ),
    labelSmall = TextStyle(
        fontFamily = PretendardVariable,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineBreak = keepAllLineBreak
    )
)
