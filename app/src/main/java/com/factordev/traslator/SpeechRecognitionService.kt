package com.factordev.traslator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.*

class SpeechRecognitionService(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onStartListening: () -> Unit,
    private val onStopListening: () -> Unit
) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    fun startListening(languageCode: String = "es-ES") {
        if (isListening) return
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        
        if (speechRecognizer == null) {
            onError("Speech recognition no disponible en este dispositivo")
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onStartListening()
            }
            
            override fun onBeginningOfSpeech() {
                // El usuario comenzó a hablar
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Cambios en el volumen del audio (se puede usar para animaciones)
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer de audio recibido
            }
            
            override fun onEndOfSpeech() {
                // El usuario terminó de hablar
                isListening = false
            }
            
            override fun onError(error: Int) {
                isListening = false
                onStopListening()
                
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se encontró coincidencia"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                    else -> "Error desconocido"
                }
                onError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                isListening = false
                onStopListening()
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    onResult(recognizedText)
                } else {
                    onError("No se pudo reconocer la voz")
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                // Resultados parciales mientras el usuario habla
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // Se podría mostrar texto parcial en tiempo real
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Eventos adicionales
            }
        })
        
        speechRecognizer?.startListening(intent)
    }
    
    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        onStopListening()
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }
    
    fun isCurrentlyListening(): Boolean = isListening
    
    companion object {
        fun isRecognitionAvailable(context: Context): Boolean {
            return SpeechRecognizer.isRecognitionAvailable(context)
        }
        
        fun getLanguageCode(languageCode: String): String {
            return when (languageCode) {
                "es" -> "es-ES"
                "en" -> "en-US"
                "fr" -> "fr-FR"
                "de" -> "de-DE"
                "it" -> "it-IT"
                "pt" -> "pt-BR"
                "zh" -> "zh-CN"
                "ja" -> "ja-JP"
                else -> "es-ES"
            }
        }
    }
} 