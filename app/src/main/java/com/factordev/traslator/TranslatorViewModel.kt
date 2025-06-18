package com.factordev.traslator

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factordev.traslator.api.ApiConfig
import com.factordev.traslator.api.LibreTranslateService
import com.factordev.traslator.api.TranslationService
import kotlinx.coroutines.launch

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
    val isTtsReady: Boolean = false,
    val sourceVoice: String? = null, // Voz del interlocutor 1 (idioma original)
    val targetVoice: String? = null, // Voz del interlocutor 2 (idioma traducido)
    val availableSourceVoices: List<VoiceInfo> = emptyList(),
    val availableTargetVoices: List<VoiceInfo> = emptyList()
)

class TranslatorViewModel : ViewModel() {
    
    private val _uiState = mutableStateOf(TranslationState())
    val uiState: State<TranslationState> = _uiState
    
    private var speechRecognitionService: SpeechRecognitionService? = null
    private var textToSpeechService: TextToSpeechService? = null
    private val translationService = TranslationService.getInstance()
    private val libreTranslateService = LibreTranslateService.getInstance()

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
                        translateText(recognizedText, context)
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
                if (success) {
                    loadAvailableVoices()
                }
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

    private fun loadAvailableVoices() {
        val sourceVoices = textToSpeechService?.getAvailableVoicesForLanguage(_uiState.value.sourceLanguage.code) ?: emptyList()
        val targetVoices = textToSpeechService?.getAvailableVoicesForLanguage(_uiState.value.targetLanguage.code) ?: emptyList()
        
        _uiState.value = _uiState.value.copy(
            availableSourceVoices = sourceVoices,
            availableTargetVoices = targetVoices
        )
    }

    fun updateSourceVoice(voiceName: String) {
        _uiState.value = _uiState.value.copy(sourceVoice = voiceName)
    }

    fun updateTargetVoice(voiceName: String) {
        _uiState.value = _uiState.value.copy(targetVoice = voiceName)
    }

    fun getAvailableVoicesForLanguage(languageCode: String): List<VoiceInfo> {
        return textToSpeechService?.getAvailableVoicesForLanguage(languageCode) ?: emptyList()
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

    fun updateOriginalText(text: String, context: Context? = null) {
        _uiState.value = _uiState.value.copy(originalText = text)
        if (text.isNotEmpty()) {
            translateText(text, context)
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun translateText(text: String, context: Context? = null) {
        if (text.isBlank()) return
        
        // Verificar conectividad si tenemos contexto
        if (context != null && !isNetworkAvailable(context)) {
            _uiState.value = _uiState.value.copy(
                translatedText = text, // Mostrar el texto original en la caja de abajo
                errorMessage = "No hay conexión a internet. Verifica tu conexión e inténtalo de nuevo."
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isTranslating = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            try {
                val sourceCode = ApiConfig.getLanguageCodeForService(_uiState.value.sourceLanguage.code)
                val targetCode = ApiConfig.getLanguageCodeForService(_uiState.value.targetLanguage.code)
                
                val result = if (ApiConfig.USE_LIBRE_TRANSLATE || !ApiConfig.hasValidGoogleApiKey()) {
                    // Usar LibreTranslate (gratuito)
                    libreTranslateService.translateText(
                        text = text,
                        sourceLanguage = sourceCode,
                        targetLanguage = targetCode
                    )
                } else {
                    // Usar Google Translate API (requiere API key)
                    translationService.translateText(
                        text = text,
                        sourceLanguage = sourceCode,
                        targetLanguage = targetCode,
                        apiKey = ApiConfig.getGoogleTranslateApiKey()
                    )
                }
                
                result.fold(
                    onSuccess = { translatedText ->
                        _uiState.value = _uiState.value.copy(
                            isTranslating = false,
                            translatedText = translatedText,
                            errorMessage = null
                        )
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Error desconocido de traducción"
                        // Mostrar el texto original en la caja de traducción cuando hay error
                        _uiState.value = _uiState.value.copy(
                            isTranslating = false,
                            translatedText = text, // Mostrar el texto original en la caja de abajo
                            errorMessage = "Error de traducción: $errorMessage"
                        )
                    }
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error inesperado desconocido"
                // Mostrar el texto original en la caja de traducción cuando hay error
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    translatedText = text, // Mostrar el texto original en la caja de abajo
                    errorMessage = "Error inesperado: $errorMessage"
                )
            }
        }
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

    fun updateSourceLanguage(language: Language, context: Context? = null) {
        _uiState.value = _uiState.value.copy(
            sourceLanguage = language,
            sourceVoice = null // Resetear voz al cambiar idioma
        )
        loadAvailableVoices()
        if (_uiState.value.originalText.isNotEmpty()) {
            translateText(_uiState.value.originalText, context)
        }
    }

    fun updateTargetLanguage(language: Language, context: Context? = null) {
        _uiState.value = _uiState.value.copy(
            targetLanguage = language,
            targetVoice = null // Resetear voz al cambiar idioma
        )
        loadAvailableVoices()
        if (_uiState.value.originalText.isNotEmpty()) {
            translateText(_uiState.value.originalText, context)
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
        
        textToSpeechService?.speak(originalText, _uiState.value.sourceLanguage.code, _uiState.value.sourceVoice)
    }
    
    fun playTranslation() {
        val translatedText = _uiState.value.translatedText
        if (translatedText.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No hay texto para reproducir")
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
        
        // Si hay error de traducción, reproducir en el idioma original
        val languageToUse = if (_uiState.value.errorMessage != null && 
            translatedText == _uiState.value.originalText) {
            _uiState.value.sourceLanguage.code
        } else {
            _uiState.value.targetLanguage.code
        }
        
        // Usar la voz configurada para el idioma correspondiente
        val voiceToUse = if (_uiState.value.errorMessage != null && 
            translatedText == _uiState.value.originalText) {
            _uiState.value.sourceVoice
        } else {
            _uiState.value.targetVoice
        }
        
        textToSpeechService?.speak(translatedText, languageToUse, voiceToUse)
    }
    
    fun stopSpeaking() {
        textToSpeechService?.stop()
        _uiState.value = _uiState.value.copy(
            isSpeakingOriginal = false,
            isSpeakingTranslation = false
        )
    }
    
    fun testVoice(voiceName: String, sampleText: String, languageCode: String) {
        if (!_uiState.value.isTtsReady) {
            _uiState.value = _uiState.value.copy(errorMessage = "Text-to-Speech no está listo")
            return
        }
        
        // Detener cualquier reproducción anterior
        textToSpeechService?.stop()
        
        textToSpeechService?.speak(sampleText, languageCode, voiceName)
    }
    
    override fun onCleared() {
        super.onCleared()
        speechRecognitionService?.destroy()
        textToSpeechService?.destroy()
    }
} 