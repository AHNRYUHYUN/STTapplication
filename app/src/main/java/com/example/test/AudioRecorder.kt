package com.example.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat

class AudioRecorder(private val context: Context) {
    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioRecord: AudioRecord

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } else {
            throw SecurityException("⚠️ 마이크 권한이 필요합니다. 앱 설정에서 권한을 허용해주세요.")
        }
    }

    fun start() {
        audioRecord.startRecording()
    }

    fun read(): ShortArray {
        val buffer = ShortArray(bufferSize / 2)
        audioRecord.read(buffer, 0, buffer.size)
        return buffer
    }

    fun stop() {
        audioRecord.stop()
        audioRecord.release()
    }
}
