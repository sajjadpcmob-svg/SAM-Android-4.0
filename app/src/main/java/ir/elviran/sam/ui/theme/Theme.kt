package ir.elviran.sam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SamColors = darkColorScheme(
    primary = Color(0xFF75D5D6),
    secondary = Color(0xFFE6A85C),
    background = Color(0xFF080D12),
    surface = Color(0xFF111D26),
    onPrimary = Color(0xFF062027),
    onBackground = Color(0xFFE4EEF0),
    onSurface = Color(0xFFE4EEF0)
)

@Composable
fun SamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SamColors,
        content = content
    )
}
