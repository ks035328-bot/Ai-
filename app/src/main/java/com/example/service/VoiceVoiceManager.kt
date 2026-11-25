package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VoiceVoiceManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onStateChanged: (VoiceState) -> Unit
) : TextToSpeech.OnInitListener, RecognitionListener {

    enum class VoiceState {
        IDLE,
        LISTENING,
        THINKING,
        SPEAKING
    }

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    private var isListening = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onStateChanged(VoiceState.SPEAKING)
                    }

                    override fun onDone(utteranceId: String?) {
                        onStateChanged(VoiceState.IDLE)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onStateChanged(VoiceState.IDLE)
                    }
                })
            }
        }
    }

    fun configureVoice(pitch: Float = 1.1f, speed: Float = 1.0f) {
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)
    }

    fun speak(text: String) {
        if (!isTtsReady) return
        stopListening()
        onStateChanged(VoiceState.SPEAKING)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LumiSpeech_${System.currentTimeMillis()}")
    }

    fun startListening() {
        if (isListening) return
        tts?.stop()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Lumi is listening...")
        }

        try {
            isListening = true
            onStateChanged(VoiceState.LISTENING)
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            onStateChanged(VoiceState.IDLE)
            onError("Voice recognition unavailable on this device. You can type commands below!")
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            speechRecognizer?.stopListening()
            onStateChanged(VoiceState.IDLE)
        }
    }

    // SpeechRecognizer Callbacks
    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {
        onStateChanged(VoiceState.LISTENING)
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        isListening = false
        onStateChanged(VoiceState.THINKING)
    }

    override fun onError(error: Int) {
        isListening = false
        onStateChanged(VoiceState.IDLE)
        val message = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No voice match found. Try again!"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
            else -> "Voice recognition paused."
        }
        onError(message)
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onResult(matches[0])
        } else {
            onStateChanged(VoiceState.IDLE)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
