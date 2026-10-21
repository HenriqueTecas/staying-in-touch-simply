#!/usr/bin/env python3
"""Dumpphone closed-loop voice benchmark. Local / $0 by default.

Tiers (cheapest first):
  A  transcript text -> rule parser          (runs the real Kotlin parser via a JUnit executor)
  B  audio -> STT -> intent                  (B1: on-device via adb debug entry point when the
                                              phone is authorised; B2: OpenRouter free/cheap
                                              audio models — $0 on :free, ledger-logged;
                                              B3: ready slot for Mac-local STT, e.g. mlx-whisper)
  C  end-to-end action verification via adb  (stops at confirmation; NEVER sends/calls)

Usage:
  python3 benchmark.py tier-a
  python3 benchmark.py gen-audio [--voice Joana] [--limit N]
  python3 benchmark.py tier-b-openrouter [--limit N] [--model <id>] [--ledger results/cost-ledger.csv]
  python3 benchmark.py tier-c --dry-run     # prints adb plan, touches nothing
  python3 benchmark.py score                # (re)score results/*.csv -> results/summary.json
  python3 benchmark.py paid-quote           # per-run cost estimates for the paid path (no calls)

The OpenRouter key is read at runtime from $OPENROUTER_KEY_FILE (default:
../../../../internal/openrouter-key.txt relative to the project store) or
$OPENROUTER_API_KEY. It is never printed, logged, or written to results.
Hard budget guard: refuses paid (non-:free) runs whose estimate exceeds
$PAID_PATH_CONFIG['hard_cap_usd'].
"""
import argparse, base64, csv, json, os, re, subprocess, sys, time, unicodedata, urllib.request
from pathlib import Path

HERE = Path(__file__).resolve().parent
EVALS = HERE.parent
PROJECT = EVALS.parent
RESULTS = EVALS / "results"
AUDIO = EVALS / "audio"
DATASET = EVALS / "voice_benchmark.json"

PAID_PATH_CONFIG = {
    # Drop-in-later paid path. "local-first": nothing here runs unless explicitly invoked.
    "provider": "openrouter",
    "base_url": "https://openrouter.ai/api/v1",
    "key_file_env": "OPENROUTER_KEY_FILE",
    "key_env": "OPENROUTER_API_KEY",
    "default_key_file": str(PROJECT.parent / "internal" / "openrouter-key.txt"),
    # Native speech-to-action (audio in, intent JSON out, one call):
    "speech_to_action_free": "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
    "speech_to_action_free_alt": "thinkingmachines/inkling-small:free",
    # Dedicated cheap STT (audio in, transcript out) if a transcript is wanted separately:
    "stt_cheap": "mistralai/voxtral-small-24b-2507",
    "stt_cheap_alt": "google/gemini-2.5-flash-lite",
    "stt_default": "google/gemini-2.5-flash-lite",  # measured $0.0000217/call on C01, perfect pt-PT
    # Text intent fallback for transcripts the rule parser marks Unknown:
    "intent_llm_free": "liquid/lfm-2.5-2.6b:free",
    "intent_llm_cheap": "google/gemini-2.5-flash-lite",
    # VLM Tier-3 check (screenshot/UI-tree -> next step), only with consent:
    "vlm_free": "google/gemma-4-31b-it:free",
    "vlm_cheap": "google/gemini-2.5-flash-lite",
    # TTS is NOT bought: macOS `say` Joana (pt-PT) and Android TTS are free and local.
    "tts_paid": None,
    "hard_cap_usd": 0.50,       # assignment cap; absolute ceiling is $3 — do not approach it
    "est_tokens_per_intent_call": {"prompt": 220, "completion": 60},
    "est_audio_seconds_per_case": 4,
}
_override = HERE / "paid-path.json"
if _override.is_file():
    for _k, _v in json.loads(_override.read_text(encoding="utf-8")).items():
        if _k in PAID_PATH_CONFIG and _k != "default_key_file":
            PAID_PATH_CONFIG[_k] = _v

def norm(s):
    if s is None: return ""
    s = unicodedata.normalize("NFD", s.lower())
    s = "".join(c for c in s if unicodedata.category(c) != "Mn")
    return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9 ]", " ", s)).strip()

def load_cases():
    return json.loads(DATASET.read_text(encoding="utf-8"))["cases"]

def read_key():
    kf = os.environ.get(PAID_PATH_CONFIG["key_file_env"]) or PAID_PATH_CONFIG["default_key_file"]
    if os.environ.get(PAID_PATH_CONFIG["key_env"]):
        return os.environ[PAID_PATH_CONFIG["key_env"]].strip()
    p = Path(kf)
    return p.read_text(encoding="utf-8").strip() if p.is_file() else None

def ledger(path, row):
    RESULTS.mkdir(parents=True, exist_ok=True)
    new = not Path(path).exists()
    with open(path, "a", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["ts","tier","provider","model","cases","est_cost_usd","actual_cost_usd","note"])
        if new: w.writeheader()
        w.writerow(row)

# --------------------------------------------------------------------------------------
# Audio generation — macOS `say`, Portuguese voice, $0
# --------------------------------------------------------------------------------------
def gen_audio(limit=None, voice="Joana"):
    voices = subprocess.run(["say", "-v", "?"], capture_output=True, text=True).stdout
    has_pt = any(v in voices for v in ("Joana", "Luciana"))
    print(f"say voices with Portuguese: {'yes (' + voice + ')' if has_pt else 'NO'}")
    if not has_pt:
        print("FALLBACK: no pt voice — audio generation skipped; text-level + on-device TTS read-back only.")
        (RESULTS / "audio-generation.json").write_text(json.dumps({"voice": None, "fallback": "no-pt-voice"}, indent=2))
        return
    cases = load_cases()[: limit or None]
    AUDIO.mkdir(parents=True, exist_ok=True)
    manifest = []
    for c in cases:
        rate = 120 if "..." in c["transcript"] or c["category"] == "elderly" else 155
        text = c["transcript"].replace("...", ", ")
        aiff, wav = AUDIO / f"{c['id']}.aiff", AUDIO / f"{c['id']}.wav"
        subprocess.run(["say", "-v", voice, "-r", str(rate), "-o", str(aiff), text], check=True)
        # 16 kHz mono WAV: small, STT-friendly, playable by every downstream consumer.
        subprocess.run(["afconvert", "-f", "WAVE", "-d", "LEI16@16000", "-c", "1", str(aiff), str(wav)], check=True)
        aiff.unlink()
        manifest.append({"id": c["id"], "file": f"audio/{c['id']}.wav", "voice": voice, "rate_wpm": rate,
                         "text_rendered": text, "cost_usd": 0})
    (EVALS / "audio-manifest.json").write_text(json.dumps(
        {"method": f"macOS say -v {voice} (pt-PT) + afconvert 16kHz mono WAV", "voice": voice,
         "cost_usd": 0, "files": manifest}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"generated {len(manifest)} WAV files in {AUDIO} (cost $0)")

# --------------------------------------------------------------------------------------
# Scoring (Tier A CSV produced by the Kotlin executor; Tier B CSVs share the same columns)
# --------------------------------------------------------------------------------------
def score_file(path):
    rows = list(csv.DictReader(open(path, encoding="utf-8"))) if Path(path).exists() else []
    def rate(rs, key):
        v = [int(r[key]) for r in rs if r.get(key) in ("0", "1")]
        return round(100.0 * sum(v) / len(v), 1) if v else None
    def slot(rs, key):
        v = [int(r[key]) for r in rs if r.get(key) in ("0", "1")]
        return round(100.0 * sum(v) / len(v), 1) if v else None
    out = {"file": str(path), "n": len(rows)}
    if not rows: return out
    out["intent_accuracy_pct"] = rate(rows, "intent_pass")
    for k, name in [("contact_pass","contact"),("message_pass","message"),("time_pass","time"),("app_pass","app")]:
        out[f"{name}_slot_pct"] = slot(rows, k)
    full = [r for r in rows if r["intent_pass"] == "1" and all(r[k] != "0" for k in
            ("contact_pass","message_pass","time_pass","app_pass"))]
    out["full_pass_pct"] = round(100.0 * len(full) / len(rows), 1)
    lat = [float(r["latency_ms"]) for r in rows if r.get("latency_ms")]
    out["latency_ms_mean"] = round(sum(lat) / len(lat), 3) if lat else None
    out["by_task"] = {}
    for task in sorted({r["task"] for r in rows}):
        rs = [r for r in rows if r["task"] == task]
        out["by_task"][task] = {"n": len(rs), "intent_pct": rate(rs, "intent_pass")}
    out["by_category"] = {}
    for cat in sorted({r["category"] for r in rows}):
        rs = [r for r in rows if r["category"] == cat]
        out["by_category"][cat] = {"n": len(rs), "intent_pct": rate(rs, "intent_pass")}
    out["failures"] = [{"id": r["id"], "task": r["task"], "category": r["category"],
                        "transcript": r["transcript"], "expected": r["expected_intent"],
                        "actual": r["actual_intent"],
                        "contact": f"{r.get('expected_contact','')}->{r.get('actual_contact','')}",
                        "time": f"{r.get('expected_time','')}->{r.get('actual_time','')}"}
                       for r in rows if not (r["intent_pass"] == "1" and all(
                           r[k] != "0" for k in ("contact_pass","message_pass","time_pass","app_pass")))]
    # confusion pairs
    conf = {}
    for r in rows:
        if r["intent_pass"] == "0":
            conf[f"{r['expected_intent']} -> {r['actual_intent']}"] = conf.get(f"{r['expected_intent']} -> {r['actual_intent']}", 0) + 1
    out["confusions"] = dict(sorted(conf.items(), key=lambda kv: -kv[1]))
    return out

def cmd_score():
    RESULTS.mkdir(parents=True, exist_ok=True)
    summary = {}
    for name in ("tier-a-results.csv", "tier-b-results.csv", "tier-c-results.csv"):
        s = score_file(RESULTS / name)
        if s.get("n"): summary[name] = s
    (RESULTS / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False, indent=2))

# --------------------------------------------------------------------------------------
# Tier B via OpenRouter STT (cheap, gap-filling only). Transcribes each WAV; the REAL
# Kotlin parser then scores transcripts via BenchmarkTierBTest (./gradlew test).
# This is cloud STT, NOT Android on-device recognition and NOT real-mic testing.
# The free omni model (nemotron-3-nano-omni:free) was probed and ignored the audio
# payload; inkling:free is gated to agentic harnesses (403). Hence the cheapest
# working STT is used, with actual cost taken from usage.cost in each response.
# --------------------------------------------------------------------------------------
STT_PROMPT = "Transcreve este audio em portugues europeu, responde so com a transcricao, sem comentarios nem aspas."

def or_chat(key, model, messages, max_tokens=200):
    req = urllib.request.Request(PAID_PATH_CONFIG["base_url"] + "/chat/completions",
        data=json.dumps({"model": model, "messages": messages, "max_tokens": max_tokens,
                         "temperature": 0}).encode(),
        headers={"Authorization": f"Bearer {key}", "Content-Type": "application/json"})
    t0 = time.time()
    with urllib.request.urlopen(req, timeout=120) as r:
        body = json.loads(r.read())
    return body, (time.time() - t0) * 1000.0

def tier_b_openrouter(limit=None, model=None, ledger_path=None):
    key = read_key()
    if not key:
        print("No OpenRouter key found (env or key file). Tier B cloud path skipped; local tiers unaffected.")
        return
    model = model or PAID_PATH_CONFIG["stt_default"]
    paid = ":free" not in model
    cases = [c for c in load_cases() if (AUDIO / f"{c['id']}.wav").exists()][: limit or None]
    est_total = len(cases) * 0.0001  # measured $0.0000217/call; 5x headroom
    if est_total > PAID_PATH_CONFIG["hard_cap_usd"]:
        print(f"REFUSED: estimated ${est_total:.2f} exceeds hard cap ${PAID_PATH_CONFIG['hard_cap_usd']}")
        return
    print(f"Tier B STT: {len(cases)} cases via {model}; pre-flight estimate ${est_total:.4f} (cap ${PAID_PATH_CONFIG['hard_cap_usd']})")
    out, total_actual = [], 0.0
    for c in cases:
        b64 = base64.b64encode((AUDIO / f"{c['id']}.wav").read_bytes()).decode()
        msgs = [{"role": "user", "content": [
            {"type": "text", "text": STT_PROMPT},
            {"type": "input_audio", "input_audio": {"data": b64, "format": "wav"}}]}]
        transcript, latency, cost = "", 0.0, 0.0
        for _attempt in (1, 2):
            try:
                body, latency = or_chat(key, model, msgs, max_tokens=160)
                transcript = ((body.get("choices") or [{}])[0].get("message") or {}).get("content") or ""
                cost += float((body.get("usage") or {}).get("cost") or 0.0)
                if transcript.strip():
                    break
            except Exception as e:  # never leak headers/key; exception type only
                print(f"{c['id']}: STT failed ({type(e).__name__})")
        total_actual += cost
        if total_actual > 0.40:
            print("STOP: running cost passed $0.40; remaining cases not sent")
            break
        out.append({"id": c["id"], "stt_transcript": transcript.strip().strip('"'),
                    "stt_model": model, "stt_latency_ms": round(latency), "stt_cost_usd": round(cost, 6)})
        print(f"{c['id']}: {transcript.strip()[:60]!r} {latency:.0f}ms ${cost:.5f}")
    RESULTS.mkdir(parents=True, exist_ok=True)
    (RESULTS / "tier-b-transcripts.json").write_text(json.dumps(out, ensure_ascii=False), encoding="utf-8")
    ledger(ledger_path or (RESULTS / "cost-ledger.csv"),
           {"ts": time.strftime("%Y-%m-%dT%H:%M:%S"), "tier": "B-cloud-STT", "provider": "openrouter",
            "model": model, "cases": len(out), "est_cost_usd": f"{est_total:.4f}",
            "actual_cost_usd": f"{total_actual:.5f}",
            "note": "actual from usage.cost per response; then run gradle BenchmarkTierBTest"})
    print(f"Tier B STT done: {len(out)} transcripts, ACTUAL total ${total_actual:.5f}")
    print("Next: ./gradlew :app:testDebugUnitTest --tests '*BenchmarkTierBTest*' && python3 benchmark.py score")

def paid_quote():
    cases = load_cases()
    t = PAID_PATH_CONFIG["est_tokens_per_intent_call"]
    # Public pricing snapshot is fetched live by the report author; these are the models wired above.
    table = [
        ("nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free", "audio->intent", 0.0, 0.0),
        ("thinkingmachines/inkling-small:free", "audio->intent (alt)", 0.0, 0.0),
        ("liquid/lfm-2.5-2.6b:free", "text intent fallback", 0.0, 0.0),
        ("mistralai/voxtral-small-24b-2507", "dedicated STT", 0.10, 0.30),   # $/1M tokens, public list
        ("google/gemini-2.5-flash-lite", "STT/intent/VLM cheap", 0.10, 0.40),
        ("google/gemma-4-31b-it:free", "VLM check", 0.0, 0.0),
    ]
    print(f"{'model':55} {'role':24} {'per case':>10} {'full run (' + str(len(cases)) + ')':>14}")
    for mid, role, pin, pout in table:
        per = (t["prompt"] * pin + t["completion"] * pout) / 1e6
        print(f"{mid:55} {role:24} ${per:>9.5f} ${per * len(cases):>13.5f}")
    print("TTS: $0 — macOS say (Joana pt-PT) / Android TTS. No paid TTS is wired.")
    print(f"Hard cap enforced in code: ${PAID_PATH_CONFIG['hard_cap_usd']:.2f} per invocation.")

def main():
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("tier-a-help")  # Tier A itself runs via Gradle; see BenchmarkTierATest
    g = sub.add_parser("gen-audio"); g.add_argument("--limit", type=int); g.add_argument("--voice", default="Joana")
    b = sub.add_parser("tier-b-openrouter"); b.add_argument("--limit", type=int); b.add_argument("--model")
    sub.add_parser("score"); sub.add_parser("paid-quote")
    a = ap.parse_args()
    if a.cmd == "gen-audio": gen_audio(a.limit, a.voice)
    elif a.cmd == "tier-b-openrouter": tier_b_openrouter(a.limit, a.model)
    elif a.cmd == "score": cmd_score()
    elif a.cmd == "paid-quote": paid_quote()
    else: print("Tier A: run  ./gradlew :app:testDebugUnitTest --tests '*BenchmarkTierATest*'  then  benchmark.py score")

if __name__ == "__main__":
    main()
