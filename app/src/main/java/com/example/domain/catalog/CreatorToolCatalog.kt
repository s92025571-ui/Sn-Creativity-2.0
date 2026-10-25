package com.example.domain.catalog

import com.example.domain.model.CreatorToolDefinition
import com.example.domain.model.StudioSection

object CreatorToolCatalog {

    val allTools: List<CreatorToolDefinition> = listOf(
        // ====================================================================
        // A. GAMING STUDIO (5 tools)
        // ====================================================================
        CreatorToolDefinition(
            id = "gaming_titles",
            section = StudioSection.GAMING_STUDIO,
            title = "Gaming Title Generator",
            subtitle = "High-CTR titles for gameplay, montages, live streams & challenges",
            inputLabel = "Game & Video Topic",
            inputPlaceholder = "e.g., PUBG Mobile 1v4 clutch in Erangel final circle with M416",
            secondaryLabel = "Key Highlight / Challenge Rule (Optional)",
            secondaryPlaceholder = "e.g., Zero damage taken, Conqueror lobby, solo vs squad",
            supportsDuration = true,
            quickChips = listOf(
                "PUBG Mobile Solo vs Squad Clutch",
                "Free Fire Ranked Grandmaster Push",
                "GTA VI Trailer Breakdown & Secrets",
                "Valorant Radiant Aim Routine",
                "Minecraft Hardcore 100 Days Survival"
            )
        ),
        CreatorToolDefinition(
            id = "gaming_hashtags",
            section = StudioSection.GAMING_STUDIO,
            title = "Gaming Hashtags",
            subtitle = "Ranked broad, niche & viral discovery tags for gaming uploads",
            inputLabel = "Game Title & Content Niche",
            inputPlaceholder = "e.g., Call of Duty Mobile sniper montage",
            secondaryLabel = "Target Region or Audience (Optional)",
            secondaryPlaceholder = "e.g., Pakistan, India, Global Esports",
            supportsDuration = false,
            quickChips = listOf(
                "PUBG Mobile Highlights",
                "Free Fire Shorts",
                "Mobile Gaming Montage",
                "PC Esports Clips",
                "Live Stream Gaming"
            )
        ),
        CreatorToolDefinition(
            id = "gaming_video_ideas",
            section = StudioSection.GAMING_STUDIO,
            title = "Gaming Video Ideas",
            subtitle = "Fresh viral concepts, thumbnail concepts & retention structures",
            inputLabel = "Favorite Game or Genre",
            inputPlaceholder = "e.g., Battle Royale, Roblox, FIFA/EA FC, Story Horror Games",
            secondaryLabel = "Channel Style / Skill Focus (Optional)",
            secondaryPlaceholder = "e.g., Funny commentary, pro competitive, no-commentary mobile",
            supportsDuration = true,
            quickChips = listOf(
                "Battle Royale Challenge Ideas",
                "Low-End Phone Lag-Free Gameplay",
                "Noob to Pro Progression Series",
                "Funny Squad Moments",
                "New Update Mythbusters"
            )
        ),
        CreatorToolDefinition(
            id = "gaming_descriptions",
            section = StudioSection.GAMING_STUDIO,
            title = "Gaming Descriptions & Captions",
            subtitle = "SEO-friendly YouTube descriptions, timestamps & punchy Reels captions",
            inputLabel = "What Happens in the Gameplay Video?",
            inputPlaceholder = "e.g., Intense 28-kill squad match + sensitivity settings reveal",
            secondaryLabel = "Device / Gear / Social Callout (Optional)",
            secondaryPlaceholder = "e.g., 4-finger claw gyro, subscribe for daily custom rooms",
            supportsDuration = true,
            quickChips = listOf(
                "Sensitivity & HUD Code Reveal",
                "Custom Room Tournament Highlights",
                "New Season Royal Pass Review",
                "Clutch Montage Caption"
            )
        ),
        CreatorToolDefinition(
            id = "gaming_tips",
            section = StudioSection.GAMING_STUDIO,
            title = "Game-Specific Tips & Content",
            subtitle = "Tactical pro guides, loadout breakdowns & educational creator scripts",
            inputLabel = "Game & Skill / Mechanic to Explain",
            inputPlaceholder = "e.g., Recoil control & close-range jiggle movement in PUBG Mobile",
            secondaryLabel = "Target Player Skill Level (Optional)",
            secondaryPlaceholder = "e.g., Beginners stuck in Crown tier",
            supportsDuration = true,
            quickChips = listOf(
                "Close Range Hip-Fire Mastery",
                "Zero Recoil Gyroscope Guide",
                "Best Drop Spots for Rank Push",
                "FPS Boost & Ping Optimization Tips"
            )
        ),

        // ====================================================================
        // B. AI PROMPT STUDIO (6 tools including Advanced Prompt Enhancer)
        // ====================================================================
        CreatorToolDefinition(
            id = "prompt_enhancer",
            section = StudioSection.AI_PROMPT_STUDIO,
            title = "Advanced Prompt Enhancer",
            subtitle = "Transform a simple idea into a master-level, copy-ready prompt",
            inputLabel = "Your Basic Idea or Instruction",
            inputPlaceholder = "e.g., A futuristic cyber gamer wearing a neon purple hoodie in a dark studio, keep my uploaded character's face unchanged",
            secondaryLabel = "Reference Image / Strict Constraints (Preserved Verbatim)",
            secondaryPlaceholder = "e.g., Preserve exact subject pose and facial identity from reference photo; do not alter logo text",
            isPromptEnhancer = true,
            supportsDuration = true,
            supportsAspectFormat = true,
            supportsWorkflowPreset = true,
            quickChips = listOf(
                "Neon purple esports mascot avatar",
                "Cinematic drone shot over cyber Karachi",
                "Shocked gamer thumbnail holding glowing trophy",
                "Product reveal commercial in liquid violet glass"
            )
        ),
        CreatorToolDefinition(
            id = "image_gen_prompts",
            section = StudioSection.AI_PROMPT_STUDIO,
            title = "Image-Generation Prompts",
            subtitle = "Detailed lighting, lens, composition & style prompts for AI art",
            inputLabel = "Subject & Scene to Visualize",
            inputPlaceholder = "e.g., Futuristic samurai standing on a rain-slicked neon rooftop",
            secondaryLabel = "Color Palette / Camera Style (Optional)",
            secondaryPlaceholder = "e.g., Neon violet & obsidian, 85mm f/1.4 portrait, Unreal Engine 5",
            supportsWorkflowPreset = true,
            quickChips = listOf(
                "3D Gaming Channel Mascot Logo",
                "Photorealistic Sci-Fi Character Portrait",
                "Isometric Neon Streaming Room",
                "Luxury Minimalist Brand Wallpaper"
            )
        ),
        CreatorToolDefinition(
            id = "video_gen_prompts",
            section = StudioSection.AI_PROMPT_STUDIO,
            title = "Video-Generation Prompts",
            subtitle = "Camera motion, physics & scene continuity for Google Flow, Veo & Sora",
            inputLabel = "Scene Action & Subject Movement",
            inputPlaceholder = "e.g., Sports car drifting through a glowing purple canyon at dusk",
            secondaryLabel = "Camera Trajectory & Audio Mood (Optional)",
            secondaryPlaceholder = "e.g., Low FPV tracking shot into slow-motion wheel close-up",
            supportsDuration = true,
            supportsWorkflowPreset = true,
            quickChips = listOf(
                "Google Flow Cinematic Intro Shot",
                "Sora FPV Drone City Flythrough",
                "Slow-Motion Gaming Intro Transition",
                "Product Unbox Hologram Reveal"
            )
        ),
        CreatorToolDefinition(
            id = "thumbnail_prompts",
            section = StudioSection.AI_PROMPT_STUDIO,
            title = "Thumbnail Prompts",
            subtitle = "Scroll-stopping visual hierarchy, rim glow & 3-word text layout ideas",
            inputLabel = "Video Title or Core Hook",
            inputPlaceholder = "e.g., I Survived 100 Players in 1 Tiny Zone!",
            secondaryLabel = "Foreground Character / Object & Emotion",
            secondaryPlaceholder = "e.g., Intense shocked expression, glowing mythic crate behind",
            supportsWorkflowPreset = true,
            quickChips = listOf(
                "1v4 Impossible Clutch Thumbnail",
                "Before vs After Upgrade Split Screen",
                "Secret Mythic Item Unlocked",
                "Tech Review Vs Battle Thumbnail"
            )
        ),
        CreatorToolDefinition(
            id = "workflow_presets_builder",
            section = StudioSection.AI_PROMPT_STUDIO,
            title = "Flow, Sora & Creator Presets",
            subtitle = "Multi-shot storyboard & engine-tuned prompt packs for top AI tools",
            inputLabel = "Project Concept or Storyline",
            inputPlaceholder = "e.g., 3-shot gaming channel trailer showing futuristic purple arena",
            secondaryLabel = "Brand Name / Text Callout (Optional)",
            secondaryPlaceholder = "e.g., SN Gaming Official",
            supportsDuration = true,
            supportsWorkflowPreset = true,
            quickChips = listOf(
                "Google Flow 3-Scene Storyboard",
                "Sora Seamless Loop Background",
                "Channel Intro + Outro Visual Pack",
                "Consistent Character Sheet Prompts"
            )
        ),
        CreatorToolDefinition(
            id = "topic_research_assistant",
            section = StudioSection.AI_PROMPT_STUDIO,
            title = "Search & Research Assistant",
            subtitle = "Fact-checked topic briefs with optional live Google Search grounding",
            inputLabel = "Topic, Trend, or Question to Research",
            inputPlaceholder = "e.g., Latest mobile esports tournament formats and creator monetization tips",
            secondaryLabel = "Specific Angle or Audience Question (Optional)",
            secondaryPlaceholder = "e.g., Summarize key talking points for a 60-second video",
            supportsWebGrounding = true,
            supportsAspectFormat = false,
            quickChips = listOf(
                "Top gaming content formats right now",
                "How AI video tools like Flow & Sora work",
                "Short-form retention science for creators",
                "Smartphone recording & audio clarity checklist"
            )
        ),

        // ====================================================================
        // C. VOICE & SCRIPT STUDIO (4 tools)
        // ====================================================================
        CreatorToolDefinition(
            id = "voiceover_scripts",
            section = StudioSection.VOICE_SCRIPT_STUDIO,
            title = "Video Voice-Over Scripts",
            subtitle = "Paced narration scripts with breathing cues, emphasis & B-roll notes",
            inputLabel = "Video Topic & Core Message",
            inputPlaceholder = "e.g., How small gaming creators can grow from 0 to 10K subscribers",
            secondaryLabel = "Key Points to Cover (Optional)",
            secondaryPlaceholder = "e.g., Thumbnail CTR, first 5 seconds hook, consistent niche",
            supportsDuration = true,
            quickChips = listOf(
                "Documentary-Style Gaming Story",
                "Top 5 Tips Voice-Over Narration",
                "Tech / Smartphone Review Voice-Over",
                "Motivational Creator Journey Script"
            )
        ),
        CreatorToolDefinition(
            id = "short_form_scripts",
            section = StudioSection.VOICE_SCRIPT_STUDIO,
            title = "Short-Form Video Scripts",
            subtitle = "Beat-by-beat Shorts, Reels & TikTok scripts built for 90%+ retention",
            inputLabel = "Short / Reel Idea",
            inputPlaceholder = "e.g., 3 hidden settings every mobile gamer needs to turn off immediately",
            secondaryLabel = "Visual Twist or Payoff at the End (Optional)",
            secondaryPlaceholder = "e.g., Show instant FPS jump on screen",
            supportsDuration = true,
            quickChips = listOf(
                "30s Gaming Secret Tip",
                "60s Myth vs Reality Breakdown",
                "Fast AI Tool Tutorial Reel",
                "Storytime Clutch Commentary"
            )
        ),
        CreatorToolDefinition(
            id = "hooks_intros",
            section = StudioSection.VOICE_SCRIPT_STUDIO,
            title = "Strong Hooks & Introductions",
            subtitle = "10 scroll-stopping first-3-second hooks (curiosity, shock, story & proof)",
            inputLabel = "What Is Your Video About?",
            inputPlaceholder = "e.g., Testing whether a budget phone can run max graphics smoothly",
            secondaryLabel = "Biggest Viewer Pain Point or Surprise (Optional)",
            secondaryPlaceholder = "e.g., Heating issue solved with one setting",
            supportsDuration = false,
            quickChips = listOf(
                "Stop Scrolling Curiosity Hooks",
                "Unpopular Opinion Gaming Hooks",
                "Challenge Stakes Intro",
                "Tutorial Problem-Solution Hook"
            )
        ),
        CreatorToolDefinition(
            id = "calls_to_action",
            section = StudioSection.VOICE_SCRIPT_STUDIO,
            title = "Calls to Action (CTA)",
            subtitle = "Natural subscribe, comment, share & pinned-comment CTAs that convert",
            inputLabel = "Goal of Your Call to Action",
            inputPlaceholder = "e.g., Get viewers to comment their favorite weapon loadout and subscribe",
            secondaryLabel = "Next Video Teaser or Community Prompt (Optional)",
            secondaryPlaceholder = "e.g., Watch part 2 linked below",
            supportsDuration = false,
            quickChips = listOf(
                "High-Comment Debate Question CTA",
                "Seamless Loop Short-Form CTA",
                "Subscribe Without Sounding Beggy",
                "Pinned Comment Engagement Prompt"
            )
        ),

        // ====================================================================
        // D. CREATOR TOOLKIT (5 tools)
        // ====================================================================
        CreatorToolDefinition(
            id = "social_captions",
            section = StudioSection.CREATOR_TOOLKIT,
            title = "Social Media Captions",
            subtitle = "Engaging Instagram, TikTok, Facebook & X captions with micro-hooks",
            inputLabel = "Post Visual or Clip Summary",
            inputPlaceholder = "e.g., Behind-the-scenes photo of my late-night editing setup",
            secondaryLabel = "Mood or Question for Followers (Optional)",
            secondaryPlaceholder = "e.g., Ask followers to rate the setup 1-10",
            supportsDuration = false,
            quickChips = listOf(
                "Setup Showcase Caption",
                "New Video Drop Announcement",
                "Milestone Thank-You Post",
                "Relatable Creator Grind Caption"
            )
        ),
        CreatorToolDefinition(
            id = "hashtag_suggestions",
            section = StudioSection.CREATOR_TOOLKIT,
            title = "Hashtag Suggestions",
            subtitle = "Smart 3-tier hashtag sets (High Volume + Niche + Community)",
            inputLabel = "Post Topic & Creator Niche",
            inputPlaceholder = "e.g., AI digital art & motion design reels",
            secondaryLabel = "Primary Platform & Target Country (Optional)",
            secondaryPlaceholder = "e.g., Instagram Reels & YouTube Shorts",
            supportsDuration = false,
            quickChips = listOf(
                "AI Art & Prompt Engineering",
                "Tech & Gadget Creator",
                "Vlogging & Lifestyle Reels",
                "Educational Micro-Content"
            )
        ),
        CreatorToolDefinition(
            id = "content_planning",
            section = StudioSection.CREATOR_TOOLKIT,
            title = "Content Planning",
            subtitle = "7-day or 30-day structured upload calendar with pillars & formats",
            inputLabel = "Channel Niche & Main Goal",
            inputPlaceholder = "e.g., Gaming + AI tutorials channel aiming for 3 Shorts and 1 long video weekly",
            secondaryLabel = "Available Hours / Posting Frequency (Optional)",
            secondaryPlaceholder = "e.g., 4 uploads per week, evening prime time",
            supportsDuration = false,
            quickChips = listOf(
                "7-Day Viral Shorts Sprint",
                "Weekly Gaming + Live Stream Schedule",
                "New Channel 30-Day Launch Plan",
                "Faceless AI Channel Content Matrix"
            )
        ),
        CreatorToolDefinition(
            id = "video_descriptions",
            section = StudioSection.CREATOR_TOOLKIT,
            title = "Video Descriptions",
            subtitle = "Search-optimized YouTube descriptions, chapters & keyword summary",
            inputLabel = "Video Title & Main Takeaways",
            inputPlaceholder = "e.g., Complete Beginner Guide to Mobile Video Editing in 2026",
            secondaryLabel = "Chapters / Resources to Mention (Optional)",
            secondaryPlaceholder = "e.g., Intro, Keyframes, Color Grading, Export Settings",
            supportsDuration = true,
            quickChips = listOf(
                "Tutorial Video SEO Description",
                "Vlog Episode Description + Chapters",
                "Podcast / Interview Summary",
                "Product Review Key Specs Description"
            )
        ),
        CreatorToolDefinition(
            id = "creative_writing_ideas",
            section = StudioSection.CREATOR_TOOLKIT,
            title = "Creative Writing & Idea Lab",
            subtitle = "Story concepts, channel lore, series names, bios & brainstorming",
            inputLabel = "Creative Concept, Theme, or Brainstorm Prompt",
            inputPlaceholder = "e.g., Invent a cyberpunk gaming clan backstory and 5 memorable series titles",
            secondaryLabel = "Tone / Worldbuilding Rules (Optional)",
            secondaryPlaceholder = "e.g., Futuristic neon-purple rebellion, heroic & mysterious",
            supportsDuration = false,
            quickChips = listOf(
                "Gaming Clan Name & Lore",
                "Channel Bio & Tagline Ideas",
                "Sci-Fi Short Film Concept",
                "10 Unique Series Formats"
            )
        )
    )

    fun toolsForSection(section: StudioSection): List<CreatorToolDefinition> =
        allTools.filter { it.section == section }

    fun findToolById(toolId: String): CreatorToolDefinition? =
        allTools.firstOrNull { it.id == toolId }
}
