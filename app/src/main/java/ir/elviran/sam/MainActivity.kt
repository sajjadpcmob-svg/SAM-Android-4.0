package ir.elviran.sam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.elviran.sam.ui.theme.SamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF080D12)
                ) {
                    SamHomeScreen()
                }
            }
        }
    }
}

@Composable
private fun SamHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101B24), Color(0xFF080D12), Color(0xFF05080C))
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text("SAM", color = Color(0xFFE4EEF0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("دستیار هوشمند شخصی", color = Color(0xFF91A5B1), fontSize = 12.sp)
            }
            StatusPill()
        }

        Spacer(Modifier.height(76.dp))
        VoiceOrb()
        Spacer(Modifier.height(30.dp))
        Text(
            text = "سلام، من سام هستم",
            color = Color(0xFFE4EEF0),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "هسته‌ی جدید SAM 4.0 آماده‌ی توسعه است",
            color = Color(0xFF91A5B1),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        FeatureRow()
        Spacer(Modifier.height(18.dp))
        Text(
            text = "رابط نمایشی اولیه • ورودی و خروجی صوتی هنوز متصل نشده‌اند",
            color = Color(0xFF637783),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusPill() {
    Row(
        modifier = Modifier
            .border(1.dp, Color(0xFF29404D), RoundedCornerShape(50))
            .background(Color(0xFF12212A), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).background(Color(0xFFE6A85C), CircleShape))
        Text("  هسته‌ی اولیه", color = Color(0xFFB8C8D0), fontSize = 11.sp)
    }
}

@Composable
private fun VoiceOrb() {
    val transition = rememberInfiniteTransition(label = "sam-orb")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-pulse"
    )

    Box(
        modifier = Modifier.size(230.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(220.dp)
                .scale(pulse)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x5537B8C8), Color(0x2237B8C8), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier
                .size(142.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF75D5D6), Color(0xFF236C82), Color(0xFF152E43))
                    ),
                    CircleShape
                )
                .border(1.dp, Color(0xFF9BE7E4), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFB7F2E9), Color(0xFF3E9AA6), Color(0xFF17465B))
                        ),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun FeatureRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FeatureTile("صوت", "در صف توسعه", Modifier.weight(1f))
        FeatureTile("کارخانه", "در صف توسعه", Modifier.weight(1f))
        FeatureTile("حافظه", "در صف توسعه", Modifier.weight(1f))
    }
}

@Composable
private fun FeatureTile(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF111D26), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF203440), RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Color(0xFFE4EEF0), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Color(0xFF8299A5), fontSize = 10.sp)
    }
}
