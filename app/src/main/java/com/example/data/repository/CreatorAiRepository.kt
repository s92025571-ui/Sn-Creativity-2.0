package com.example.data.repository

import com.example.config.AiConnectionMode
import com.example.config.AiEnvironmentStatus
import com.example.config.OwnerSupportConfig
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateContentRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiNetworkModule
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRestApi
import com.example.data.remote.SecureBackendProxyRequest
import com.example.domain.generator.LocalTemplateFallbackGenerator
import com.example.domain.model.AiWorkflowPreset
import com.example.domain.model.CreatorTone
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.GenerationOutcome
import com.example.domain.model.GenerationParameters
import com.example.domain.model.GenerationSourceBadge
import com.example.domain.model.GroundingCitation
import com.example.domain.model.OutputDetailLevel
import com.example.domain.model.PromptEnhancerOutputType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class AiServiceResult {
    data class Success(val outcome: GenerationOutcome) : AiServiceResult()

    data class SetupRequired(
        val message: String,
        val fallbackPreviewAvailable: Boolean = true
    ) : AiServiceResult()

    data class Failure(
        val title: String,
        val message: String,
        val isQuotaOrRateLimit: Boolean = false,
        val isNetworkError: Boolean = false
    ) : AiServiceResult()
}

class CreatorAiRepository(
    private val api: GeminiRestApi = GeminiNetworkModule.api
) {

    suspend fun generateWithGemini(
        params: GenerationParameters,
        variationIndex: Int = 0
    ): AiServiceResult = withContext(Dispatchers.IO) {
        val mode = AiEnvironmentStatus.currentMode()
        if (mode == AiConnectionMode.UNCONFIGURED_LOCAL_ONLY) {
            return@withContext AiServiceResult.SetupRequired(
                message = "Gemini AI is not configured yet. Configure GEMINI_BACKEND_PROXY_URL (recommended for production) or GEMINI_API_KEY in the AI Studio Secrets panel / .env file, or tap 'Use Local Studio Template' below for offline fallback."
            )
        }

        val systemInstruction = buildSystemPrompt(params)
        val userPrompt = buildUserPrompt(params, variationIndex)
        val temperature = if (variationIndex > 0) 0.88f else 0.76f

        try {
            when (mode) {
                AiConnectionMode.SECURE_BACKEND_PROXY -> {
                    val proxyUrl = AiEnvironmentStatus.getBackendProxyUrl()
                    val response = api.callSecureBackendProxy(
                        proxyEndpointUrl = proxyUrl,
                        request = SecureBackendProxyRequest(
                            model = OwnerSupportConfig.GEMINI_DEFAULT_MODEL,
                            systemPrompt = systemInstruction,
                            userPrompt = userPrompt,
                            enableSearchGrounding = params.enableWebResearchGrounding,
                            temperature = temperature
                        )
                    )
                    if (!response.isSuccessful) {
                        return@withContext mapHttpError(response.code(), response.errorBody()?.string())
                    }
                    val body = response.body()
                    if (body == null || (!body.error.isNullOrBlank())) {
                        return@withContext AiServiceResult.Failure(
                            title = "Backend Proxy Error",
                            message = body?.error ?: "Secure backend returned an empty response."
                        )
                    }
                    val text = body.text?.trim().orEmpty()
                    if (text.isEmpty()) {
                        return@withContext AiServiceResult.Failure(
                            title = "Invalid AI Response",
                            message = "The backend returned an empty generation payload."
                        )
                    }
                    val citations = body.sources.orEmpty().mapNotNull { src ->
                        val url = src.uri?.trim().orEmpty()
                        if (url.isNotEmpty()) {
                            GroundingCitation(
                                title = src.title?.takeIf { it.isNotBlank() } ?: url,
                                url = url
                            )
                        } else null
                    }
                    AiServiceResult.Success(
                        GenerationOutcome(
                            content = text,
                            source = GenerationSourceBadge.SECURE_PROXY,
                            modelUsed = body.model ?: OwnerSupportConfig.GEMINI_DEFAULT_MODEL,
                            groundingSources = citations
                        )
                    )
                }

                AiConnectionMode.DIRECT_GEMINI_API -> {
                    val apiKey = AiEnvironmentStatus.getDirectApiKey()
                    val tools = if (params.enableWebResearchGrounding) {
                        listOf(
                            buildJsonObject {
                                put("google_search", buildJsonObject {})
                            }
                        )
                    } else {
                        null
                    }

                    val request = GeminiGenerateContentRequest(
                        contents = listOf(
                            GeminiContent(
                                role = "user",
                                parts = listOf(GeminiPart(text = userPrompt))
                            )
                        ),
                        systemInstruction = GeminiContent(
                            parts = listOf(GeminiPart(text = systemInstruction))
                        ),
                        generationConfig = GeminiGenerationConfig(
                            temperature = temperature,
                            topP = 0.95f,
                            topK = 40,
                            maxOutputTokens = 2048
                        ),
                        tools = tools
                    )

                    val response = api.generateContent(
                        model = OwnerSupportConfig.GEMINI_DEFAULT_MODEL,
                        apiKey = apiKey,
                        request = request
                    )

                    if (!response.isSuccessful) {
                        // If google_search grounding tool is not supported on the user's tier/key,
                        // transparently inform and retry without grounding ONLY if grounding caused 400.
                        val errorText = response.errorBody()?.string().orEmpty()
                        if (params.enableWebResearchGrounding && response.code() == 400 &&
                            errorText.contains("google_search", ignoreCase = true)
                        ) {
                            return@withContext retryWithoutSearchGrounding(
                                apiKey = apiKey,
                                systemInstruction = systemInstruction,
                                userPrompt = userPrompt,
                                temperature = temperature
                            )
                        }
                        return@withContext mapHttpError(response.code(), errorText)
                    }

                    val payload = response.body()
                    val blockReason = payload?.promptFeedback?.blockReason
                    if (!blockReason.isNullOrBlank()) {
                        return@withContext AiServiceResult.Failure(
                            title = "Request Blocked by Safety Filter",
                            message = "Gemini declined this request ($blockReason). Try rephrasing your topic."
                        )
                    }

                    val candidate = payload?.candidates?.firstOrNull()
                    val generatedText = candidate?.content?.parts
                        ?.mapNotNull { it.text }
                        ?.joinToString("\n")
                        ?.trim()
                        .orEmpty()

                    if (generatedText.isEmpty()) {
                        return@withContext AiServiceResult.Failure(
                            title = "Empty Gemini Response",
                            message = "Gemini returned no text output (finishReason: ${candidate?.finishReason ?: "unknown"}). Please retry."
                        )
                    }

                    val citations = candidate?.groundingMetadata?.groundingChunks
                        .orEmpty()
                        .mapNotNull { chunk ->
                            val uri = chunk.web?.uri?.trim().orEmpty()
                            if (uri.isNotEmpty()) {
                                GroundingCitation(
                                    title = chunk.web?.title?.takeIf { it.isNotBlank() } ?: uri,
                                    url = uri
                                )
                            } else null
                        }
                        .distinctBy { it.url }

                    val badge = if (params.enableWebResearchGrounding && citations.isNotEmpty()) {
                        GenerationSourceBadge.GEMINI_GROUNDED_WEB
                    } else {
                        GenerationSourceBadge.GEMINI_AI
                    }

                    val warning = if (params.enableWebResearchGrounding && citations.isEmpty()) {
                        "Live web citations were not returned for this query; treat time-sensitive facts as unverified."
                    } else null

                    AiServiceResult.Success(
                        GenerationOutcome(
                            content = generatedText,
                            source = badge,
                            modelUsed = OwnerSupportConfig.GEMINI_DEFAULT_MODEL,
                            groundingSources = citations,
                            warningBanner = warning
                        )
                    )
                }

                AiConnectionMode.UNCONFIGURED_LOCAL_ONLY -> {
                    AiServiceResult.SetupRequired(
                        message = "Gemini setup is required before calling cloud AI."
                    )
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (timeout: SocketTimeoutException) {
            AiServiceResult.Failure(
                title = "Request Timed Out",
                message = "Gemini took too long to respond. Check your internet connection or tap Retry.",
                isNetworkError = true
            )
        } catch (offline: UnknownHostException) {
            AiServiceResult.Failure(
                title = "No Internet Connection",
                message = "Unable to reach Google Gemini servers. Check Wi-Fi / mobile data or use Local Template Fallback.",
                isNetworkError = true
            )
        } catch (io: IOException) {
            AiServiceResult.Failure(
                title = "Network Error",
                message = "Connection interrupted while contacting Gemini (${io.localizedMessage ?: "IO error"}).",
                isNetworkError = true
            )
        } catch (e: Exception) {
            AiServiceResult.Failure(
                title = "Generation Error",
                message = e.localizedMessage ?: "Unexpected error while processing AI response."
            )
        }
    }

    private suspend fun retryWithoutSearchGrounding(
        apiKey: String,
        systemInstruction: String,
        userPrompt: String,
        temperature: Float
    ): AiServiceResult {
        val fallbackRequest = GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "$systemInstruction\nIMPORTANT: Live web search grounding is unavailable on this API key tier. Explicitly note when any recent information cannot be verified live."
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = temperature)
        )
        val retryResponse = api.generateContent(
            model = OwnerSupportConfig.GEMINI_DEFAULT_MODEL,
            apiKey = apiKey,
            request = fallbackRequest
        )
        if (!retryResponse.isSuccessful) {
            return mapHttpError(retryResponse.code(), retryResponse.errorBody()?.string())
        }
        val text = retryResponse.body()?.candidates?.firstOrNull()?.content?.parts
            ?.mapNotNull { it.text }
            ?.joinToString("\n")
            ?.trim()
            .orEmpty()
        if (text.isEmpty()) {
            return AiServiceResult.Failure(
                title = "Invalid Response",
                message = "Gemini returned an empty response."
            )
        }
        return AiServiceResult.Success(
            GenerationOutcome(
                content = text,
                source = GenerationSourceBadge.GEMINI_AI,
                modelUsed = OwnerSupportConfig.GEMINI_DEFAULT_MODEL,
                warningBanner = "Live Google Search grounding is unavailable for this API key tier. Generated using Gemini model knowledge only."
            )
        )
    }

    fun generateLocalFallback(params: GenerationParameters, variationIndex: Int = 0): GenerationOutcome {
        val text = LocalTemplateFallbackGenerator.generate(params, variationIndex)
        return GenerationOutcome(
            content = text,
            source = GenerationSourceBadge.LOCAL_FALLBACK,
            modelUsed = "Local SN Studio Engine",
            warningBanner = "Generated locally using offline creator templates (Gemini Cloud AI was not called)."
        )
    }

    private fun mapHttpError(code: Int, rawErrorBody: String?): AiServiceResult.Failure {
        val snippet = rawErrorBody?.lineSequence()?.take(4)?.joinToString(" ")?.take(180).orEmpty()
        return when (code) {
            400 -> AiServiceResult.Failure(
                title = "Invalid API Request (400)",
                message = "Gemini rejected the request configuration or API key format. Verify GEMINI_API_KEY in AI Studio Secrets."
            )
            401, 403 -> AiServiceResult.Failure(
                title = "API Key / Permission Error ($code)",
                message = "Gemini API access was denied. Ensure your Gemini API key is active and permitted."
            )
            429 -> AiServiceResult.Failure(
                title = "Rate Limit or Quota Reached (429)",
                message = "Gemini free-tier rate limit or quota has been reached. Wait a moment and retry, or use Local Template Fallback.",
                isQuotaOrRateLimit = true
            )
            in 500..599 -> AiServiceResult.Failure(
                title = "Gemini Service Busy ($code)",
                message = "Google Gemini servers are temporarily overloaded. Please retry in a few seconds."
            )
            else -> AiServiceResult.Failure(
                title = "API Error ($code)",
                message = if (snippet.isNotBlank()) "Request failed ($code): $snippet" else "Unexpected HTTP $code response."
            )
        }
    }

    private fun buildSystemPrompt(params: GenerationParameters): String {
        val lang = GenerationLanguage.fromCode(params.languageCode)
        return buildString {
            appendLine("You are SN Creativity AI 2.0, an elite creative director, gaming content strategist, scriptwriter, and AI prompt engineer.")
            appendLine(lang.promptInstruction)
            appendLine("Rules:")
            appendLine("1. Deliver clean, well-structured, immediately copy-ready output without generic meta filler.")
            appendLine("2. Never claim you generated an actual image, video, or audio file — when building image/video/thumbnail prompts, clearly provide copy-ready text prompts.")
            appendLine("3. Never invent reference images. If the user provides reference-image or identity constraints, preserve them faithfully and explicitly.")
            if (!params.enableWebResearchGrounding) {
                appendLine("4. You do NOT have live internet browsing enabled for this request; do not fabricate live breaking news or unverified statistics.")
            } else {
                appendLine("4. Use grounded search results accurately when available and distinguish verified facts from creative suggestions.")
            }
        }
    }

    private fun buildUserPrompt(params: GenerationParameters, variationIndex: Int): String {
        val detail = runCatching { OutputDetailLevel.valueOf(params.detailLevel) }
            .getOrDefault(OutputDetailLevel.DETAILED)
        val tone = runCatching { CreatorTone.valueOf(params.tone) }
            .getOrDefault(CreatorTone.ENERGETIC)
        val preset = AiWorkflowPreset.entries.firstOrNull { it.id == params.workflowPresetId }
            ?: AiWorkflowPreset.STANDARD

        return buildString {
            appendLine("Studio Tool: ${params.toolTitle} (id: ${params.toolId})")
            appendLine("Primary User Input: ${params.primaryInput.trim()}")
            if (params.secondaryInput.isNotBlank()) {
                if (params.toolId == "prompt_enhancer" && params.preserveReferenceConstraints) {
                    appendLine("Strict Reference / Identity Constraints (MUST PRESERVE): ${params.secondaryInput.trim()}")
                } else {
                    appendLine("Additional Context / Highlight: ${params.secondaryInput.trim()}")
                }
            }
            appendLine("Target Platform: ${params.platform}")
            appendLine("Tone & Style: ${tone.label}")
            appendLine("Detail Level: ${detail.label} (${detail.description})")
            appendLine("Format / Aspect Ratio: ${params.aspectRatio}")
            appendLine("Target Duration: ${params.durationLabel}")
            appendLine("Workflow Preset: ${preset.label} (${preset.targetEngine} — ${preset.styleDirective})")

            if (params.toolId == "prompt_enhancer") {
                val outType = runCatching {
                    PromptEnhancerOutputType.valueOf(params.enhancerOutputType)
                }.getOrDefault(PromptEnhancerOutputType.IMAGE_GENERATION)
                appendLine("Prompt Enhancer Target Output Type: ${outType.label}")
                if (params.visualStyle.isNotBlank()) {
                    appendLine("Requested Visual Style: ${params.visualStyle}")
                }
                if (params.specialRequirements.isNotBlank()) {
                    appendLine("Special Requirements: ${params.specialRequirements}")
                }
                appendLine("Task: Transform the user's simple instruction into a master-grade, copy-ready prompt tailored for ${outType.label}. Preserve every original user instruction and reference constraint without inventing unrequested reference files. Provide: (1) Master Copy-Ready Prompt, (2) Preserved Constraints Check, and (3) Pro Settings / Negative Prompt cues.")
            }

            if (variationIndex > 0) {
                appendLine("Regeneration Request #$variationIndex: Take a fresh, distinct creative angle compared to standard obvious phrasing.")
            }
        }
    }
}
