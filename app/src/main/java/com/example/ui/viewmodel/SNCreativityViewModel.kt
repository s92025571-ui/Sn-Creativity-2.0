package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.config.AiConnectionMode
import com.example.config.AiEnvironmentStatus
import com.example.data.local.PromptHistoryEntity
import com.example.data.local.PromptHistoryRepository
import com.example.data.local.SNCreativityDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserPreferencesState
import com.example.data.remote.GeminiNetworkModule
import com.example.data.repository.AiServiceResult
import com.example.data.repository.CreatorAiRepository
import com.example.domain.catalog.CreatorToolCatalog
import com.example.domain.model.AiWorkflowPreset
import com.example.domain.model.AspectFormat
import com.example.domain.model.CreatorPlatform
import com.example.domain.model.CreatorTone
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.GenerationOutcome
import com.example.domain.model.GenerationParameters
import com.example.domain.model.OutputDetailLevel
import com.example.domain.model.PromptEnhancerOutputType
import com.example.domain.model.StudioSection
import com.example.domain.model.VideoDurationPreset
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

sealed class ToolExecutionState {
    data object Idle : ToolExecutionState()
    data object Generating : ToolExecutionState()
    data class Success(val outcome: GenerationOutcome, val historyId: Long) : ToolExecutionState()
    data class SetupRequired(val message: String) : ToolExecutionState()
    data class Error(
        val title: String,
        val message: String,
        val canUseLocalFallback: Boolean = true
    ) : ToolExecutionState()
}

data class ActiveToolFormState(
    val toolId: String = "gaming_titles",
    val primaryInput: String = "",
    val secondaryInput: String = "",
    val language: GenerationLanguage = GenerationLanguage.ENGLISH,
    val detailLevel: OutputDetailLevel = OutputDetailLevel.DETAILED,
    val tone: CreatorTone = CreatorTone.ENERGETIC,
    val aspectFormat: AspectFormat = AspectFormat.VERTICAL_9_16,
    val durationPreset: VideoDurationPreset = VideoDurationPreset.SHORT_60S,
    val platform: CreatorPlatform = CreatorPlatform.YOUTUBE,
    val workflowPreset: AiWorkflowPreset = AiWorkflowPreset.STANDARD,
    val enhancerOutputType: PromptEnhancerOutputType = PromptEnhancerOutputType.IMAGE_GENERATION,
    val visualStyle: String = "",
    val specialRequirements: String = "",
    val preserveReferenceConstraints: Boolean = true,
    val enableWebResearchGrounding: Boolean = false,
    val regenerationCount: Int = 0,
    val currentSavedFavorite: Boolean = false,
    val executionState: ToolExecutionState = ToolExecutionState.Idle
)

class SNCreativityViewModel(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository,
    private val historyRepository: PromptHistoryRepository,
    private val aiRepository: CreatorAiRepository
) : AndroidViewModel(application) {

    val preferencesState: StateFlow<UserPreferencesState> =
        preferencesRepository.preferencesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferencesState()
        )

    val allHistory: StateFlow<List<PromptHistoryEntity>> =
        historyRepository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _toolFormState = MutableStateFlow(ActiveToolFormState())
    val toolFormState: StateFlow<ActiveToolFormState> = _toolFormState.asStateFlow()

    private val _aiConnectionMode = MutableStateFlow(AiEnvironmentStatus.currentMode())
    val aiConnectionMode: StateFlow<AiConnectionMode> = _aiConnectionMode.asStateFlow()

    private var activeGenerationJob: Job? = null

    fun refreshConnectionStatus() {
        _aiConnectionMode.value = AiEnvironmentStatus.currentMode()
    }

    fun initializeToolSession(toolId: String, historyEntryId: Long = -1L) {
        activeGenerationJob?.cancel()
        val preferredLang = preferencesState.value.preferredLanguage
        val tool = CreatorToolCatalog.findToolById(toolId)

        val defaultWorkflow = when (toolId) {
            "video_gen_prompts", "workflow_presets_builder" -> AiWorkflowPreset.GOOGLE_FLOW
            "image_gen_prompts" -> AiWorkflowPreset.MIDJOURNEY_IMAGE
            "thumbnail_prompts" -> AiWorkflowPreset.VIRAL_THUMBNAIL
            else -> AiWorkflowPreset.STANDARD
        }

        val defaultAspect = when (toolId) {
            "thumbnail_prompts", "gaming_descriptions", "video_descriptions" ->
                AspectFormat.HORIZONTAL_16_9
            else -> AspectFormat.VERTICAL_9_16
        }

        if (historyEntryId > 0L) {
            viewModelScope.launch {
                val entry = historyRepository.getEntry(historyEntryId)
                if (entry != null) {
                    val decoded = runCatching {
                        GeminiNetworkModule.jsonSerializer.decodeFromString<GenerationParameters>(
                            entry.parametersJson
                        )
                    }.getOrNull()

                    _toolFormState.value = ActiveToolFormState(
                        toolId = entry.toolId,
                        primaryInput = entry.userInput,
                        secondaryInput = entry.secondaryInput,
                        language = GenerationLanguage.fromCode(decoded?.languageCode ?: preferredLang.code),
                        detailLevel = runCatching {
                            OutputDetailLevel.valueOf(decoded?.detailLevel ?: "")
                        }.getOrDefault(OutputDetailLevel.DETAILED),
                        tone = runCatching {
                            CreatorTone.valueOf(decoded?.tone ?: "")
                        }.getOrDefault(CreatorTone.ENERGETIC),
                        aspectFormat = AspectFormat.entries.firstOrNull {
                            it.ratio == decoded?.aspectRatio
                        } ?: defaultAspect,
                        durationPreset = VideoDurationPreset.entries.firstOrNull {
                            it.label == decoded?.durationLabel
                        } ?: VideoDurationPreset.SHORT_60S,
                        platform = CreatorPlatform.entries.firstOrNull {
                            it.label == decoded?.platform
                        } ?: CreatorPlatform.YOUTUBE,
                        workflowPreset = AiWorkflowPreset.entries.firstOrNull {
                            it.id == decoded?.workflowPresetId
                        } ?: defaultWorkflow,
                        enhancerOutputType = runCatching {
                            PromptEnhancerOutputType.valueOf(decoded?.enhancerOutputType ?: "")
                        }.getOrDefault(PromptEnhancerOutputType.IMAGE_GENERATION),
                        visualStyle = decoded?.visualStyle.orEmpty(),
                        specialRequirements = decoded?.specialRequirements.orEmpty(),
                        preserveReferenceConstraints = decoded?.preserveReferenceConstraints ?: true,
                        enableWebResearchGrounding = decoded?.enableWebResearchGrounding ?: false,
                        currentSavedFavorite = entry.isSavedFavorite,
                        executionState = ToolExecutionState.Idle
                    )
                }
            }
        } else {
            _toolFormState.value = ActiveToolFormState(
                toolId = tool?.id ?: toolId,
                language = preferredLang,
                aspectFormat = defaultAspect,
                workflowPreset = defaultWorkflow,
                enableWebResearchGrounding = tool?.supportsWebGrounding == true
            )
        }
    }

    fun updatePrimaryInput(value: String) {
        _toolFormState.update { it.copy(primaryInput = value) }
    }

    fun updateSecondaryInput(value: String) {
        _toolFormState.update { it.copy(secondaryInput = value) }
    }

    fun updateToolLanguage(language: GenerationLanguage) {
        _toolFormState.update { it.copy(language = language) }
    }

    fun updateDetailLevel(level: OutputDetailLevel) {
        _toolFormState.update { it.copy(detailLevel = level) }
    }

    fun updateTone(tone: CreatorTone) {
        _toolFormState.update { it.copy(tone = tone) }
    }

    fun updateAspectFormat(format: AspectFormat) {
        _toolFormState.update { it.copy(aspectFormat = format) }
    }

    fun updateDurationPreset(preset: VideoDurationPreset) {
        _toolFormState.update { it.copy(durationPreset = preset) }
    }

    fun updatePlatform(platform: CreatorPlatform) {
        _toolFormState.update { it.copy(platform = platform) }
    }

    fun updateWorkflowPreset(preset: AiWorkflowPreset) {
        _toolFormState.update { it.copy(workflowPreset = preset) }
    }

    fun updateEnhancerOutputType(type: PromptEnhancerOutputType) {
        _toolFormState.update { it.copy(enhancerOutputType = type) }
    }

    fun updateVisualStyle(style: String) {
        _toolFormState.update { it.copy(visualStyle = style) }
    }

    fun updateSpecialRequirements(req: String) {
        _toolFormState.update { it.copy(specialRequirements = req) }
    }

    fun updatePreserveReferenceConstraints(preserve: Boolean) {
        _toolFormState.update { it.copy(preserveReferenceConstraints = preserve) }
    }

    fun updateWebResearchGrounding(enabled: Boolean) {
        _toolFormState.update { it.copy(enableWebResearchGrounding = enabled) }
    }

    fun generateContent(isRegenerate: Boolean = false) {
        val current = _toolFormState.value
        val trimmedInput = current.primaryInput.trim()
        if (trimmedInput.isEmpty()) {
            _toolFormState.update {
                it.copy(
                    executionState = ToolExecutionState.Error(
                        title = "Input Required",
                        message = "Please enter your topic, idea, or prompt instruction above, or tap one of the quick idea chips.",
                        canUseLocalFallback = false
                    )
                )
            }
            return
        }

        activeGenerationJob?.cancel()
        val nextVariation = if (isRegenerate) current.regenerationCount + 1 else 0
        val params = buildGenerationParameters(current)

        // Check if user explicitly turned on Auto Local Fallback when Gemini is unconfigured
        val mode = AiEnvironmentStatus.currentMode()
        if (mode == AiConnectionMode.UNCONFIGURED_LOCAL_ONLY &&
            preferencesState.value.autoFallbackToLocalOnUnconfigured
        ) {
            generateUsingLocalFallback(isRegenerate = isRegenerate)
            return
        }

        _toolFormState.update {
            it.copy(
                regenerationCount = nextVariation,
                currentSavedFavorite = false,
                executionState = ToolExecutionState.Generating
            )
        }

        activeGenerationJob = viewModelScope.launch {
            try {
                when (val result = aiRepository.generateWithGemini(params, nextVariation)) {
                    is AiServiceResult.Success -> {
                        val savedId = persistHistory(params, result.outcome)
                        _toolFormState.update {
                            it.copy(
                                executionState = ToolExecutionState.Success(
                                    outcome = result.outcome,
                                    historyId = savedId
                                )
                            )
                        }
                    }

                    is AiServiceResult.SetupRequired -> {
                        _toolFormState.update {
                            it.copy(
                                executionState = ToolExecutionState.SetupRequired(result.message)
                            )
                        }
                    }

                    is AiServiceResult.Failure -> {
                        _toolFormState.update {
                            it.copy(
                                executionState = ToolExecutionState.Error(
                                    title = result.title,
                                    message = result.message,
                                    canUseLocalFallback = true
                                )
                            )
                        }
                    }
                }
            } catch (_: CancellationException) {
                _toolFormState.update { state ->
                    if (state.executionState is ToolExecutionState.Generating) {
                        state.copy(executionState = ToolExecutionState.Idle)
                    } else state
                }
            }
        }
    }

    fun generateUsingLocalFallback(isRegenerate: Boolean = false) {
        val current = _toolFormState.value
        if (current.primaryInput.trim().isEmpty()) {
            _toolFormState.update {
                it.copy(
                    executionState = ToolExecutionState.Error(
                        title = "Input Required",
                        message = "Enter a topic or select a quick chip first.",
                        canUseLocalFallback = false
                    )
                )
            }
            return
        }
        activeGenerationJob?.cancel()
        val nextVariation = if (isRegenerate) current.regenerationCount + 1 else current.regenerationCount
        val params = buildGenerationParameters(current)

        viewModelScope.launch {
            val outcome = aiRepository.generateLocalFallback(params, nextVariation)
            val savedId = persistHistory(params, outcome)
            _toolFormState.update {
                it.copy(
                    regenerationCount = nextVariation,
                    currentSavedFavorite = false,
                    executionState = ToolExecutionState.Success(
                        outcome = outcome,
                        historyId = savedId
                    )
                )
            }
        }
    }

    fun cancelActiveGeneration() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _toolFormState.update {
            it.copy(executionState = ToolExecutionState.Idle)
        }
    }

    fun toggleCurrentResultSaved() {
        val state = _toolFormState.value
        val success = state.executionState as? ToolExecutionState.Success ?: return
        val newSaved = !state.currentSavedFavorite
        _toolFormState.update { it.copy(currentSavedFavorite = newSaved) }
        viewModelScope.launch {
            historyRepository.toggleFavorite(success.historyId, newSaved)
        }
    }

    fun toggleHistoryFavorite(id: Long, currentlySaved: Boolean) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(id, !currentlySaved)
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteEntry(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setPreferredLanguage(language: GenerationLanguage) {
        viewModelScope.launch {
            preferencesRepository.setPreferredLanguage(language)
            _toolFormState.update { it.copy(language = language) }
        }
    }

    fun setEnable3DMotion(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setEnable3DMotion(enabled)
        }
    }

    fun setAutoLocalFallback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoLocalFallback(enabled)
        }
    }

    private fun buildGenerationParameters(state: ActiveToolFormState): GenerationParameters {
        val tool = CreatorToolCatalog.findToolById(state.toolId)
        val section = tool?.section ?: StudioSection.GAMING_STUDIO
        return GenerationParameters(
            toolId = state.toolId,
            toolTitle = tool?.title ?: "Creator Tool",
            sectionId = section.id,
            primaryInput = state.primaryInput.trim(),
            secondaryInput = state.secondaryInput.trim(),
            languageCode = state.language.code,
            detailLevel = state.detailLevel.name,
            tone = state.tone.name,
            aspectRatio = state.aspectFormat.ratio,
            durationLabel = state.durationPreset.label,
            platform = state.platform.label,
            workflowPresetId = state.workflowPreset.id,
            enhancerOutputType = state.enhancerOutputType.name,
            visualStyle = state.visualStyle.trim(),
            specialRequirements = state.specialRequirements.trim(),
            preserveReferenceConstraints = state.preserveReferenceConstraints,
            enableWebResearchGrounding = state.enableWebResearchGrounding
        )
    }

    private suspend fun persistHistory(
        params: GenerationParameters,
        outcome: GenerationOutcome
    ): Long {
        val tool = CreatorToolCatalog.findToolById(params.toolId)
        val sectionTitle = tool?.section?.title ?: "SN Studio"
        val summary = buildString {
            append(GenerationLanguage.fromCode(params.languageCode).label)
            append(" • ")
            append(params.platform)
            append(" • ")
            append(params.aspectRatio)
        }
        val encodedJson = runCatching {
            GeminiNetworkModule.jsonSerializer.encodeToString(params)
        }.getOrDefault("{}")

        return historyRepository.saveEntry(
            PromptHistoryEntity(
                toolId = params.toolId,
                toolTitle = params.toolTitle,
                sectionId = params.sectionId,
                sectionTitle = sectionTitle,
                userInput = params.primaryInput,
                secondaryInput = params.secondaryInput,
                parametersSummary = summary,
                parametersJson = encodedJson,
                generatedOutput = outcome.content,
                sourceBadge = outcome.source.label
            )
        )
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = SNCreativityDatabase.getInstance(application)
            val historyRepo = PromptHistoryRepository(db.promptHistoryDao())
            val prefsRepo = UserPreferencesRepository(application)
            val aiRepo = CreatorAiRepository()
            return SNCreativityViewModel(
                application = application,
                preferencesRepository = prefsRepo,
                historyRepository = historyRepo,
                aiRepository = aiRepo
            ) as T
        }
    }
}
