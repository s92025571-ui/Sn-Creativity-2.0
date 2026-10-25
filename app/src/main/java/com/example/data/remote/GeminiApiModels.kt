package com.example.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null,
    val tools: List<JsonObject>? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = 0.78f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 2048
)

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
    val promptFeedback: GeminiPromptFeedback? = null
)

@Serializable
data class GeminiPromptFeedback(
    val blockReason: String? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null,
    val groundingMetadata: GeminiGroundingMetadata? = null
)

@Serializable
data class GeminiGroundingMetadata(
    val webSearchQueries: List<String>? = null,
    val groundingChunks: List<GeminiGroundingChunk>? = null
)

@Serializable
data class GeminiGroundingChunk(
    val web: GeminiWebSource? = null
)

@Serializable
data class GeminiWebSource(
    val uri: String? = null,
    val title: String? = null
)

/**
 * Contract for a secure production backend proxy (e.g., Cloud Run / Firebase Cloud Functions)
 * so the Gemini API secret never needs to live inside a public production APK.
 */
@Serializable
data class SecureBackendProxyRequest(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String,
    val enableSearchGrounding: Boolean = false,
    val temperature: Float = 0.78f
)

@Serializable
data class SecureBackendProxyResponse(
    val text: String? = null,
    val model: String? = null,
    val sources: List<GeminiWebSource>? = null,
    val error: String? = null
)

interface GeminiRestApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): Response<GeminiGenerateContentResponse>

    @POST
    suspend fun callSecureBackendProxy(
        @Url proxyEndpointUrl: String,
        @Body request: SecureBackendProxyRequest
    ): Response<SecureBackendProxyResponse>
}

object GeminiNetworkModule {
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    val jsonSerializer: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val api: GeminiRestApi by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonSerializer.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiRestApi::class.java)
    }
}
