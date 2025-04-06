package com.example.test

import android.content.Intent
import android.os.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.TestTheme

class STTActivity : ComponentActivity() {
    private var partialResult by mutableStateOf("🎤 음성 인식 중...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        partialResult = intent.getStringExtra("stt_partial_result") ?: "❌ STT 부분 결과 없음"
        Log.d("STTActivity", "📡 STT 결과 수신: $partialResult")

        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📝 STT 실시간 결과", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = partialResult, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            goToMainActivity()
        }, 2000)
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}
