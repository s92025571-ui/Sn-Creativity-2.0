package com.example.domain.generator

import com.example.domain.model.AiWorkflowPreset
import com.example.domain.model.CreatorTone
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.GenerationParameters
import com.example.domain.model.OutputDetailLevel
import com.example.domain.model.PromptEnhancerOutputType

/**
 * Deterministic, studio-grade offline template generator for SN Creativity AI 2.0.
 * Used ONLY when the user explicitly requests Local Template Fallback or enables offline templates,
 * never masquerading as a live Gemini AI response.
 */
object LocalTemplateFallbackGenerator {

    fun generate(params: GenerationParameters, variationSeed: Int = 0): String {
        val topic = params.primaryInput.trim().ifEmpty { "Futuristic Gaming & Creator Content" }
        val extra = params.secondaryInput.trim()
        val lang = GenerationLanguage.fromCode(params.languageCode)
        val isRomanUrdu = lang == GenerationLanguage.ROMAN_URDU
        val isDetailed = params.detailLevel == OutputDetailLevel.DETAILED.name
        val tone = runCatching { CreatorTone.valueOf(params.tone).label }.getOrDefault("Energetic")
        val preset = AiWorkflowPreset.entries.firstOrNull { it.id == params.workflowPresetId }
            ?: AiWorkflowPreset.STANDARD

        return when (params.toolId) {
            "prompt_enhancer" -> buildPromptEnhancerFallback(params, topic, extra, preset, variationSeed)
            "image_gen_prompts" -> buildImagePromptFallback(topic, extra, params, preset, variationSeed)
            "video_gen_prompts", "workflow_presets_builder" ->
                buildVideoPromptFallback(topic, extra, params, preset, variationSeed)
            "thumbnail_prompts" -> buildThumbnailPromptFallback(topic, extra, params, variationSeed)
            "topic_research_assistant" -> buildResearchFallback(topic, extra, isRomanUrdu)
            "gaming_titles" -> buildGamingTitlesFallback(topic, extra, isRomanUrdu, isDetailed, variationSeed)
            "gaming_hashtags", "hashtag_suggestions" ->
                buildHashtagsFallback(topic, extra, params.platform)
            "gaming_video_ideas" -> buildGamingIdeasFallback(topic, extra, isRomanUrdu, params)
            "gaming_descriptions", "video_descriptions" ->
                buildDescriptionFallback(topic, extra, isRomanUrdu, params)
            "gaming_tips" -> buildGamingTipsFallback(topic, extra, isRomanUrdu, tone)
            "voiceover_scripts" -> buildVoiceOverFallback(topic, extra, isRomanUrdu, params)
            "short_form_scripts" -> buildShortScriptFallback(topic, extra, isRomanUrdu, params)
            "hooks_intros" -> buildHooksFallback(topic, extra, isRomanUrdu, variationSeed)
            "calls_to_action" -> buildCtaFallback(topic, extra, isRomanUrdu, params.platform)
            "social_captions" -> buildCaptionsFallback(topic, extra, isRomanUrdu, params.platform)
            "content_planning" -> buildContentPlanFallback(topic, extra, isRomanUrdu)
            else -> buildCreativeIdeasFallback(topic, extra, isRomanUrdu, tone)
        }
    }

    private fun buildPromptEnhancerFallback(
        params: GenerationParameters,
        topic: String,
        constraints: String,
        preset: AiWorkflowPreset,
        seed: Int
    ): String {
        val outputType = runCatching {
            PromptEnhancerOutputType.valueOf(params.enhancerOutputType)
        }.getOrDefault(PromptEnhancerOutputType.IMAGE_GENERATION)

        val lightingStyles = listOf(
            "volumetric neon-violet rim lighting with deep obsidian contrast and subtle holographic reflections",
            "dramatic dual-tone cyber-purple key light, crisp specular highlights, atmospheric haze",
            "cinematic anamorphic studio lighting, soft bounced fill, electric magenta edge glow"
        )
        val chosenLight = lightingStyles[seed % lightingStyles.size]
        val visualStyle = params.visualStyle.ifBlank { "Ultra-detailed futuristic creator aesthetic, 8K clarity" }
        val special = params.specialRequirements.trim()

        return buildString {
            appendLine("### Copy-Ready Enhanced Prompt (${outputType.label})")
            appendLine()
            appendLine("> **Note:** Text prompt generated locally via SN Studio Template Engine (no image or video file was rendered).")
            appendLine()
            append("**Master Prompt:** ")
            append("$topic. ")
            append("Styled in $visualStyle with a ${params.tone.lowercase()} mood. ")
            append("Engineered for ${preset.targetEngine}: ${preset.styleDirective} ")
            append("Lighting & Optics: $chosenLight. ")
            append("Framing & Aspect Ratio: ${params.aspectRatio}")
            if (outputType == PromptEnhancerOutputType.VIDEO_GENERATION) {
                append(", target pacing ${params.durationLabel}, smooth gimbal-stabilized camera motion, coherent subject identity across frames")
            }
            append(".")
            if (special.isNotEmpty()) {
                append(" Special requirements: $special.")
            }
            if (constraints.isNotEmpty() && params.preserveReferenceConstraints) {
                appendLine()
                appendLine()
                appendLine("**Preserved Reference & Identity Constraints (Do Not Alter):**")
                appendLine("- $constraints")
                appendLine("- Keep original user-provided subject/reference attributes intact without inventing unrequested reference images.")
            }
            appendLine()
            appendLine()
            appendLine("**Recommended Parameters:**")
            appendLine("- Aspect Ratio: `${params.aspectRatio}`")
            appendLine("- Preset Workflow: `${preset.label}`")
            appendLine("- Negative / Avoid Cues: blurry details, distorted hands/faces, muddy shadows, unintended watermarks, cluttered text artifacts")
        }.trim()
    }

    private fun buildImagePromptFallback(
        topic: String,
        extra: String,
        params: GenerationParameters,
        preset: AiWorkflowPreset,
        seed: Int
    ): String {
        val lenses = listOf("35mm f/1.4 prime lens", "85mm portrait telephoto lens", "24mm wide cinematic lens")
        val lens = lenses[seed % lenses.size]
        return """
            ### AI Image Generation Prompt Pack (${preset.label})

            **Option 1 — Hero Studio Render (`${params.aspectRatio}`)**
            `$topic${if (extra.isNotEmpty()) ", $extra" else ""}, captured on $lens, hyper-detailed textures, dramatic neon-purple and cool cyan rim lighting, deep charcoal depth of field, ${preset.styleDirective} --ar ${params.aspectRatio}`

            **Option 2 — Stylized High-Contrast Concept (`${params.aspectRatio}`)**
            `Bold stylized 3D digital illustration of $topic, crisp geometric forms, glowing violet energy accents, clean focal silhouette, studio reflections, ultra-sharp 8K composition --ar ${params.aspectRatio}`

            **Lighting & Composition Tip:** Place your primary subject on the upper-third intersection and keep background clutter minimal for maximum mobile feed impact.
        """.trimIndent()
    }

    private fun buildVideoPromptFallback(
        topic: String,
        extra: String,
        params: GenerationParameters,
        preset: AiWorkflowPreset,
        seed: Int
    ): String {
        val cameraMoves = listOf(
            "Slow push-in dolly shot transitioning into a subtle 15-degree orbit",
            "Low-angle tracking shot moving smoothly alongside the main subject",
            "Crisp crane reveal descending into a locked hero medium close-up"
        )
        val move = cameraMoves[seed % cameraMoves.size]
        return """
            ### AI Video Prompt (${preset.label} • ${params.durationLabel} • ${params.aspectRatio})

            **Copy-Ready Video Prompt:**
            `$move of $topic${if (extra.isNotEmpty()) " ($extra)" else ""}. Realistic physical motion, consistent character and object geometry across the entire ${params.durationLabel} clip, volumetric purple and tungsten studio lighting, natural motion blur, atmospheric particles drifting in foreground depth. Format ${params.aspectRatio}.`

            **Multi-Beat Pacing Guide:**
            1. **0s–2s (Visual Hook):** Immediate motion contrast on focal subject.
            2. **Middle Beat:** Smooth continuous camera movement showcasing environment depth.
            3. **Final Beat:** Clean settle frame ideal for looping or transition cut.
        """.trimIndent()
    }

    private fun buildThumbnailPromptFallback(
        topic: String,
        extra: String,
        params: GenerationParameters,
        seed: Int
    ): String {
        val badgeWords = listOf("IMPOSSIBLE?!", "NEW SECRET!", "1v4 CLUTCH!", "MAX GRAPHICS!")
        val overlay = badgeWords[seed % badgeWords.size]
        return """
            ### High-CTR Gaming & Creator Thumbnail Prompt (`${params.aspectRatio}`)

            **Visual Generation Prompt:**
            `High-impact YouTube/Shorts thumbnail composition featuring $topic${if (extra.isNotEmpty()) ", $extra" else ""}. Expressive close-up foreground subject on the right rule-of-thirds, intense neon-purple and golden rim glow separating subject from a slightly blurred high-action background, dramatic contrast, vibrant color grading, clean negative space on the left for bold 3-word text overlay, ${params.aspectRatio} aspect ratio.`

            **Suggested 3-Word Thumbnail Text Overlay:**
            - Primary Text: **"$overlay"**
            - Color Combo: Pure White + Electric Yellow accent with dark drop-shadow.
        """.trimIndent()
    }

    private fun buildResearchFallback(topic: String, extra: String, isRomanUrdu: Boolean): String {
        return if (isRomanUrdu) {
            """
                ### Offline Topic Structure Brief (Live Web Grounding Unavailable)
                > **Important Notice:** Live internet search is currently unavailable because Gemini API / Search Grounding is not connected. Niche diya gaya outline offline creator framework par mabni hai — current numbers ya news post karne se pehle verify zaroor karein.

                **Topic:** $topic ${if (extra.isNotEmpty()) "($extra)" else ""}
                1. **Core Hook Angle:** Audience ko pehle 3 seconds mein batayein ke yeh topic unke gameplay ya workflow ko kaise behtar banata hai.
                2. **3 Key Talking Points:**
                   - Problem jo zyada tar creators ya players face karte hain.
                   - Practical step-by-step comparison ya live proof.
                   - Actionable takeaway jo viewer foran try kar sake.
                3. **Verification Checklist:** Official patch notes, creator studio analytics, ya live Gemini Search Grounding enable karke ताज़ा facts confirm karein.
            """.trimIndent()
        } else {
            """
                ### Offline Research Structure Brief (Live Web Research Unavailable)
                > **Important Notice:** Live internet research is unavailable in Local Fallback mode. Do not treat this outline as live news; connect Gemini with Google Search Grounding in Settings for verified real-time web citations.

                **Research Topic:** $topic ${if (extra.isNotEmpty()) "— $extra" else ""}
                1. **Core Viewer Question:** Define why audiences care about *$topic* right now and what problem it solves.
                2. **Recommended Investigation Pillars:**
                   - **Baseline Mechanics / Context:** Explain the core concept in plain language under 15 seconds.
                   - **Practical Comparison:** Contrast common mistakes vs. tested pro workflows.
                   - **Actionable Takeaway:** End with 1 concrete step viewers can test immediately.
                3. **Fact-Check Reminder:** Verify latest version numbers, patch notes, or pricing before publishing.
            """.trimIndent()
        }
    }

    private fun buildGamingTitlesFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        isDetailed: Boolean,
        seed: Int
    ): String {
        val tag = if (extra.isNotEmpty()) " ($extra)" else ""
        return if (isRomanUrdu) {
            """
                ### Gaming Title Pack — Roman Urdu (${if (isDetailed) "Detailed" else "Quick"})
                1. **Bhai Yeh Kaise Hua?!** $topic Mein Sab Se Crazy Clutch!$tag
                2. **Sirf 1% Players Jante Hain!** $topic Secret Trick Revealed
                3. **Solo vs Full Lobby!** $topic Intense Final Zone Gameplay
                4. **Noob Se Pro Level!** $topic Complete Pro Strategy (${2026 + (seed % 2)})
                5. **Lobby Shock Ho Gayi!** $topic High-IQ Moments
            """.trimIndent()
        } else {
            """
                ### High-CTR Gaming Titles (${if (isDetailed) "Pro Pack" else "Quick Pack"})
                1. I Tried **$topic** in a Sweaty Lobby… Here's What Happened!$tag
                2. The Secret **$topic** Strategy Only 1% of Players Use
                3. Zero Mistakes Allowed: Insane **$topic** Final Circle Clutch!
                4. Why Everyone Is Switching to This **$topic** Setup Right Now
                5. 100 IQ Plays in **$topic** That Broke the Whole Lobby
            """.trimIndent()
        }
    }

    private fun buildHashtagsFallback(topic: String, extra: String, platform: String): String {
        val cleanSlug = topic.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .split(" ")
            .filter { it.length > 2 }
            .take(3)
            .joinToString("")
            .ifEmpty { "gamingcreator" }

        return """
            ### 3-Tier Hashtag Pack for $platform
            **Topic:** $topic ${if (extra.isNotEmpty()) "• $extra" else ""}

            **Tier 1 — High-Discovery Broad Tags:**
            `#gaming` `#gamer` `#creator` `#viralclips` `#shorts` `#trendinggaming`

            **Tier 2 — Niche & Topic Specific Tags:**
            `#$cleanSlug` `#${cleanSlug}clips` `#${cleanSlug}tips` `#progameplay` `#gamingcommunity`

            **Tier 3 — Brand & Format Tags:**
            `#sncreativity` `#contentcreator` `#montage` `#clutchmoments` `#gaminghighlights`

            **Copy-Ready Line:**
            `#$cleanSlug #${cleanSlug}clips #gaming #gamingcommunity #progameplay #viralclips #sncreativity`
        """.trimIndent()
    }

    private fun buildGamingIdeasFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        params: GenerationParameters
    ): String {
        return if (isRomanUrdu) {
            """
                ### Gaming Video Ideas (${params.platform} • ${params.durationLabel})
                1. **Challenge Concept:** $topic sirf starter loadout ke saath jeetna ${if (extra.isNotEmpty()) "($extra)" else ""}.
                   - *Thumbnail:* Health 1 HP bar + shocked reaction.
                2. **Mythbuster Concept:** $topic ke 5 viral myths test karein ke kaunsa sach hai.
                   - *Hook:* "90% players yeh ghalti roz karte hain!"
                3. **Pro Breakdown:** Apne best match ko pause karke decision-making samjhayein.
            """.trimIndent()
        } else {
            """
                ### Viral Gaming Video Concepts (${params.platform} • ${params.durationLabel})
                1. **The Handicap Challenge:** Win a match in *$topic* with one strict limitation ${if (extra.isNotEmpty()) "($extra)" else "(no upgrades / default gear)"}.
                   - *Hook:* Show the final tense 3 seconds first, then rewind to how it started.
                2. **5 Myths Tested:** Put popular community rumors in *$topic* to an actual side-by-side test.
                3. **Inside the Mind of a Clutch:** Freeze-frame breakdown of positioning, audio cues, and aim timing.
            """.trimIndent()
        }
    }

    private fun buildDescriptionFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        params: GenerationParameters
    ): String {
        return if (isRomanUrdu) {
            """
                ### SEO Video Description (Roman Urdu + Keywords)
                Aaj ki video mein hum cover kar rahe hain **$topic**! ${if (extra.isNotEmpty()) "Khaas highlight: $extra." else ""}
                Agar aapko high-energy gameplay aur pro tips pasand aayein to video ko Like karein aur channel Subscribe karke bell icon on kar lein!

                **Chapters / Highlights:**
                00:00 — Crazy Intro Hook
                00:45 — Main Action & Strategy ($topic)
                02:30 — Pro Settings & Breakdown
                04:00 — Final Clutch & Outro

                Apna favorite part comments mein zaroor batayein!
            """.trimIndent()
        } else {
            """
                ### Search-Optimized Video Description (${params.platform})
                In today's upload, we dive straight into **$topic**${if (extra.isNotEmpty()) " featuring $extra" else ""}! Built for maximum action and practical takeaways you can use right away.

                **Video Chapters:**
                00:00 — Highlight Hook
                00:40 — Core Setup & Context
                02:15 — Key Breakdown: $topic
                04:10 — Final Verdict & Community Question

                Drop a comment below with what you want tested next, and subscribe for more uploads!
            """.trimIndent()
        }
    }

    private fun buildGamingTipsFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        tone: String
    ): String {
        return if (isRomanUrdu) {
            """
                ### Pro Gaming Tips Guide ($tone Tone) — $topic
                1. **Crosshair Placement Pehle Rakhein:** Enemy dikhne se pehle head-level par pre-aim rakhein ${if (extra.isNotEmpty()) "($extra)" else ""}.
                2. **Cover-to-Cover Rotation:** Open field mein sprint karne ke bajaye smoke aur hard cover angles use karein.
                3. **5-Minute Warmup Drill:** Ranked match se pehle training ground mein burst transfer practice karein.
            """.trimIndent()
        } else {
            """
                ### Pro Tactical Tips ($tone Tone) — $topic
                1. **Pre-Aim Common Angles:** Keep crosshair placement at head height before peeking corners${if (extra.isNotEmpty()) " ($extra)" else ""}.
                2. **Isolate 1v1 Fights:** Use hard cover and right-hand/angle advantage so multiple opponents cannot trade damage simultaneously.
                3. **Consistent Sensitivity & Warmup:** Lock one sensitivity profile for 14 days instead of changing after every loss.
            """.trimIndent()
        }
    }

    private fun buildVoiceOverFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        params: GenerationParameters
    ): String {
        return if (isRomanUrdu) {
            """
                ### Voice-Over Script (${params.durationLabel} • Roman Urdu)
                **[0:00 - Hook | High Energy]**
                "Ruko! Agar aap bhi **$topic** ko halkay mein le rahe ho, to aglay kuch seconds aapka nazariya badal denge!"

                **[Middle Beat | Clear Pacing + B-Roll]**
                *(Visual: Clean zoom-in gameplay / demo clip)*
                "Zyada tar log sirf basic tareeqa try karte hain, lekin asli farq tab aata hai jab aap timing aur smart setup combine karte ho. ${if (extra.isNotEmpty()) "Khaas taur par: $extra." else ""}"

                **[Ending | Strong Punchline]**
                "Abhi isay try karo aur comments mein batao aapka result kaisa raha!"
            """.trimIndent()
        } else {
            """
                ### Voice-Over Narration Script (${params.durationLabel} • ${params.platform})
                **[0:00–0:04 | Hook — Crisp & Immediate]**
                "Most creators get **$topic** completely backwards—and fixing this one detail changes everything."

                **[0:04–Middle | Core Story & Proof]**
                *(B-Roll: Fast side-by-side visual demonstration)*
                "Instead of overcomplicating the setup, focus on clarity, pacing, and the exact moment viewer curiosity peaks. ${if (extra.isNotEmpty()) "Notice how $extra elevates the entire sequence." else ""}"

                **[Outro | Clean Callout]**
                "Try this on your next upload and tell me in the comments how it performed."
            """.trimIndent()
        }
    }

    private fun buildShortScriptFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        params: GenerationParameters
    ): String {
        return if (isRomanUrdu) {
            """
                ### Short-Form 9:16 Script (${params.durationLabel} • Roman Urdu)
                - **0–3s (Visual + Audio Hook):** "99% log **$topic** mein yeh secret miss kar dete hain!" *(Fast zoom + sound whoosh)*
                - **3–18s (Fast Value):** "Step 1: Setup clean rakho. Step 2: ${extra.ifEmpty { "Timing aur angle par focus rakho" }}. Dekho farq kitna saaf nazar aa raha hai!"
                - **18–End (Loop CTA):** "Save kar lo taake bhool na jao—aur batao agla konsa topic cover karein?"
            """.trimIndent()
        } else {
            """
                ### High-Retention Short-Form Script (${params.durationLabel} • ${params.aspectRatio})
                - **0–3s (Pattern Interrupt Hook):** "Stop scrolling if you care about **$topic**—watch what happens in 5 seconds."
                - **3–20s (Rapid Visual Proof):** "First, strip away the filler. Second, lock in ${extra.ifEmpty { "the core visual payoff right away" }}. Look at the immediate difference."
                - **Final 4s (Seamless Loop CTA):** "Save this for your next session and comment 'PART 2' if you want the advanced setup—because…" *(loops cleanly into start)*
            """.trimIndent()
        }
    }

    private fun buildHooksFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        seed: Int
    ): String {
        return if (isRomanUrdu) {
            """
                ### 5 Scroll-Stopping Hooks (Roman Urdu)
                1. "Aglay 10 seconds mein main aapko **$topic** ka sab se bara raaz dikhane wala hoon!"
                2. "Maine poore 7 din **$topic** test kiya, aur result dekh kar main khud hairan reh gaya…"
                3. "Yeh ek choti si ghalti aapke **$topic** ko bilkul kharab kar rahi hai!"
                4. "Agar aap ${extra.ifEmpty { "pro level results" }} chahte ho, to is clip ka end miss mat karna."
                5. "Sab keh rahe thay yeh namumkin hai—chalo live test karte hain!"
            """.trimIndent()
        } else {
            """
                ### 5 High-Retention First-3-Second Hooks (#${seed + 1})
                1. **Curiosity Gap:** "Nobody talks about this hidden detail in **$topic**, yet it changes everything."
                2. **Visual Proof First:** "Watch the screen closely—here is **$topic** pushed to the absolute limit."
                3. **Mistake Callout:** "If your **$topic** feels stuck, you're probably making this exact mistake."
                4. **Time-Boxed Challenge:** "Give me 20 seconds and I'll upgrade how you approach **$topic**${if (extra.isNotEmpty()) " ($extra)" else ""}."
                5. **Contrarian Opener:** "Forget the standard advice about **$topic**—do this instead."
            """.trimIndent()
        }
    }

    private fun buildCtaFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        platform: String
    ): String {
        return if (isRomanUrdu) {
            """
                ### Natural High-Converting CTAs ($platform • Roman Urdu)
                1. **Comment Trigger:** "Aap **$topic** ke baare mein kya sochte ho? Niche comment mein 1 se 10 tak rate karo!"
                2. **Save & Share:** "Is reel/short ko abhi Save kar lo aur apne squad mate ko bhejo!"
                3. **Subscribe Value CTA:** "Roz aisi hi pro gaming aur AI tips ke liye Subscribe button daba do! ${extra}"
            """.trimIndent()
        } else {
            """
                ### High-Converting Calls to Action ($platform)
                1. **Debate Comment CTA:** "Which side are you on for **$topic**? Drop your honest take in the comments!"
                2. **Save-Driven CTA:** "Bookmark this right now so it's ready before your next upload or match."
                3. **Value Subscribe CTA:** "Follow/Subscribe if you want part two${if (extra.isNotEmpty()) " covering $extra" else ""} without any fluff!"
            """.trimIndent()
        }
    }

    private fun buildCaptionsFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        platform: String
    ): String {
        return if (isRomanUrdu) {
            """
                ### Social Media Captions ($platform • Roman Urdu)
                **1. Short & Punchy:**
                Grind never stops! **$topic** mode ON. ${extra.ifEmpty { "Rate this 1–10 👇" }}

                **2. Story Vibe:**
                Har frame ke peeche mehnat aur creativity hoti hai. Aaj ka focus: **$topic**. Aapko sab se best part konsa laga?

                **3. Engagement Question:**
                Squad ready hai ya solo hi kaafi hai? Comments mein apni رائے (opinion) dein!
            """.trimIndent()
        } else {
            """
                ### Social Media Captions ($platform)
                **1. Punchy One-Liner:**
                Built different when **$topic** hits peak focus. ${extra.ifEmpty { "Rate this 1–10 below!" }}

                **2. Behind-the-Scenes Creator Vibe:**
                Dialing in every detail on **$topic** tonight. Small 1% upgrades compound fast—what are you building this week?

                **3. Community Question:**
                Be honest: would you try this **$topic** setup or stick to the classic? Let's settle it in the comments.
            """.trimIndent()
        }
    }

    private fun buildContentPlanFallback(topic: String, extra: String, isRomanUrdu: Boolean): String {
        return if (isRomanUrdu) {
            """
                ### 7-Day Creator Content Plan — $topic
                - **Day 1 (Viral Short):** Fast 30s tip ya clutch highlight ($topic).
                - **Day 2 (Tutorial/Guide):** Step-by-step problem solver ${if (extra.isNotEmpty()) "($extra)" else ""}.
                - **Day 3 (Community Poll + Reel):** Myth vs Reality comparison clip.
                - **Day 4 (Hero Upload):** Deep-dive main video + strong custom thumbnail.
                - **Day 5 (Behind the Scenes):** Setup, workflow ya prompt breakdown.
                - **Day 6 (Challenge Clip):** High-energy stakes video.
                - **Day 7 (Recap & Q&A):** Top viewer comment reply video.
            """.trimIndent()
        } else {
            """
                ### 7-Day High-Growth Content Calendar — $topic
                - **Day 1 (Discovery Short 9:16):** 30-second high-curiosity hook on *$topic*.
                - **Day 2 (Educational Value):** Practical 3-step breakdown ${if (extra.isNotEmpty()) "($extra)" else "for beginners & intermediates"}.
                - **Day 3 (Comparison / Mythbuster):** Test a popular assumption live on screen.
                - **Day 4 (Flagship 16:9 Video):** Deep-dive story or showcase with high-CTR thumbnail.
                - **Day 5 (Workflow Reveal):** Share your behind-the-scenes process or settings.
                - **Day 6 (Entertainment / Challenge):** High-stakes challenge episode.
                - **Day 7 (Community Reply Short):** Answer the #1 comment from Day 4 to boost loyalty.
            """.trimIndent()
        }
    }

    private fun buildCreativeIdeasFallback(
        topic: String,
        extra: String,
        isRomanUrdu: Boolean,
        tone: String
    ): String {
        return if (isRomanUrdu) {
            """
                ### Creative Idea Lab ($tone • Roman Urdu)
                **Concept:** $topic ${if (extra.isNotEmpty()) "— $extra" else ""}
                - **Series Title Idea:** *Neon Protocol: $topic Chronicles*
                - **Core Premise:** Ek futuristic creator universe jahan har episode ek naya high-stakes challenge unlock karta hai.
                - **Signature Tagline:** "Soch Se Screen Tak — Next-Level Creativity."
            """.trimIndent()
        } else {
            """
                ### Creative Idea Lab ($tone Tone)
                **Concept:** $topic ${if (extra.isNotEmpty()) "— $extra" else ""}
                - **Signature Series Name:** *Project Violet: $topic Unleashed*
                - **Worldbuilding / Hook:** Frame every episode as an escalating experiment with clear stakes, sleek visual chapters, and recurring audience choices.
                - **Channel Tagline Idea:** *"Where Precision Prompts Meet Next-Gen Play."*
            """.trimIndent()
        }
    }
}
