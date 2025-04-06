package com.example.test

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            STTScreen()
        }
    }
}

@Composable
fun STTScreen() {
    val context = LocalContext.current

    // ✅ 상태 변수
    var resultText by remember { mutableStateOf("🎤 아직 인식되지 않았습니다") }
    var isListening by remember { mutableStateOf(false) }

    // ✅ 음성 인식 객체
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    // ✅ UI 화면
    TestTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎤 STT 테스트", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        startSpeechRecognition(
                            context = context,
                            onStart = { isListening = true },
                            onResult = {
                                Log.d("✅ STT 결과", it)
                                resultText = it
                                isListening = false
                            },
                            assignRecognizer = { speechRecognizer = it }
                        )
                    } else {
                        Toast.makeText(context, "마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("🎙️ 음성 인식 시작")
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isListening) {
                    Text("🟢 인식 중입니다...", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("✅ 최종 결과", style = MaterialTheme.typography.bodyMedium)
                Text(text = resultText, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    // ✅ 앱 종료 시 SpeechRecognizer 해제
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }
}
fun startSpeechRecognition(
    context: Context,
    onStart: () -> Unit,
    onResult: (String) -> Unit,
    assignRecognizer: (SpeechRecognizer) -> Unit
) {
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("STT", "🎧 준비됨")
            onStart()
        }

        override fun onBeginningOfSpeech() {
            Log.d("STT", "🗣️ 시작됨")
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            Log.d("STT", "🔇 종료됨")
        }

        override fun onError(error: Int) {
            Log.e("STT", "❌ 오류 발생: $error")
            onResult("인식 실패 (오류 코드: $error)")
        }

        override fun onResults(results: Bundle?) {
            val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                ?: "결과 없음"
            onResult(result)
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    recognizer.startListening(intent)
    assignRecognizer(recognizer)
}
