package com.gptplus18.app.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color,
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Psychology,
            title = "ذكاء بلا قيود",
            description = "دردشة حقيقية مع ذكاء اصطناعي متقدم — بدون فلاتر ولا قيود",
            accentColor = Accent,
        ),
        OnboardingPage(
            icon = Icons.Default.Code,
            title = "فريق برمجة كامل",
            description = "5 نماذج ذكية تعمل معاً لبناء مشاريعك البرمجية باحترافية",
            accentColor = Color(0xFF93E0FF),
        ),
        OnboardingPage(
            icon = Icons.Default.Image,
            title = "صور + أغاني + فيديو",
            description = "أنشئ وسائط احترافية بأمر واحد — صور، أغاني، وتعديلات ذكية",
            accentColor = Color(0xFFFFC58F),
        ),
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                val page = pages[pageIndex]
                OnboardingPageView(page)
            }

            // Dots + Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    repeat(pages.size) { index ->
                        val selected = pagerState.currentPage == index
                        val size by animateFloatAsState(
                            targetValue = if (selected) 24f else 8f,
                            label = "dot",
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(size.dp)
                                .background(
                                    if (selected) Accent else TextTertiary.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp),
                                ),
                        )
                    }
                }

                // Button
                val isLastPage = pagerState.currentPage == pages.size - 1
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text(
                        if (isLastPage) "ابدأ الآن" else "التالي",
                        color = BgPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (!isLastPage) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("تخطي", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(page.accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(page.accentColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.accentColor,
                    modifier = Modifier.size(60.dp),
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = page.title,
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.description,
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )
    }
}
