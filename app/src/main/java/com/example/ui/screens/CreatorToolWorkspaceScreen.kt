package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.catalog.CreatorToolCatalog
import com.example.domain.model.AiWorkflowPreset
import com.example.domain.model.AspectFormat
import com.example.domain.model.CreatorPlatform
import com.example.domain.model.CreatorTone
import com.example.domain.model.CreatorToolDefinition
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.GenerationOutcome
import com.example.domain.model.OutputDetailLevel
import com.example.domain.model.PromptEnhancerOutputType
import com.example.domain.model.VideoDurationPreset
import com.example.ui.components.Neon3DActionButton
import com.example.ui.theme.LocalExtendedTheme
import com.example.ui.viewmodel.ActiveToolFormState
import com.example.ui.viewmodel.ToolExecutionState
import com.example.util.ClipboardHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorToolWorkspaceScreen(
    formState: ActiveToolFormState,
    enable3DMotion: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onPrimaryInputChange: (String) -> Unit,
    onSecondaryInputChange: (String) -> Unit,
    onLanguageChange: (GenerationLanguage) -> Unit,
    onDetailChange: (OutputDetailLevel) -> Unit,
    onToneChange: (CreatorTone) -> Unit,
    onAspectChange: (AspectFormat) -> Unit,
    onDurationChange: (VideoDurationPreset) -> Unit,
    onPlatformChange: (CreatorPlatform) -> Unit,
    onWorkflowPresetChange: (AiWorkflowPreset) -> Unit,
    onEnhancerOutputTypeChange: (PromptEnhancerOutputType) -> Unit,
    onVisualStyleChange: (String) -> Unit,
    onSpecialRequirementsChange: (String) -> Unit,
    onPreserveConstraintsChange: (Boolean) -> Unit,
    onWebGroundingChange: (Boolean) -> Unit,
    onGenerate: (Boolean) -> Unit,
    onGenerateLocalFallback: (Boolean) -> Unit,
    onCancelGeneration: () -> Unit,
    onToggleSaveResult: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tool = CreatorToolCatalog.findToolById(formState.toolId) ?: CreatorToolCatalog.allTools.first()
    val meta = studioVisualMeta(tool.section)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = tool.section.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = meta.accent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("tool_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("tool_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .testTag("tool_workspace_scroll"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Idea Starter Chips
                if (tool.quickChips.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Quick Creator Starters",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tool.quickChips.forEachIndexed { idx, sample ->
                                    AssistChip(
                                        onClick = { onPrimaryInputChange(sample) },
                                        label = { Text(sample, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.testTag("quick_chip_$idx")
                                    )
                                }
                            }
                        }
                    }
                }

                // Primary & Secondary Input Fields
                item {
                    InputCardSection(
                        tool = tool,
                        formState = formState,
                        onPrimaryInputChange = onPrimaryInputChange,
                        onSecondaryInputChange = onSecondaryInputChange,
                        onEnhancerOutputTypeChange = onEnhancerOutputTypeChange,
                        onVisualStyleChange = onVisualStyleChange,
                        onSpecialRequirementsChange = onSpecialRequirementsChange,
                        onPreserveConstraintsChange = onPreserveConstraintsChange,
                        onWebGroundingChange = onWebGroundingChange
                    )
                }

                // Creator Presets (Language: English/Roman Urdu, Detail, Tone, Aspect, Duration, Platform, Workflow)
                item {
                    PresetsCardSection(
                        tool = tool,
                        formState = formState,
                        onLanguageChange = onLanguageChange,
                        onDetailChange = onDetailChange,
                        onToneChange = onToneChange,
                        onAspectChange = onAspectChange,
                        onDurationChange = onDurationChange,
                        onPlatformChange = onPlatformChange,
                        onWorkflowPresetChange = onWorkflowPresetChange
                    )
                }

                // Primary Generate & Local Template Buttons
                item {
                    val isGenerating = formState.executionState is ToolExecutionState.Generating
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Neon3DActionButton(
                            text = if (isGenerating) "Generating with Gemini…" else "Generate with Gemini AI",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { onGenerate(false) },
                            enabled = !isGenerating,
                            enable3DMotion = enable3DMotion,
                            isPrimary = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_gemini_button")
                        )

                        OutlinedButton(
                            onClick = { onGenerateLocalFallback(false) },
                            enabled = !isGenerating,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_local_fallback_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Instant Offline Studio Template")
                        }
                    }
                }

                // Execution Result / Loading / Setup / Error Area
                item {
                    ExecutionStatePanel(
                        executionState = formState.executionState,
                        isSaved = formState.currentSavedFavorite,
                        enable3DMotion = enable3DMotion,
                        onCancel = onCancelGeneration,
                        onRetryGemini = { onGenerate(false) },
                        onRegenerate = { isOffline ->
                            if (isOffline) onGenerateLocalFallback(true) else onGenerate(true)
                        },
                        onUseLocalFallback = { onGenerateLocalFallback(false) },
                        onOpenSettings = onOpenSettings,
                        onToggleSave = onToggleSaveResult,
                        onCopyText = { textToCopy ->
                            val ok = ClipboardHelper.copyText(context, tool.title, textToCopy)
                            if (ok) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Copied to clipboard!")
                                }
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun InputCardSection(
    tool: CreatorToolDefinition,
    formState: ActiveToolFormState,
    onPrimaryInputChange: (String) -> Unit,
    onSecondaryInputChange: (String) -> Unit,
    onEnhancerOutputTypeChange: (PromptEnhancerOutputType) -> Unit,
    onVisualStyleChange: (String) -> Unit,
    onSpecialRequirementsChange: (String) -> Unit,
    onPreserveConstraintsChange: (Boolean) -> Unit,
    onWebGroundingChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, LocalExtendedTheme.current.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tool.isPromptEnhancer) {
                Text(
                    text = "Target Output Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PromptEnhancerOutputType.entries.forEach { type ->
                        FilterChip(
                            selected = formState.enhancerOutputType == type,
                            onClick = { onEnhancerOutputTypeChange(type) },
                            label = { Text(type.label) },
                            modifier = Modifier.testTag("enhancer_type_${type.name.lowercase()}")
                        )
                    }
                }
                Text(
                    text = formState.enhancerOutputType.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = formState.primaryInput,
                onValueChange = onPrimaryInputChange,
                label = { Text(tool.inputLabel) },
                placeholder = { Text(tool.inputPlaceholder) },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_primary_input")
            )

            if (tool.secondaryLabel != null) {
                OutlinedTextField(
                    value = formState.secondaryInput,
                    onValueChange = onSecondaryInputChange,
                    label = { Text(tool.secondaryLabel) },
                    placeholder = { Text(tool.secondaryPlaceholder.orEmpty()) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_secondary_input")
                )
            }

            if (tool.isPromptEnhancer) {
                OutlinedTextField(
                    value = formState.visualStyle,
                    onValueChange = onVisualStyleChange,
                    label = { Text("Visual Style (Optional)") },
                    placeholder = { Text("e.g., Cyberpunk Unreal Engine 5, Anime Cel-Shaded, 35mm Film") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enhancer_visual_style_input")
                )

                OutlinedTextField(
                    value = formState.specialRequirements,
                    onValueChange = onSpecialRequirementsChange,
                    label = { Text("Special Requirements (Optional)") },
                    placeholder = { Text("e.g., Leave clean left-side negative space for title text") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enhancer_special_req_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Preserve Reference & Original Constraints",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Never overwrite your subject identity or invent fake reference images",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = formState.preserveReferenceConstraints,
                        onCheckedChange = onPreserveConstraintsChange,
                        modifier = Modifier.testTag("preserve_constraints_switch")
                    )
                }
            }

            if (tool.supportsWebGrounding) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Search Grounding (Live Web)",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Uses Gemini Search Grounding when supported by your API tier; clearly warns if unavailable.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = formState.enableWebResearchGrounding,
                        onCheckedChange = onWebGroundingChange,
                        modifier = Modifier.testTag("web_grounding_switch")
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetsCardSection(
    tool: CreatorToolDefinition,
    formState: ActiveToolFormState,
    onLanguageChange: (GenerationLanguage) -> Unit,
    onDetailChange: (OutputDetailLevel) -> Unit,
    onToneChange: (CreatorTone) -> Unit,
    onAspectChange: (AspectFormat) -> Unit,
    onDurationChange: (VideoDurationPreset) -> Unit,
    onPlatformChange: (CreatorPlatform) -> Unit,
    onWorkflowPresetChange: (AiWorkflowPreset) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, LocalExtendedTheme.current.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Language & Creator Presets",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Language: English & Roman Urdu
            PresetRowTitle("Output Language")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenerationLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = formState.language == lang,
                        onClick = { onLanguageChange(lang) },
                        label = { Text(lang.label) },
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    )
                }
            }

            // Output Detail: Short vs Detailed
            PresetRowTitle("Output Length")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutputDetailLevel.entries.forEach { level ->
                    FilterChip(
                        selected = formState.detailLevel == level,
                        onClick = { onDetailChange(level) },
                        label = { Text(level.label) },
                        modifier = Modifier.testTag("detail_chip_${level.name.lowercase()}")
                    )
                }
            }

            // Tone: Energetic, Cinematic, Professional, Casual
            PresetRowTitle("Tone & Writing Style")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreatorTone.entries.forEach { tone ->
                    FilterChip(
                        selected = formState.tone == tone,
                        onClick = { onToneChange(tone) },
                        label = { Text(tone.label) },
                        modifier = Modifier.testTag("tone_chip_${tone.name.lowercase()}")
                    )
                }
            }

            // Platform
            PresetRowTitle("Creator Platform")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreatorPlatform.entries.forEach { platform ->
                    FilterChip(
                        selected = formState.platform == platform,
                        onClick = { onPlatformChange(platform) },
                        label = { Text(platform.label) }
                    )
                }
            }

            // Aspect Format (9:16 Vertical, 16:9 Horizontal, 1:1)
            if (tool.supportsAspectFormat) {
                PresetRowTitle("Format / Aspect Ratio")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectFormat.entries.forEach { format ->
                        FilterChip(
                            selected = formState.aspectFormat == format,
                            onClick = { onAspectChange(format) },
                            label = { Text(format.label) },
                            modifier = Modifier.testTag("aspect_chip_${format.ratio}")
                        )
                    }
                }
            }

            // Duration Preset
            if (tool.supportsDuration) {
                PresetRowTitle("Target Video Duration")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoDurationPreset.entries.forEach { dur ->
                        FilterChip(
                            selected = formState.durationPreset == dur,
                            onClick = { onDurationChange(dur) },
                            label = { Text(dur.label) }
                        )
                    }
                }
            }

            // AI Workflow Presets (Google Flow, Sora, Image Gen, Thumbnail)
            if (tool.supportsWorkflowPreset) {
                PresetRowTitle("AI Engine Workflow Preset")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiWorkflowPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = formState.workflowPreset == preset,
                            onClick = { onWorkflowPresetChange(preset) },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("workflow_chip_${preset.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetRowTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ExecutionStatePanel(
    executionState: ToolExecutionState,
    isSaved: Boolean,
    enable3DMotion: Boolean,
    onCancel: () -> Unit,
    onRetryGemini: () -> Unit,
    onRegenerate: (Boolean) -> Unit,
    onUseLocalFallback: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSave: () -> Unit,
    onCopyText: (String) -> Unit
) {
    val context = LocalContext.current
    val extended = LocalExtendedTheme.current

    when (executionState) {
        is ToolExecutionState.Idle -> {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter your topic above, customize your language & presets, and tap Generate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        is ToolExecutionState.Generating -> {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loading_indicator_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Gemini AI is crafting your studio output…",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Analyzing tone, platform retention & creator presets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("cancel_generation_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }

        is ToolExecutionState.SetupRequired -> {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.2.dp, extended.statusWarning),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_required_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = extended.statusWarning
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = executionState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onUseLocalFallback,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_use_fallback_button")
                        ) {
                            Text("Use Local Template")
                        }
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("setup_guide_button")
                        ) {
                            Text("Setup Guide")
                        }
                    }
                }
            }
        }

        is ToolExecutionState.Error -> {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("error_state_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = executionState.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = executionState.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRetryGemini,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retry_generation_button")
                        ) {
                            Text(stringResource(R.string.action_retry))
                        }
                        if (executionState.canUseLocalFallback) {
                            OutlinedButton(
                                onClick = onUseLocalFallback,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("error_fallback_button")
                            ) {
                                Text("Local Fallback")
                            }
                        }
                    }
                }
            }
        }

        is ToolExecutionState.Success -> {
            val outcome: GenerationOutcome = executionState.outcome
            val isLocal = outcome.source.name == "LOCAL_FALLBACK"

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_output_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with source badge & bookmark action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Generated Result",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${outcome.source.label} • ${outcome.modelUsed}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("bookmark_result_button")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Remove from Saved" else "Save Result",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!outcome.warningBanner.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = extended.statusWarning.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = outcome.warningBanner,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SelectionContainer {
                        Text(
                            text = outcome.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generated_result_text")
                        )
                    }

                    // Grounded Web Citations if returned by Gemini Search Grounding
                    if (outcome.groundingSources.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "Grounded Web Sources",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        outcome.groundingSources.take(5).forEach { citation ->
                            AssistChip(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(citation.url)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = citation.title,
                                        maxLines = 1
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Open source",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Copy & Regenerate Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Neon3DActionButton(
                            text = stringResource(R.string.action_copy),
                            icon = Icons.Default.ContentCopy,
                            onClick = { onCopyText(outcome.content) },
                            enable3DMotion = enable3DMotion,
                            isPrimary = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_result_button")
                        )
                        Neon3DActionButton(
                            text = stringResource(R.string.action_regenerate),
                            icon = Icons.Default.Refresh,
                            onClick = { onRegenerate(isLocal) },
                            enable3DMotion = enable3DMotion,
                            isPrimary = false,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("regenerate_result_button")
                        )
                    }
                }
            }
        }
    }
}
