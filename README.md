# SN Creativity AI 2.0 — Native Android Studio Project

**Package ID:** `com.sncreator.app`  
**Language & UI:** 100% Kotlin + Jetpack Compose (Material 3)  
**Build System:** Gradle Kotlin DSL (`.gradle.kts`)  
**Expected Debug APK Output:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 1. Architecture & Core Modules

- **Animated 3D Splash Screen (`ui/screens/SplashScreen.kt`, `ui/components/Interactive3DComponents.kt`)**: Genuine perspective-projected rotating 3D crystal octahedron + glowing `SN` emblem, floor reflection, drifting neon-purple particles, and smooth transition into the main dashboard.
- **Four Primary Creator Studios (`domain/catalog/CreatorToolCatalog.kt`, `ui/screens/DashboardScreen.kt`)**:
  1. **Gaming Studio**: Gaming Title Generator, Gaming Hashtags, Gaming Video Ideas, Gaming Descriptions & Captions, and Game-Specific Tips.
  2. **AI Prompt Studio**: Advanced Prompt Enhancer, Image-Generation Prompts, Video-Generation Prompts, Thumbnail Prompts, Google Flow / Sora / Creator Workflow Presets, and Search & Research Assistant (with optional Google Search Grounding).
  3. **Voice & Script Studio**: Video Voice-Over Scripts, Short-Form Video Scripts, Strong Hooks & Introductions, and Calls to Action.
  4. **Creator Toolkit**: Social Media Captions, Hashtag Suggestions, Content Planning, Video Descriptions, and Creative Writing & Idea Lab.
- **Themes (`ui/theme/`)**: Selectable **Neon Dark** and **Creative Light** themes persisted locally via Jetpack DataStore.
- **Languages & Creator Presets**: Full generation support for **English** and **Roman Urdu**, Short/Detailed output, Tone, Video Duration, `9:16` / `16:9` / `1:1` formats, and platform presets.
- **Local Prompt History (`data/local/PromptHistoryDatabase.kt`, `ui/screens/HistoryScreen.kt`)**: Room SQLite database storing generated outputs, bookmarks, copy, prompt reuse, single-entry deletion, and confirmed Clear All.

---

## 2. Configuring Google Gemini AI Securely

SN Creativity AI 2.0 compiles cleanly **without** requiring a real API key at build time. No API secrets are hardcoded in Kotlin source or committed to Git.

### Option A — Production Architecture (Recommended for Public APK Distribution)
Android APKs can be decompiled; never ship a private API key inside a publicly shared APK.
1. Deploy a lightweight HTTPS proxy endpoint (e.g., Google Cloud Run or Firebase Cloud Functions) that holds your `GEMINI_API_KEY` in server-side secret storage and forwards requests matching `SecureBackendProxyRequest` (`data/remote/GeminiApiModels.kt`).
2. Set `GEMINI_BACKEND_PROXY_URL=https://your-secure-proxy.example.com/generate` in your gitignored `.env` file (or via the **Secrets panel in Google AI Studio**).
3. When `GEMINI_BACKEND_PROXY_URL` is configured, `CreatorAiRepository` automatically routes requests through your backend proxy instead of using a client key.

### Option B — Local Prototyping / Google AI Studio Preview
1. Obtain a Gemini API key from Google AI Studio.
2. In **Google AI Studio Build**, open the **Secrets** panel and set `GEMINI_API_KEY`.
3. In local **Android Studio**, copy `.env.example` to `.env` (already in `.gitignore`) and set:
   ```properties
   GEMINI_API_KEY=your_actual_key_here
   ```
4. If neither `GEMINI_BACKEND_PROXY_URL` nor `GEMINI_API_KEY` is configured, the app clearly displays **"Gemini Setup Required"** and offers the offline **Local Studio Template** fallback instead of faking an AI response.

### Gemini Free-Tier & Quota Notes
- Google AI Studio provides a rate-limited free tier for supported Gemini models (such as `gemini-3.5-flash`) in eligible regions.
- Free-tier quotas, requests-per-minute (RPM), daily limits, and Google Search grounding availability vary by account/region and are **not guaranteed**.
- Cloud billing is **never** enabled automatically by this app. HTTP `429` quota/rate-limit errors, network timeouts, and offline states are caught gracefully with Retry and Local Fallback options.

---

## 3. Configuring Owner WhatsApp Support Number

1. Open `app/src/main/java/com/example/config/OwnerSupportConfig.kt`.
2. Set `OWNER_WHATSAPP_NUMBER` to the owner's real international WhatsApp number using digits only (country code + number, no `+` or spaces, e.g., `"923001234567"`).
3. No WhatsApp login and zero contact permissions (`READ_CONTACTS`) are used. If WhatsApp is not installed on the user's phone, the app safely falls back to the universal `https://wa.me/` browser link.

---

## 4. Building & Testing

- **AI Studio / Container CLI:** `gradle assembleDebug` and `gradle :app:testDebugUnitTest`
- **Local Android Studio / GitHub Actions:** Open project in Android Studio or run Gradle `assembleDebug`. Output APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
