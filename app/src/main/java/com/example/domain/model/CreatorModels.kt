package com.example.domain.model

import kotlinx.serialization.Serializable

enum class StudioSection(
    val id: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val badge: String
) {
    GAMING_STUDIO(
        id = "gaming_studio",
        title = "Gaming Studio",
        shortTitle = "Gaming",
        subtitle = "Titles, viral tags, gameplay ideas, captions & pro tips",
        badge = "5 TOOLS"
    ),
    AI_PROMPT_STUDIO(
        id = "ai_prompt_studio",
        title = "AI Prompt Studio",
        shortTitle = "AI Prompts",
        subtitle = "Prompt Enhancer, image/video/thumbnail prompts, Flow/Sora & research",
        badge = "6 TOOLS"
    ),
    VOICE_SCRIPT_STUDIO(
        id = "voice_script_studio",
        title = "Voice & Script Studio",
        shortTitle = "Voice & Scripts",
        subtitle = "Voice-overs, Shorts/Reels scripts, killer hooks & CTAs",
        badge = "4 TOOLS"
    ),
    CREATOR_TOOLKIT(
        id = "creator_toolkit",
        title = "Creator Toolkit",
        shortTitle = "Toolkit",
        subtitle = "Social captions, hashtag packs, content calendar & creative ideas",
        badge = "5 TOOLS"
    )
}

enum class GenerationLanguage(val code: String, val label: String, val promptInstruction: String) {
    ENGLISH(
        code = "en",
        label = "English",
        promptInstruction = "Write all user-facing generated content in clear, natural, high-impact English."
    ),
    ROMAN_URDU(
        code = "roman_ur",
        label = "Roman Urdu",
        promptInstruction = "Write user-facing generated titles, scripts, hooks, captions, and explanations in authentic, natural Roman Urdu (Urdu written in Latin/English alphabet) popular with Pakistani and South Asian gaming/digital creators. Keep technical prompt keywords (camera angles, lighting, aspect ratios) in English where needed for AI image/video generators."
    );

    companion object {
        fun fromCode(code: String?): GenerationLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

enum class OutputDetailLevel(val label: String, val description: String) {
    SHORT("Short & Punchy", "Fast, concise, high-retention output ready to post"),
    DETAILED("Detailed & Pro", "Comprehensive breakdown with multiple variations & pro structure")
}

enum class CreatorTone(val label: String) {
    ENERGETIC("Energetic"),
    CINEMATIC("Cinematic"),
    PROFESSIONAL("Professional"),
    CASUAL("Casual")
}

enum class AspectFormat(val ratio: String, val label: String) {
    VERTICAL_9_16("9:16", "9:16 Vertical (Shorts / Reels / TikTok)"),
    HORIZONTAL_16_9("16:9", "16:9 Horizontal (YouTube / Widescreen)"),
    SQUARE_1_1("1:1", "1:1 Square (Feed / Post)")
}

enum class VideoDurationPreset(val label: String, val secondsHint: String) {
    SHORT_15S("15s Clip", "15 seconds"),
    REEL_30S("30s Reel/Short", "30 seconds"),
    SHORT_60S("60s Short", "60 seconds"),
    YOUTUBE_5M("5-8m Video", "5 to 8 minutes"),
    DEEP_12M("10-15m Deep Dive", "10 to 15 minutes")
}

enum class CreatorPlatform(val label: String) {
    YOUTUBE("YouTube"),
    YOUTUBE_SHORTS("YT Shorts"),
    TIKTOK("TikTok"),
    INSTAGRAM_REELS("IG Reels"),
    FACEBOOK_GAMING("FB Gaming"),
    X_TWITTER("X / Threads")
}

enum class AiWorkflowPreset(
    val id: String,
    val label: String,
    val targetEngine: String,
    val styleDirective: String
) {
    STANDARD(
        id = "standard",
        label = "Universal Pro",
        targetEngine = "General Creator Workflow",
        styleDirective = "Clear subject hierarchy, vivid lighting, crisp composition, zero filler."
    ),
    GOOGLE_FLOW(
        id = "google_flow",
        label = "Google Flow / Veo",
        targetEngine = "Google Flow & Cinematic Video AI",
        styleDirective = "Continuous camera movement, temporal coherence, lens focal length, atmospheric volumetric lighting, realistic physics, ambient sound cues."
    ),
    SORA_CINEMATIC(
        id = "sora",
        label = "Sora Video",
        targetEngine = "Sora Text-to-Video",
        styleDirective = "Single cohesive shot or motivated cut, physical environment dynamics, foreground-midground-background depth, natural motion blur, 35mm cinema aesthetic."
    ),
    MIDJOURNEY_IMAGE(
        id = "image_gen",
        label = "Pro Image Gen",
        targetEngine = "High-Res AI Image Generator",
        styleDirective = "Photorealistic or stylized render specs, studio rim lighting, octanerender/unreal detail, crisp focal point, color grading palette."
    ),
    VIRAL_THUMBNAIL(
        id = "thumbnail_ctr",
        label = "High-CTR Thumbnail",
        targetEngine = "YouTube / Gaming Thumbnail",
        styleDirective = "Extreme expressive foreground subject, high-contrast complementary rim glow, uncluttered background depth, bold focal negative space for 3-word text overlay."
    )
}

enum class PromptEnhancerOutputType(val label: String, val note: String) {
    IMAGE_GENERATION(
        "Image Generation",
        "Produces a copy-ready text prompt for AI image generators (no image file is synthesized locally)."
    ),
    VIDEO_GENERATION(
        "Video Generation",
        "Produces a copy-ready cinematic motion text prompt for Flow, Sora, Veo, or Runway."
    ),
    GAMING_THUMBNAILS(
        "Gaming Thumbnails",
        "Produces a high-CTR gaming thumbnail composition & text-overlay prompt."
    ),
    SOCIAL_MEDIA_CONTENT(
        "Social Media Content",
        "Produces a complete structured social post brief, caption & hook prompt."
    ),
    CREATIVE_WRITING(
        "Creative Writing",
        "Produces a rich storytelling, lore, or narrative script prompt."
    )
}

data class CreatorToolDefinition(
    val id: String,
    val section: StudioSection,
    val title: String,
    val subtitle: String,
    val inputLabel: String,
    val inputPlaceholder: String,
    val secondaryLabel: String? = null,
    val secondaryPlaceholder: String? = null,
    val supportsWebGrounding: Boolean = false,
    val isPromptEnhancer: Boolean = false,
    val supportsDuration: Boolean = false,
    val supportsAspectFormat: Boolean = true,
    val supportsWorkflowPreset: Boolean = false,
    val quickChips: List<String> = emptyList()
)

@Serializable
data class GenerationParameters(
    val toolId: String,
    val toolTitle: String,
    val sectionId: String,
    val primaryInput: String,
    val secondaryInput: String = "",
    val languageCode: String = GenerationLanguage.ENGLISH.code,
    val detailLevel: String = OutputDetailLevel.DETAILED.name,
    val tone: String = CreatorTone.ENERGETIC.name,
    val aspectRatio: String = AspectFormat.VERTICAL_9_16.ratio,
    val durationLabel: String = VideoDurationPreset.SHORT_60S.label,
    val platform: String = CreatorPlatform.YOUTUBE.label,
    val workflowPresetId: String = AiWorkflowPreset.STANDARD.id,
    val enhancerOutputType: String = PromptEnhancerOutputType.IMAGE_GENERATION.name,
    val visualStyle: String = "",
    val specialRequirements: String = "",
    val preserveReferenceConstraints: Boolean = true,
    val enableWebResearchGrounding: Boolean = false
)

enum class GenerationSourceBadge(val label: String) {
    GEMINI_AI("Gemini AI"),
    GEMINI_GROUNDED_WEB("Gemini + Google Search Grounding"),
    SECURE_PROXY("Secure Backend Proxy"),
    LOCAL_FALLBACK("Offline Studio Template Fallback")
}

data class GenerationOutcome(
    val content: String,
    val source: GenerationSourceBadge,
    val modelUsed: String,
    val groundingSources: List<GroundingCitation> = emptyList(),
    val warningBanner: String? = null
)

data class GroundingCitation(
    val title: String,
    val url: String
)
