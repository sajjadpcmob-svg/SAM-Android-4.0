
package ir.elviran.sam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ir.elviran.sam.ui.theme.SamTheme
import java.util.Locale

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
    val context = LocalContext.current

    var transcript by remember { mutableStateOf("") }
    var partialText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("آماده") }
    var listening by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }
    var faVoiceAvailable by remember { mutableStateOf(false) }

    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val intent = createRecognitionIntent()
                recognizer?.startListening(intent)
                listening = recognizer != null
                status = if (recognizer != null) {
                    "در حال آماده‌سازی میکروفون"
                } else {
                    "سرویس تشخیص گفتار در دسترس نیست"
                }
            } else {
                status = "مجوز میکروفون داده نشد"
            }
        }

    // Text-to-Speech lifecycle
    DisposableEffect(context) {
        lateinit var engine: TextToSpeech

        engine = TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                val languageResult =
                    engine.setLanguage(Locale.forLanguageTag("fa-IR"))

                faVoiceAvailable =
                    languageResult >= TextToSpeech.LANG_AVAILABLE

                ttsReady = true

                if (faVoiceAvailable) {
                    status = "موتور گفتار آماده است"
                } else {
                    status = "صدای فارسی نصب نیست"
                }
            } else {
                ttsReady = false
                faVoiceAvailable = false
                status = "راه‌اندازی موتور گفتار ناموفق بود"
            }
        }

        tts = engine

        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
        }
    }

    // Speech recognition lifecycle
    DisposableEffect(context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            status = "سرویس تشخیص گفتار در دستگاه موجود نیست"
        } else {
            val speech = SpeechRecognizer.createSpeechRecognizer(context)

            speech.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        listening = true
                        status = "گوش می‌دهم..."
                    }

                    override fun onBeginningOfSpeech() {
                        status = "صدایت دریافت می‌شود..."
                    }

                    override fun onRmsChanged(rmsdB: Float) = Unit

                    override fun onBufferReceived(buffer: ByteArray?) = Unit

                    override fun onEndOfSpeech() {
                        listening = false
                        status = "در حال پردازش گفتار..."
                    }

                    override fun onError(error: Int) {
                        listening = false
                        partialText = ""

                        status = when (error) {
                            SpeechRecognizer.ERROR_AUDIO ->
                                "خطا در دریافت صدا"

                            SpeechRecognizer.ERROR_CLIENT ->
                                "خطای ارتباط با سرویس گفتار"

                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "مجوز میکروفون کافی نیست"

                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "سرویس تشخیص گفتار به شبکه دسترسی ندارد"

                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "گفتاری تشخیص داده نشد"

                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                                "سرویس گفتار مشغول است"

                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "صدایی دریافت نشد"

                            else ->
                                "خطای تشخیص گفتار: $error"
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        listening = false
                        partialText = ""

                        val recognized =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )?.firstOrNull().orEmpty()

                        if (recognized.isNotBlank()) {
                            transcript = listOf(
                                transcript.trim(),
                                recognized.trim()
                            ).filter { it.isNotBlank() }.joinToString("\n")

                            status = "گفتار به متن تبدیل شد"
                        } else {
                            status = "متنی تشخیص داده نشد"
                        }
                    }

                    override fun onPartialResults(results: Bundle?) {
                        partialText =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )?.firstOrNull().orEmpty()
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) = Unit
                }
            )

            recognizer = speech
        }

        onDispose {
            recognizer?.cancel()
            recognizer?.destroy()
            recognizer = null
            listening = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF101B24),
                        Color(0xFF080D12),
                        Color(0xFF05080C)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "SAM",
                    color = Color(0xFFE4EEF0),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "دستیار هوشمند شخصی",
                    color = Color(0xFF91A5B1),
                    fontSize = 12.sp
                )
            }

            StatusPill(status)
        }

        Spacer(Modifier.height(22.dp))

        VoiceOrb(listening)

        Text(
            "سلام، من سام هستم",
            color = Color(0xFFE4EEF0),
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            status,
            color = Color(0xFF91A5B1),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = transcript,
            onValueChange = { transcript = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            minLines = 4,
            maxLines = 8,
            label = {
                Text("متن مکالمه")
            },
            placeholder = {
                Text("اینجا بنویس یا با سام صحبت کن...")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFFE4EEF0),
                unfocusedTextColor = Color(0xFFE4EEF0),
                focusedBorderColor = Color(0xFF53B9C3),
                unfocusedBorderColor = Color(0xFF29404D),
                cursorColor = Color(0xFF75D5D6)
            )
        )

        if (partialText.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                "در حال تشخیص: $partialText",
                color = Color(0xFF75D5D6),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (listening) {
                        recognizer?.stopListening()
                        status = "در حال پایان دادن به دریافت صدا..."
                    } else {
                        val granted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            if (recognizer != null) {
                                listening = true
                                status = "در حال شروع..."
                                recognizer?.startListening(
                                    createRecognitionIntent()
                                )
                            } else {
                                status = "سرویس تشخیص گفتار موجود نیست"
                            }
                        } else {
                            permissionLauncher.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF236C82)
                )
            ) {
                Text(if (listening) "توقف" else "شروع مکالمه")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (transcript.isBlank()) {
                        status = "متنی برای خواندن وجود ندارد"
                    } else if (!ttsReady) {
                        status = "موتور گفتار هنوز آماده نیست"
                    } else if (!faVoiceAvailable) {
                        status = "صدای فارسی روی دستگاه نصب نیست"
                    } else {
                        tts?.speak(
                            transcript,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "sam-persian-utterance"
                        )
                        status = "در حال خواندن متن"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF244A43)
                )
            ) {
                Text("خواندن متن")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    transcript = ""
                    partialText = ""
                    status = "متن پاک شد"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF34404A)
                )
            ) {
                Text("پاک کردن متن")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    tts?.stop()
                    status = "پخش صدا متوقف شد"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF34404A)
                )
            ) {
                Text("توقف صدا")
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "تشخیص گفتار ممکن است به سرویس آنلاین دستگاه وابسته باشد. " +
                "این نسخه هنوز موتور پاسخ‌گوی هوش مصنوعی ندارد.",
            color = Color(0xFF718792),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun createRecognitionIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fa-IR")
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }
}

@Composable
private fun StatusPill(status: String) {
    Row(
        modifier = Modifier
            .border(
                1.dp,
                Color(0xFF29404D),
                RoundedCornerShape(50)
            )
            .background(
                Color(0xFF12212A),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(
                    if (status.contains("گوش")) Color(0xFF65D6A5)
                    else Color(0xFFE6A85C),
                    CircleShape
                )
        )

        Text(
            "  $status",
            color = Color(0xFFB8C8D0),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun VoiceOrb(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "sam-orb")

    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (active) 1.12f else 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 700 else 2200
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-pulse"
    )

    Box(
        modifier = Modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(180.dp)
                .scale(pulse)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x6637B8C8),
                            Color(0x2237B8C8),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .size(124.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF75D5D6),
                            Color(0xFF236C82),
                            Color(0xFF152E43)
                        )
                    ),
                    CircleShape
                )
                .border(
                    1.dp,
                    Color(0xFF9BE7E4),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(82.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFB7F2E9),
                                Color(0xFF3E9AA6),
                                Color(0xFF17465B)
                            )
                        ),
                        CircleShape
                    )
            )
        }
    }
}
