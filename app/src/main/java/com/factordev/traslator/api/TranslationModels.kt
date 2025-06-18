package com.factordev.traslator.api

import com.google.gson.annotations.SerializedName

// Modelo de petición para traducción
data class TranslationRequest(
    @SerializedName("q") val text: String,
    @SerializedName("source") val sourceLanguage: String,
    @SerializedName("target") val targetLanguage: String,
    @SerializedName("format") val format: String = "text"
)

// Modelo de respuesta de traducción
data class TranslationResponse(
    @SerializedName("data") val data: TranslationData
)

data class TranslationData(
    @SerializedName("translations") val translations: List<Translation>
)

data class Translation(
    @SerializedName("translatedText") val translatedText: String,
    @SerializedName("detectedSourceLanguage") val detectedSourceLanguage: String? = null
)

// Modelo de error de API
data class ApiError(
    @SerializedName("error") val error: ErrorDetails
)

data class ErrorDetails(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String
)

// Modelo para detección de idioma
data class DetectionRequest(
    @SerializedName("q") val text: String
)

data class DetectionResponse(
    @SerializedName("data") val data: DetectionData
)

data class DetectionData(
    @SerializedName("detections") val detections: List<List<Detection>>
)

data class Detection(
    @SerializedName("language") val language: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("isReliable") val isReliable: Boolean
) 