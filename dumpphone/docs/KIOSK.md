# Phase 7 — Device Owner / kiosk readiness

Dumpphone ships first as a **normal HOME launcher** (Phase 1). The code path
to harden it into a dedicated device is implemented and guarded; a custom
AOSP ROM is **deferred** — evaluate it only after this kiosk path proves out
on one standard phone model (per plan).

## What is already in the code

- `launcher/KioskManager.kt` — checks `DevicePolicyManager.isDeviceOwnerApp`,
  calls `setLockTaskPackages` for Dumpphone + WhatsApp + dialler,
  and `startLockTask()` / `stopLockTask()`. All no-ops when not Device Owner.
- `launcher/DumpphoneAdminReceiver.kt` + `res/xml/device_admin.xml` —
  provisioning target declared in the manifest.
- Caregiver setting **"Modo quiosque" readiness**: MainActivity attempts
  LockTask on launch only when the caregiver enabled it *and* the app is
  Device Owner. Settings (PIN, SOS, variant, VLM consent) stay behind the
  caregiver PIN in every mode.
- The caregiver settings screen reports live kiosk status
  ("Dumpphone É o dono do dispositivo" vs "ainda não provisionado").

## Provisioning Device Owner (caregiver/technical person only)

> ⚠️ This requires a **factory-reset phone with no accounts signed in**
> (Android refuses Device Owner otherwise). Back up the phone first.

```bash
# 1. Install the pilot APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Make Dumpphone the Device Owner
adb shell dpm set-device-owner com.dumpphone/.launcher.DumpphoneAdminReceiver

# Expected: "Success: Device owner set to package com.dumpphone"
```

Then: open Dumpphone → gear → caregiver PIN → enable kiosk use, set it as
the default HOME app, and reboot. The user should now be unable to leave
Dumpphone with Back/Home; WhatsApp and the dialler remain reachable only
through Dumpphone's own flows (LockTask whitelist above).

## Undoing it (returning the phone to normal)

```bash
# From a computer (removes Device Owner; may require factory reset on some models):
adb shell dpm remove-active-admin com.dumpphone/.launcher.DumpphoneAdminReceiver
```

Or factory-reset the phone. Document which route worked on the pilot model
in `docs/BASELINE.md`.

## What is deliberately NOT done yet

- No managed-Google-Play / Android Enterprise enrollment.
- No remote family configuration server — settings are on-device, behind PIN.
- No custom ROM, no system-app signing, no removal of the setup wizard.
  Revisit only after: (a) LockTask survives reboot + app updates on the
  pilot model, and (b) a supervised elderly-user session (Phase 6) passes.

## Side-load distribution note

There are **no Play Store promises** for this project. AccessibilityService
use for WhatsApp automation is restricted by Google Play policy;
the pilot is distributed as a direct (side-loaded) APK install — see the
root `README.md`.
