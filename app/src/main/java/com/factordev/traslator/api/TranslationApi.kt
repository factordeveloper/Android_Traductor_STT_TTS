package com.factordev.traslator.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface TranslationApi {
    
    @FormUrlEncoded
    @POST("language/translate/v2")
    suspend fun translateText(
        @Field("q") text: String,
        @Field("source") sourceLanguage: String,
        @Field("target") targetLanguage: String,
        @Field("format") format: String = "text",
        @Query("key") apiKey: String
    ): Response<TranslationResponse>
    
    @FormUrlEncoded
    @POST("language/translate/v2/detect")
    suspend fun detectLanguage(
        @Field("q") text: String,
        @Query("key") apiKey: String
    ): Response<DetectionResponse>
    
    companion object {
        const val BASE_URL = "https://translation.googleapis.com/"
    }
} 