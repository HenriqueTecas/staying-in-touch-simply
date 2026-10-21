# Phase 0 — Test-phone baseline (fill in on the user's own phone)

> Status: **template, filled in as far as possible without the physical phone.**
> Everything marked `A PREENCHER` must be completed by the user on their own
> phone before the pilot session (Phase 6). Defaults below are the locked
> decisions; do not silently change them — note any change in the last column.

## 1. Device baseline

| Field | Value | Notes |
|---|---|---|
| Phone make / model | A PREENCHER (e.g. Samsung Galaxy A15) | Settings → About phone |
| Android version / API level | A PREENCHER (app requires Android 8.0 / API 26+) | Settings → About phone → Android version |
| WhatsApp version | A PREENCHER | WhatsApp → Settings → Help → App info |
| WhatsApp package present | A PREENCHER (expected `com.whatsapp`) | Check it opens from the WhatsApp tile |
| Portuguese variant | **pt-PT (default)** — change in Dumpphone caregiver settings if the user speaks pt-BR | Affects SpeechRecognizer locale, TTS voice, and `wa.me` country-code defaults (+351 vs +55) |
| TTS Portuguese voice installed | A PREENCHER (yes/no) | Settings → Accessibility / General management → Text-to-speech; download a Portuguese voice if missing |
| On-device speech recognition available | A PREENCHER (yes/no) | Tap FALAR with Wi-Fi/mobile data off; if it fails, enable "On-device recognition" / download the Portuguese language pack in the Google app / Gboard settings |
| Screen size / font scale | A PREENCHER | Set system font size to the largest comfortable size; Dumpphone uses its own oversized type, but system dialogs inherit this |
| Caregiver PIN changed from default `1234` | A PREENCHER (must be YES before handover) | Dumpphone → gear icon → enter `1234` → "Mudar o PIN" |

## 2. How to fill this in (5 minutes, on the phone)

1. Install Dumpphone (see `README.md` at the project root, "Install on a phone").
2. Open Settings → About phone; copy model + Android version into the table.
3. Open WhatsApp once and note its version. Messenger is not part of this phone.
4. In Dumpphone: gear icon (top right) → PIN `1234` → confirm the Portuguese
   variant (pt-PT default; switch to pt-BR only if the user is Brazilian),
   set the SOS contact name + number, and change the caregiver PIN.
5. Speak each phrase in `evals/voice_intents.json` via the FALAR button and
   tick it off in §3 below; note any phrase that was mis-heard verbatim.

## 3. Fixed launch-task checklist (acceptance for v1)

Every task must be completable **by voice in Portuguese**, in **≤2 taps**
from the home screen (one confirmation counts as the second step), with
**no message ever sent without read-back confirmation**.

| # | Task (say it like this) | Tier expected | Done on phone? |
|---|---|---|---|
| 1 | Call a favourite contact — "Ligar para a Maria" | 1 (tel:) | ☐ |
| 2 | Send an SMS — "Manda um SMS para a Maria a dizer já cheguei" | 1 (sms:) | ☐ |
| 3 | Set an alarm — "Põe o despertador para as sete e meia" | 1 (AlarmClock) | ☐ |
| 4 | Send a WhatsApp message — "Manda um WhatsApp à Maria a dizer já cheguei" | 1 (wa.me) or 2 | ☐ |
| 5 | Messenger is unavailable — "Manda uma mensagem no Messenger para a Ana a dizer olá" stays in Dumpphone, opens nothing, sends nothing | spoken refusal | ☐ |
| 6 | Read new messages aloud — "Ler as mensagens do WhatsApp" | 2 (accessibility + TTS) | ☐ |
| 7 | SOS — tap the red **SOS — Ajuda** tile (or say "Socorro") | 1 (call SOS number / 112) | ☐ |
| 8 | Ambiguous name — "Ligar para a Maria" with two Marias in contacts → must ask which one, never guess | Safety | ☐ |
| 9 | Nickname — "Ligar para o Zé" matches José | Safety/matching | ☐ |
| 10 | Cancel mid-task — say "Cancela" / tap NÃO at confirmation → nothing is sent or dialled | Safety | ☐ |
| 11 | Caregiver settings behind PIN — gear icon asks for PIN; wrong PIN is rejected | Safety | ☐ |
| 12 | Action log — caregiver can read, in plain Portuguese, what happened ("14:03 — A ligar para Maria Silva") | Safety | ☐ |
| 13 | Unknown request with VLM consent OFF — "Qual é o tempo amanhã?" → app asks again, does not guess or open a browser | Router | ☐ |
| 14 | (Optional, consent ON) Unknown request → demo/narration runs, stops and asks instead of acting blindly | 3 (VLM/demo) | ☐ |

Record pass/fail per task, the tier that actually resolved it, and any
mis-heard phrase. VLM (Tier 3) should be needed for **< ~20%** of these tasks.

## 4. Known blanks only the user can fill

- Exact phone model / Android build and the two app versions above.
- Whether pt-PT on-device recognition works offline on that model.
- Which contacts are favourited (starred) and their nicknames
  (e.g. "mãezinha" → mother): add per-contact nicknames via caregiver notes
  today in `SettingsStore` nicknames; a contacts-editing UI is a pilot follow-up.
- The real SOS number (empty = 112).
