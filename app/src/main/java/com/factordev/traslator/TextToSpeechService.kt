package com.factordev.traslator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class TextToSpeechService(
    private val context: Context,
    private val onInitialized: (Boolean) -> Unit,
    private val onSpeakingStarted: () -> Unit = {},
    private val onSpeakingFinished: () -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var isSpeaking = false
    
    init {
        initializeTextToSpeech()
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                setupUtteranceProgressListener()
                onInitialized(true)
            } else {
                isInitialized = false
                onInitialized(false)
                onError("Error al inicializar Text-to-Speech")
            }
        }
    }
    
    private fun setupUtteranceProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
                onSpeakingStarted()
            }
            
            override fun onDone(utteranceId: String?) {
                isSpeaking = false
                onSpeakingFinished()
            }
            
            override fun onError(utteranceId: String?) {
                isSpeaking = false
                onSpeakingFinished()
                onError("Error al reproducir audio")
            }
        })
    }
    
    fun speak(text: String, languageCode: String) {
        if (!isInitialized) {
            onError("Text-to-Speech no está inicializado")
            return
        }
        
        if (text.isBlank()) {
            onError("No hay texto para reproducir")
            return
        }
        
        // Detener cualquier reproducción anterior
        stop()
        
        // Configurar idioma
        val locale = getLocaleFromLanguageCode(languageCode)
        val result = textToSpeech?.setLanguage(locale)
        
        when (result) {
            TextToSpeech.LANG_MISSING_DATA -> {
                onError("Datos de idioma no disponibles")
                return
            }
            TextToSpeech.LANG_NOT_SUPPORTED -> {
                onError("Idioma no soportado")
                return
            }
        }
        
        // Configurar parámetros de velocidad y tono
        textToSpeech?.setSpeechRate(0.9f) // Velocidad normal
        textToSpeech?.setPitch(1.0f) // Tono normal
        
        // Reproducir texto
        val utteranceId = "TTS_${System.currentTimeMillis()}"
        val speakResult = textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
        
        if (speakResult == TextToSpeech.ERROR) {
            onError("Error al iniciar reproducción")
        }
    }
    
    fun stop() {
        if (isSpeaking) {
            textToSpeech?.stop()
            isSpeaking = false
            onSpeakingFinished()
        }
    }
    
    fun pause() {
        // TextToSpeech no tiene pausa nativa, solo stop
        stop()
    }
    
    fun isCurrentlySpeaking(): Boolean = isSpeaking
    
    fun isReady(): Boolean = isInitialized
    
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        isSpeaking = false
    }
    
    private fun getLocaleFromLanguageCode(languageCode: String): Locale {
        return when (languageCode) {
            "es" -> Locale("es", "ES")
            "en" -> Locale("en", "US")
            "fr" -> Locale("fr", "FR")
            "de" -> Locale("de", "DE")
            "it" -> Locale("it", "IT")
            "pt" -> Locale("pt", "BR")
            "zh" -> Locale("zh", "CN")
            "ja" -> Locale("ja", "JP")
            else -> Locale("es", "ES")
        }
    }
    
    companion object {
        fun isTextToSpeechAvailable(context: Context): Boolean {
            return try {
                val intent = android.content.Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
                val activities = context.packageManager.queryIntentActivities(intent, 0)
                activities.isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        
        fun getAvailableLanguages(textToSpeech: TextToSpeech?): Set<Locale> {
            return textToSpeech?.availableLanguages ?: emptySet()
        }
    }
} 