# Dumpphone

A Portuguese-first Android launcher that gives an elderly user a dumb-phone
surface — **Ligar, Mensagens, Despertador, WhatsApp, Messenger, SOS** — backed
by Portuguese voice control, with a strict safety rule: **no message is ever
sent and no ambiguous contact is ever called without a spoken read-back
confirmation.**

Architecture (locked plan): **intent first, accessibility second, VLM last
resort.** Most tasks use deterministic Android intents (`tel:`, SMS,
`AlarmClock`, `wa.me`); an AccessibilityService handles what intents cannot
(open a named WhatsApp/Messenger chat, type a confirmed message, read messages
aloud); a provider-agnostic vision-model loop exists only as an opt-in,
consented, step-capped fallback — and runs as an honest **demo** when no API
key is configured.

- 📋 Phase 0 baseline + launch-task checklist: [`docs/BASELINE.md`](docs/BASELINE.md)
- 🔒 Phase 7 kiosk / Device Owner path: [`docs/KIOSK.md`](docs/KIOSK.md)
- 🧪 Evals: [`evals/voice_intents.json`](evals/voice_intents.json), [`evals/automation_scenarios.json`](evals/automation_scenarios.json)

## Install on a phone (pilot, side-loaded — no Play Store)

> There are **no Play Store promises** for this project. Google Play restricts
> AccessibilityService use for chat automation, so the pilot is installed
> directly (side-loaded). Only install on a phone you own.

You need the debug APK: `app-debug.apk` (shipped alongside this project in
the deliverable folder, or built with `./gradlew assembleDebug` →
`app/build/outputs/apk/debug/app-debug.apk`).

1. **Copy the APK to the phone** (USB cable, email it to yourself, or
   `adb install -r app-debug.apk` from a computer).
2. On the phone, tap the APK in Files/Downloads. If asked, allow
   **"Install unknown apps"** for the Files app, then tap **Install**.
3. Open **Dumpphone**. Allow the permissions it asks for:
   **Contacts** (favourite contacts + name matching), **Microphone**
   (the FALAR voice button), **Phone** (one-tap calls and SOS).
4. **Make Dumpphone the home screen:** press the phone's Home button and
   choose **Dumpphone → Always**, or go to Dumpphone → gear icon → caregiver
   PIN (`1234`) → **"Escolher Dumpphone como ecrã inicial"** and select it.
5. **Enable accessibility (needed for Messenger + reading messages aloud):**
   gear icon → PIN → **"Ativar acessibilidade do Dumpphone"** → find
   **Dumpphone** in the list → turn it on → confirm Android's warning.
   WhatsApp, calls, SMS and alarms work without this; Messenger typing and
   "Ler as minhas mensagens" do not.
6. **Caregiver setup (do this once, before handing the phone over):**
   gear icon → PIN `1234` → set the Portuguese variant (**pt-PT** default,
   pt-BR if needed), the **SOS contact name + number** (empty = 112),
   and **change the PIN** from `1234`.
7. Fill in [`docs/BASELINE.md`](docs/BASELINE.md) and run its 14-task checklist.

To go back to the normal launcher: Android Settings → Apps → Default apps →
Home app → choose the previous launcher. Nothing about the phone is changed
irreversibly; uninstalling Dumpphone restores everything.

## Using it (what the elderly user sees)

- Six huge buttons. Favourite (starred) contacts below — tap a face/name to call.
- **FALAR** (big black button): push-to-talk, in Portuguese. Try:
  - *"Ligar para a Maria"*
  - *"Manda um WhatsApp à Maria a dizer já cheguei"*
  - *"Manda uma mensagem no Messenger para a Ana a dizer olá"*
  - *"Põe o despertador para as sete e meia"*
  - *"Ler as mensagens do WhatsApp"*
  - *"Socorro"* (SOS)
- Before anything is sent or dialled, Dumpphone reads it back —
  *"Quer enviar 'Já cheguei' para a Maria Silva?"* — and waits for **SIM/NÃO**.
  If two contacts share a name, it asks which one. It never guesses.
- **SOS — Ajuda** (red) calls the SOS number (or 112) immediately, in one tap.

## Build & test (developers)

Requirements: JDK 17+, Android SDK platform 34 + build-tools 34.

```bash
echo "sdk.dir=/path/to/android-sdk" > local.properties   # or set ANDROID_HOME
./gradlew testDebugUnitTest   # 52 unit tests: parser, matching, router, safety, VLM loop
./gradlew assembleDebug       # -> app/build/outputs/apk/debug/app-debug.apk
```

Validated in the build environment for this deliverable: **52/52 unit tests
pass, `assembleDebug` succeeds.** Everything else (real STT/TTS voices,
WhatsApp/Messenger behaviour, HOME-launcher replacement, LockTask) can only
be verified on the physical pilot phone — see the checklist in
`docs/BASELINE.md`.

## Project layout

```
app/src/main/java/com/dumpphone/
  launcher/  Compose home screen (HOME launcher), MainActivity, kiosk (Phase 7)
  voice/     SpeechRecognizer (pt, on-device preferred) + cloud-STT stub,
             rule-based PT intent parser (+ LLM-pluggable interface), TTS
  router/    Tier 1 / 2 / 3 routing decisions
  actions/   Tier-1 intents: tel:, sms:, AlarmClock, wa.me, m.me
  a11y/      AccessibilityService executor + WhatsApp/Messenger flows
  vlm/       Provider-agnostic VLM loop: consent, narration, step/cost caps,
             stop-and-ask; MockVlmClient demo when no key is configured
  safety/    Confirmation policy, PT contact matching (nicknames),
             caregiver PIN (salted hash), plain-language action log
  data/      Contacts, settings, pt-PT/pt-BR variant
docs/        BASELINE.md (Phase 0), KIOSK.md (Phase 7)
evals/       PT voice-intent cases + WhatsApp/Messenger automation scenarios
```

## Honest limitations of this pilot

- **Tier 2 is best-effort.** WhatsApp/Messenger have no official personal
  APIs; app updates can break accessibility node matching. Failures are
  spoken aloud and **nothing is sent** — re-run `evals/automation_scenarios.json`
  after those apps update.
- **Messenger cannot pre-fill text via deep link**, so without accessibility
  the app opens the chat and says plainly that it cannot type for the user.
  It never falsely claims a message was sent.
- **Tier 3 (VLM) is a demo without a configured endpoint + key**, and its
  HTTP transport is a stub (`HttpVlmClient`) — by design, it does not pretend
  a real vision model works without one. Screenshots are never captured or
  logged by default; the loop prefers the accessibility UI tree.
- **Cloud STT** is likewise a stub behind the `SpeechToTextEngine` interface
  until a Whisper-class endpoint/key is configured by the caregiver.
- On-device Portuguese recognition/TTS quality depends on the phone model
  and its downloaded language packs — that is exactly what Phase 0 records.
- Demo contacts labelled "(exemplo)" appear only when contacts permission is
  denied or the phone has no contacts, so the UI is never blank in testing.
