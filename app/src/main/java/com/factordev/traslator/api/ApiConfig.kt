package com.factordev.traslator.api

object ApiConfig {
    
    // IMPORTANTE: En una app real, nunca hardcodees la API key aquí
    // Usa BuildConfig, SharedPreferences o un servidor backend
    private const val GOOGLE_TRANSLATE_API_KEY = "TU_API_KEY_AQUI"
    
    // Por defecto usaremos LibreTranslate que es gratuito
    const val USE_LIBRE_TRANSLATE = true
    
    fun getGoogleTranslateApiKey(): String {
        return GOOGLE_TRANSLATE_API_KEY
    }
    
    fun hasValidGoogleApiKey(): Boolean {
        return GOOGLE_TRANSLATE_API_KEY != "TU_API_KEY_AQUI" && GOOGLE_TRANSLATE_API_KEY.isNotEmpty()
    }
    
    // Mapeo de códigos de idioma para diferentes servicios
    fun getLanguageCodeForService(languageCode: String): String {
        return if (USE_LIBRE_TRANSLATE) {
            // LibreTranslate usa códigos ISO 639-1
            when (languageCode) {
                "es" -> "es"
                "en" -> "en"
                "fr" -> "fr"
                "de" -> "de"
                "it" -> "it"
                "pt" -> "pt"
                "zh" -> "zh"
                "ja" -> "ja"
                else -> "es"
            }
        } else {
            // Google Translate
            when (languageCode) {
                "es" -> "es"
                "en" -> "en"
                "fr" -> "fr"
                "de" -> "de"
                "it" -> "it"
                "pt" -> "pt"
                "zh" -> "zh-cn"
                "ja" -> "ja"
                else -> "es"
            }
        }
    }
    
    // Lista de idiomas soportados por LibreTranslate
    val LIBRE_TRANSLATE_SUPPORTED_LANGUAGES = setOf(
        "es", "en", "fr", "de", "it", "pt", "zh", "ja", "ar", "ru", "ko", "hi"
    )
    
    fun isLanguageSupportedByLibreTranslate(languageCode: String): Boolean {
        return LIBRE_TRANSLATE_SUPPORTED_LANGUAGES.contains(languageCode)
    }
} 