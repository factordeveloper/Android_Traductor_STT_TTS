package com.factordev.traslator

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel

data class Language(
    val code: String,
    val name: String,
    val flag: String = ""
)

data class TranslationState(
    val isListening: Boolean = false,
    val isTranslating: Boolean = false,
    val originalText: String = "",
    val translatedText: String = "",
    val sourceLanguage: Language = Language("es", "Español"),
    val targetLanguage: Language = Language("en", "Inglés"),
    val errorMessage: String? = null,
    val hasAudioPermission: Boolean = false,
    val isSpeakingOriginal: Boolean = false,
    val isSpeakingTranslation: Boolean = false,
    val isTtsReady: Boolean = false
)

class TranslatorViewModel : ViewModel() {
    
    private val _uiState = mutableStateOf(TranslationState())
    val uiState: State<TranslationState> = _uiState
    
    private var speechRecognitionService: SpeechRecognitionService? = null
    private var textToSpeechService: TextToSpeechService? = null

    private val availableLanguages = listOf(
        Language("es", "Español"),
        Language("en", "Inglés"),
        Language("fr", "Francés"),
        Language("de", "Alemán"),
        Language("it", "Italiano"),
        Language("pt", "Portugués"),
        Language("zh", "Chino"),
        Language("ja", "Japonés")
    )

    fun getAvailableLanguages(): List<Language> = availableLanguages

    fun initializeSpeechRecognition(context: Context) {
        // Verificar permisos
        val hasPermission = PermissionManager.hasAudioPermission(context)
        _uiState.value = _uiState.value.copy(hasAudioPermission = hasPermission)
        
        if (hasPermission) {
            speechRecognitionService = SpeechRecognitionService(
                context = context,
                onResult = { recognizedText ->
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        originalText = recognizedText,
                        errorMessage = null
                    )
                    if (recognizedText.isNotEmpty()) {
                        translateText(recognizedText)
                    }
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        errorMessage = error
                    )
                },
                onStartListening = {
                    _uiState.value = _uiState.value.copy(
                        isListening = true,
                        errorMessage = null
                    )
                },
                onStopListening = {
                    _uiState.value = _uiState.value.copy(isListening = false)
                }
            )
        }
        
        // Inicializar Text-to-Speech
        initializeTextToSpeech(context)
    }
    
    private fun initializeTextToSpeech(context: Context) {
        textToSpeechService = TextToSpeechService(
            context = context,
            onInitialized = { success ->
                _uiState.value = _uiState.value.copy(isTtsReady = success)
            },
            onSpeakingStarted = {
                // Se determinará en la función específica cuál está hablando
            },
            onSpeakingFinished = {
                _uiState.value = _uiState.value.copy(
                    isSpeakingOriginal = false,
                    isSpeakingTranslation = false
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error,
                    isSpeakingOriginal = false,
                    isSpeakingTranslation = false
                )
            }
        )
    }

    fun startListening() {
        if (speechRecognitionService == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Servicio de reconocimiento de voz no inicializado"
            )
            return
        }
        
        if (!_uiState.value.hasAudioPermission) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Se necesita permiso de micrófono"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            originalText = "",
            translatedText = "",
            errorMessage = null
        )
        
        val languageCode = SpeechRecognitionService.getLanguageCode(_uiState.value.sourceLanguage.code)
        speechRecognitionService?.startListening(languageCode)
    }

    fun stopListening() {
        speechRecognitionService?.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false)
    }
    
    fun checkPermissions(context: Context) {
        val hasPermission = PermissionManager.hasAudioPermission(context)
        _uiState.value = _uiState.value.copy(hasAudioPermission = hasPermission)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun updateOriginalText(text: String) {
        _uiState.value = _uiState.value.copy(originalText = text)
        if (text.isNotEmpty()) {
            translateText(text)
        }
    }

    private fun translateText(text: String) {
        _uiState.value = _uiState.value.copy(isTranslating = true)
        
        // TODO: Implementar traducción real con API
        // Por ahora simulamos la traducción
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _uiState.value = _uiState.value.copy(
                isTranslating = false,
                translatedText = "Traducción de: $text"
            )
        }, 1500)
    }

    fun swapLanguages() {
        val currentSource = _uiState.value.sourceLanguage
        val currentTarget = _uiState.value.targetLanguage
        
        _uiState.value = _uiState.value.copy(
            sourceLanguage = currentTarget,
            targetLanguage = currentSource,
            originalText = _uiState.value.translatedText,
            translatedText = _uiState.value.originalText
        )
    }

    fun updateSourceLanguage(language: Language) {
        _uiState.value = _uiState.value.copy(sourceLanguage = language)
        if (_uiState.value.originalText.isNotEmpty()) {
            translateText(_uiState.value.originalText)
        }
    }

    fun updateTargetLanguage(language: Language) {
        _uiState.value = _uiState.value.copy(targetLanguage = language)
        if (_uiState.value.originalText.isNotEmpty()) {
            translateText(_uiState.value.originalText)
        }
    }

    fun clearText() {
        _uiState.value = _uiState.value.copy(
            originalText = "",
            translatedText = ""
        )
    }

    fun playOriginalText() {
        val originalText = _uiState.value.originalText
        if (originalText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No hay texto original para reproducir")
            return
        }
        
        if (!_uiState.value.isTtsReady) {
            _uiState.value = _uiState.value.copy(errorMessage = "Text-to-Speech no está listo")
            return
        }
        
        // Detener cualquier reproducción anterior
        textToSpeechService?.stop()
        
        // Marcar que está reproduciendo el texto original
        _uiState.value = _uiState.value.copy(
            isSpeakingOriginal = true,
            isSpeakingTranslation = false
        )
        
        textToSpeechService?.speak(originalText, _uiState.value.sourceLanguage.code)
    }
    
    fun playTranslation() {
        val translatedText = _uiState.value.translatedText
        if (translatedText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No hay traducción para reproducir")
            return
        }
        
        if (!_uiState.value.isTtsReady) {
            _uiState.value = _uiState.value.copy(errorMessage = "Text-to-Speech no está listo")
            return
        }
        
        // Detener cualquier reproducción anterior
        textToSpeechService?.stop()
        
        // Marcar que está reproduciendo la traducción
        _uiState.value = _uiState.value.copy(
            isSpeakingOriginal = false,
            isSpeakingTranslation = true
        )
        
        textToSpeechService?.speak(translatedText, _uiState.value.targetLanguage.code)
    }
    
    fun stopSpeaking() {
        textToSpeechService?.stop()
        _uiState.value = _uiState.value.copy(
            isSpeakingOriginal = false,
            isSpeakingTranslation = false
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        speechRecognitionService?.destroy()
        textToSpeechService?.destroy()
    }
} 