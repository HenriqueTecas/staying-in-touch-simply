#!/usr/bin/env python3
"""Tier C on the USB phone. Stops at the confirmation screen.

SAFETY (do not relax):
- Never taps SIM / Enviar / the call button.
- Never runs SOS cases (those would dial).
- Never issues ACTION_CALL, sms send, or WhatsApp send.
- Success means the confirmation sentence is on screen.
- Alarm is NOT confirmed either, unless you pass --confirm-alarms, which only
  taps SIM when the on-screen text contains "despertador" and nothing else.
- Does not change the default HOME launcher unless you pass --set-home.

Requires the debug build that logs DUMPPHONE_BENCH (bench transcript extra).
The phone must show as `device` in `adb devices` (not `unauthorized`).
"""
import argparse, json, subprocess, sys, time
from pathlib import Path

HERE = Path(__file__).resolve().parent
EVALS = HERE.parent
DATASET = EVALS / "voice_benchmark.json"
RESULTS = EVALS / "results"
PKG = "com.dumpphone"

def adb(*args, check=True):
    r = subprocess.run(["adb", *args], capture_output=True, text=True)
    if check and r.returncode != 0:
        sys.exit(f"adb {' '.join(args)} failed: {r.stderr.strip()[:200]}")
    return r

def device_ready():
    out = adb("devices", check=False).stdout
    lines = [ln.split() for ln in out.splitlines()[1:] if ln.strip()]
    return any(len(p) >= 2 and p[1] == "device" for p in lines)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--install", metavar="APK", help="install this apk, then grant mic/contacts/phone")
    ap.add_argument("--set-home", action="store_true", help="offer Dumpphone as HOME (not done by default)")
    ap.add_argument("--confirm-alarms", action="store_true",
                    help="ONLY for alarm phrases: tap SIM. Never used for calls or messages.")
    ap.add_argument("--limit", type=int, default=8)
    args = ap.parse_args()

    if not device_ready():
        sys.exit("No authorised adb device. Unlock the phone and accept the USB debugging prompt. Nothing was sent.")

    if args.install:
        adb("install", "-r", args.install)
        for perm in ("RECORD_AUDIO", "READ_CONTACTS", "CALL_PHONE"):
            adb("shell", "pm", "grant", PKG, f"android.permission.{perm}", check=False)
    if args.set_home:
        print("HOME change is a manual chooser on Android 10+. Not setting it automatically.")

    cases = json.loads(DATASET.read_text(encoding="utf-8"))["cases"]
    # Calls, messages and SOS are observed only. They are never confirmed.
    runnable = [c for c in cases if c["task"] != "sos"][: args.limit]
    rows = []
    for c in runnable:
        adb("shell", "am", "force-stop", PKG, check=False)
        adb("logcat", "-c", check=False)
        adb("shell", "am", "start", "-n", f"{PKG}/.launcher.MainActivity",
            "--es", "dumpphone_bench_transcript", c["transcript"],
            "--es", "dumpphone_bench_nonce", c["id"])
        time.sleep(2.5)
        log = adb("logcat", "-d", "-s", "DUMPPHONE_BENCH", check=False).stdout
        ui = adb("exec-out", "uiautomator", "dump", "/dev/tty", check=False).stdout
        prompt_ok = "Quer " in ui
        # Hard refusal: never tap a positive button. Alarm confirm is opt-in and text-gated.
        if args.confirm_alarms and c["task"] == "alarm" and "despertador" in ui.lower():
            print(f"{c['id']}: --confirm-alarms was set; this build does not auto-tap. Confirm on the phone by hand.")
        rows.append({"id": c["id"], "task": c["task"], "confirmation_visible": prompt_ok,
                     "log_seen": "DUMPPHONE_BENCH" in log, "executed": False})
        print(f"{c['id']}: confirmation_visible={prompt_ok} executed=false")
        adb("shell", "input", "keyevent", "KEYCODE_BACK", check=False)
    RESULTS.mkdir(parents=True, exist_ok=True)
    (RESULTS / "tier-c-results.json").write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Tier C done: {sum(1 for r in rows if r['confirmation_visible'])}/{len(rows)} confirmations visible, 0 actions executed")

if __name__ == "__main__":
    main()
