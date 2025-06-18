package com.factordev.traslator.api

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TranslationService private constructor() {
    
    private val api: TranslationApi
    
    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(TranslationApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        api = retrofit.create(TranslationApi::class.java)
    }
    
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        apiKey: String
    ): Result<String> {
        return try {
            val response = api.translateText(
                text = text,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                apiKey = apiKey
            )
            
            if (response.isSuccessful) {
                val translatedText = response.body()?.data?.translations?.firstOrNull()?.translatedText
                if (translatedText != null) {
                    Result.success(translatedText)
                } else {
                    Result.failure(Exception("No se pudo obtener la traducción"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                    apiError.error.message
                } catch (e: Exception) {
                    "Error en la traducción: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No hay conexión a internet"
                is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
                is java.net.ConnectException -> "No se pudo conectar al servidor"
                else -> e.message ?: "Error de conexión desconocido"
            }
            Result.failure(Exception("Error de red: $errorMessage"))
        }
    }
    
    suspend fun detectLanguage(
        text: String,
        apiKey: String
    ): Result<String> {
        return try {
            val response = api.detectLanguage(text, apiKey)
            
            if (response.isSuccessful) {
                val detectedLanguage = response.body()?.data?.detections?.firstOrNull()?.firstOrNull()?.language
                if (detectedLanguage != null) {
                    Result.success(detectedLanguage)
                } else {
                    Result.failure(Exception("No se pudo detectar el idioma"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                    apiError.error.message
                } catch (e: Exception) {
                    "Error en la detección: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No hay conexión a internet"
                is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
                is java.net.ConnectException -> "No se pudo conectar al servidor"
                else -> e.message ?: "Error de conexión desconocido"
            }
            Result.failure(Exception("Error de red: $errorMessage"))
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: TranslationService? = null
        
        fun getInstance(): TranslationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationService().also { INSTANCE = it }
            }
        }
    }
}

// Servicio alternativo gratuito usando LibreTranslate
class LibreTranslateService private constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    // Lista de servidores LibreTranslate disponibles (orden de preferencia)
    private val servers = listOf(
        "https://libretranslate.com/translate",
        "https://translate.argosopentech.com/translate",
        "https://libretranslate.pussthecat.org/translate"
    )
    
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String> {
        
        // Validar que los idiomas estén soportados
        if (!ApiConfig.isLanguageSupportedByLibreTranslate(sourceLanguage) || 
            !ApiConfig.isLanguageSupportedByLibreTranslate(targetLanguage)) {
            return Result.failure(Exception("Idioma no soportado por LibreTranslate. Idiomas disponibles: ${ApiConfig.LIBRE_TRANSLATE_SUPPORTED_LANGUAGES.joinToString(", ")}"))
        }
        
        // Intentar con cada servidor hasta que uno funcione
        for (serverUrl in servers) {
            try {
                val result = tryTranslateWithServer(text, sourceLanguage, targetLanguage, serverUrl)
                if (result.isSuccess) {
                    return result
                }
            } catch (e: Exception) {
                // Continuar con el siguiente servidor
                continue
            }
        }
        
        return Result.failure(Exception("No se pudo conectar a ningún servidor de LibreTranslate. Verifica tu conexión a internet."))
    }
    
    private suspend fun tryTranslateWithServer(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        serverUrl: String
    ): Result<String> {
        return try {
            val jsonBody = """
                {
                    "q": "${text.replace("\"", "\\\"")}",
                    "source": "$sourceLanguage",
                    "target": "$targetLanguage",
                    "format": "text"
                }
            """.trimIndent()
            
            val request = okhttp3.Request.Builder()
                .url(serverUrl)
                .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "TraslatorApp/1.0")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    try {
                        val gson = Gson()
                        // Intenta primero con el formato estándar
                        try {
                            val result = gson.fromJson(responseBody, LibreTranslateResponse::class.java)
                            if (result.translatedText.isNotEmpty()) {
                                Result.success(result.translatedText)
                            } else {
                                Result.failure(Exception("Respuesta vacía del servidor"))
                            }
                        } catch (e1: Exception) {
                            // Intenta con el formato alternativo
                            try {
                                val resultAlt = gson.fromJson(responseBody, LibreTranslateResponseAlt::class.java)
                                val translatedText = resultAlt.translatedText 
                                    ?: resultAlt.translations?.firstOrNull()?.text
                                if (!translatedText.isNullOrEmpty()) {
                                    Result.success(translatedText)
                                } else {
                                    throw Exception("No se encontró texto traducido en la respuesta")
                                }
                            } catch (e2: Exception) {
                                // Si es solo texto plano, úsalo directamente
                                val cleanText = responseBody.trim().removeSurrounding("\"")
                                if (cleanText.isNotEmpty() && !cleanText.startsWith("{") && !cleanText.startsWith("[")) {
                                    Result.success(cleanText)
                                } else {
                                    Result.failure(Exception("Formato de respuesta no reconocido: $responseBody"))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Result.failure(Exception("Error procesando la respuesta del servidor $serverUrl: ${e.message}"))
                    }
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.body?.string()
                Result.failure(Exception("Error de traducción: ${response.code} - ${errorBody ?: "Error desconocido"}"))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "No hay conexión a internet"
                is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
                is java.net.ConnectException -> "No se pudo conectar al servidor"
                else -> e.message ?: "Error de conexión desconocido"
            }
            Result.failure(Exception("Error de red: $errorMessage"))
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: LibreTranslateService? = null
        
        fun getInstance(): LibreTranslateService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LibreTranslateService().also { INSTANCE = it }
            }
        }
    }
}

data class LibreTranslateResponse(
    val translatedText: String
)

// Modelo alternativo para respuestas de LibreTranslate
data class LibreTranslateResponseAlt(
    val translations: List<LibreTranslateTranslation>? = null,
    val translatedText: String? = null
)

data class LibreTranslateTranslation(
    val text: String
) 