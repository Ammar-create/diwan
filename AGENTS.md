# Diwan (دیوان) — Architectural Overhaul Blueprint & Agent Playbook

> **Status:** Phase 1 complete (3/10 build verified). Awaiting Phase 2 execution.  
> **Target Device:** Lenovo Tab M9 (Android 13, 4GB RAM).  
> **Design Standards:** Apple Fluid Interfaces (`apple-design`), Emil Kowalski motion (`/skills/animate`), and strict anti-slop typography/hierarchy (`unslop`).

---

## 1. Executive Post-Mortem & The 3/10 Verdict

The initial prototype proved the technical pipeline (clean compilation, single minified APK, authentic Nastaliq rendering, and fluid deck carousel physics), but the UI/UX suffered from fragmented architecture and crude interface design.

### What Earned the 3 Marks:
1. **Build & Deployment Pipeline:** Smooth CI/CD build via GitHub Actions, zero-tap silent Shizuku installation, and compact asset footprint.
2. **Parchment Base Palette:** Warm `#FDFBF7` manuscript background and `#0D3B66` lapis ink hierarchy.
3. **Poem Deck Carousel:** Smooth Compose `HorizontalPager` with continuous lerp scaling and capsule indicators.

### What Was Completely Hideous & Failed:
1. **Clunky Settings Page:**
   - Full-width text fields with oversized Comic Sans fonts displaying long API keys and base URLs.
   - Disconnected, chunky pill buttons with awkward padding.
   - Cluttered vertical layout lacking Apple-grade grouped sectioning and subtle hairlines.
2. **Crude Chat Interface:**
   - Completely blank, cavernous white void with an ugly, static message bubble.
   - Overlapping header text (`محفلِ سخن (Poet's Salon)` crashed against subtitle).
   - Amateur, boxy outlined input bar with a generic circle send button.
3. **Fragmented Features (The OCR/Romanization Disconnect):**
   - Having a separate "Scan Calligraphy" screen and separate "Romanization" toggle created a disjointed experience.
   - No image attachment in chat, no direct clipboard paste, no streaming tokens, and no thinking animation.

---

## 2. Core Architectural Pivot: The Unified Chat Sanctuary

Instead of dividing the app into a separate OCR scanner, a separate chat tab, and a separate romanization tool, **the Chat Interface becomes the unified heartbeat of Diwan.**

### The Workflow:
1. **Drop Calligraphy into Chat:** Tap the photo icon or **directly paste an image from clipboard** right into the chat input bar.
2. **Live Reasoning / Thinking:** The AI streams its internal reasoning in an animated, collapsible `<think>` block (mimicking LastChat's thinking accordion with elapsed timer and shimmer).
3. **Streaming Manuscript Extraction:** The AI streams the verified Urdu Nastaliq verses, followed by rhythmic Roman Urdu and literary explanation.
4. **Interactive "Add to Folio" Card Artifact:** Underneath the streaming message, an interactive Compose card preview appears with a single "Save to My Folio" action button that drops the couplets straight into the home carousel.
5. **Conversational Debate:** Continue the conversation immediately—ask about meter (*bahr*), Persian roots, or alternative couplets in the same fluid thread.

---

## 3. Porting LastChat's Chat Engine Components

To bring Diwan's chat to parity with LastChat's polished experience, port and adapt the following modules from `/workspace/apps/LastChat/`:

### A. MinimalChatInput & Attachment Engine
* **Source:** `me.rerere.rikkahub.ui.components.ai.MinimalChatInput.kt` and `MinimalChatInputAttachmentSupport.kt`.
* **Features:**
  * Floating pill/capsule input bar with smooth multi-line expansion.
  * Image attachment button with thumbnail preview strip and remove badges (`AnimatedVisibility`).
  * **Direct Clipboard Paste Handler:** Seamlessly detects image/data URIs on long-press or tap and stages them into the attachment bar.
  * Morphing Send / Stop Streaming action button with haptic feedback.

### B. Server-Sent Events (SSE) Streaming Client
* **Source:** `OpenAiClient.kt` overhaul using OkHttp `EventSource` or streaming `ResponseBody.byteStream()`.
* **Features:**
  * Emits streaming token deltas in real-time.
  * Prevents word welding by avoiding `.trim()` on streaming deltas (as established in LastChat AGENTS.md Section 7).
  * Smooth auto-scroll behavior tracking new tokens.

### C. Collapsible Thinking / Reasoning Block
* Renders `<think>...</think>` tokens in a frosted glass accordion with subtle pulsing rim and active elapsed-seconds counter.
* Collapses automatically on completion or remains expandable on tap.

---

## 4. Settings Page Redesign (Apple & Unslop Pass)

1. **Grouped Inset Lists:**
   - Replace standalone rectangular text inputs with grouped, rounded-corner cards (16dp radius) featuring subtle hairline dividers (`0.5dp` opacity 10%).
   - Inputs formatted cleanly with muted monospace or system body typography (restricting Comic Sans to playful preview contexts rather than technical API fields).
2. **Compact Theme & Font Switchers:**
   - Segmented sliding tabs with spring indicators for:
     - Theme: `System Default` | `Dark Nocturne` | `Parchment Light`
     - Urdu Font: `Noto Nastaliq` | `Jameel Noori` | `Gulzar`
     - Latin Font: `Instrument Serif` | `Google Sans Flex` | `Comic Neue` | `Inter`
3. **Debug Logger Modal:**
   - Retain the newly built `DebugLogger` console dialog accessible via an unobtrusive bug icon in the top app bar for one-tap crash inspection.

---

## 5. File Inventory & Modification Targets for Phase 2

| Target File | Planned Changes |
| :--- | :--- |
| `data/api/OpenAiClient.kt` | Implement SSE streaming chat completions (`chatStream`) with image upload payload support. |
| `ui/chat/PoetSalonScreen.kt` | Replace crude layout with LastChat's chat stream, thinking accordion, and rich message bubbles. |
| `ui/chat/PoetSalonViewModel.kt` | Implement streaming state flow, attachment staging, and clipboard image parser. |
| `ui/chat/components/DiwanChatInput.kt` | Port `MinimalChatInput` with image picker, clipboard paste, and fluid animations. |
| `ui/chat/components/ThinkingBlock.kt` | Collapsible reasoning block with pulse and timer. |
| `ui/settings/SettingsScreen.kt` | Full Apple-grade grouped list unslop pass; clean up font sizes and text fields. |
| `ui/dashboard/DashboardScreen.kt` | Refine top bar and integrate direct "Add to Folio" intent from chat. |

---

## 6. Phase 2 Execution Rules

* **DO NOT touch code until Ammar returns and gives the explicit signal.**
* When ready, execute the streaming client and input attachment port first.
* Build exclusively via GitHub Actions CI (`build-stable.yml`). Never compile locally in PRoot.
