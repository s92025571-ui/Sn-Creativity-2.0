package com.example

import com.example.config.OwnerSupportConfig
import com.example.domain.catalog.CreatorToolCatalog
import com.example.domain.generator.LocalTemplateFallbackGenerator
import com.example.domain.model.AiWorkflowPreset
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.GenerationParameters
import com.example.domain.model.PromptEnhancerOutputType
import com.example.domain.model.StudioSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `dashboard has exactly four primary studio sections and complete tool catalog`() {
        assertEquals(4, StudioSection.entries.size)
        StudioSection.entries.forEach { section ->
            val tools = CreatorToolCatalog.toolsForSection(section)
            assertTrue("Expected tools in ${section.title}", tools.isNotEmpty())
        }
        assertTrue(CreatorToolCatalog.findToolById("prompt_enhancer")?.isPromptEnhancer == true)
    }

    @Test
    fun `prompt enhancer fallback preserves reference constraints without faking image synthesis`() {
        val params = GenerationParameters(
            toolId = "prompt_enhancer",
            toolTitle = "Advanced Prompt Enhancer",
            sectionId = StudioSection.AI_PROMPT_STUDIO.id,
            primaryInput = "Cyberpunk gamer portrait in neon purple hoodie",
            secondaryInput = "Keep exact facial structure from uploaded reference photo",
            languageCode = GenerationLanguage.ENGLISH.code,
            workflowPresetId = AiWorkflowPreset.GOOGLE_FLOW.id,
            enhancerOutputType = PromptEnhancerOutputType.VIDEO_GENERATION.name,
            preserveReferenceConstraints = true
        )

        val output = LocalTemplateFallbackGenerator.generate(params)
        assertTrue(output.contains("Keep exact facial structure from uploaded reference photo"))
        assertTrue(output.contains("no image or video file was rendered"))
    }

    @Test
    fun `roman urdu generation produces roman urdu output`() {
        val params = GenerationParameters(
            toolId = "gaming_titles",
            toolTitle = "Gaming Title Generator",
            sectionId = StudioSection.GAMING_STUDIO.id,
            primaryInput = "PUBG Mobile 1v4 Clutch",
            languageCode = GenerationLanguage.ROMAN_URDU.code
        )
        val output = LocalTemplateFallbackGenerator.generate(params)
        assertTrue(output.contains("Roman Urdu"))
        assertTrue(output.contains("PUBG Mobile 1v4 Clutch"))
    }

    @Test
    fun `owner whatsapp number is not fabricated by default`() {
        assertFalse(OwnerSupportConfig.isWhatsAppConfigured())
    }
}
