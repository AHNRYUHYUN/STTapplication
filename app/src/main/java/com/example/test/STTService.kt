package com.example.test

import android.app.*
import android.content.Intent
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat

class STTService : Service() {
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("STTService", "🎙️ STTService 시작됨")
        startForegroundNotification()
        startSpeechRecognition()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ✅ 포그라운드 서비스 알림 설정
    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "stt_channel"
            val channel = NotificationChannel(
                channelId, "STT 서비스", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("음성 인식 중...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build()

            startForeground(1, notification)
        }
    }

    // ✅ STT 실행
    private fun startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e("STTService", "❌ 음성 인식을 사용할 수 없음")
            stopSelf()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("STTService", "🟢 준비됨")
                sendBroadcast(Intent("com.example.test.STT_READY"))
            }

            override fun onBeginningOfSpeech() {
                Log.d("STTService", "🗣️ 말하기 시작")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("STTService", "🔇 말하기 종료")
            }

            override fun onError(error: Int) {
                Log.e("STTService", "❌ 오류 발생: $error")
                stopSelf() // ✅ 에러 발생 시 서비스 종료
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val result = matches?.firstOrNull() ?: "결과 없음"
                Log.d("STTService", "✅ 인식 결과: $result")

                sendBroadcast(Intent("com.example.test.STT_FINAL_RESULT").apply {
                    putExtra("stt_final_result", result)
                })

                stopSelf() // ✅ 결과 전달 후 종료
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        Log.d("STTService", "🛑 STTService 종료됨")
    }
}
