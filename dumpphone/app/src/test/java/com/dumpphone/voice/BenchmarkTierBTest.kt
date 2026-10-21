package com.dumpphone.voice

import com.dumpphone.data.normalizedForMatching
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tier B executor: STT transcripts (evals/results/tier-b-transcripts.json, produced
 * by whichever STT ran — on-device via adb or a cloud STT) -> the real
 * [RuleBasedIntentParser] -> evals/results/tier-b-results.csv. Same scoring columns
 * as Tier A so benchmark.py can score both identically. If no transcripts file
 * exists the test is a no-op pass (Tier B is optional/local-first).
 */
class BenchmarkTierBTest {

    private fun strField(chunk: String, name: String): String? {
        val m = Regex(""""$name"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(chunk) ?: return null
        return m.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", " ")
    }

    private fun intField(chunk: String, name: String): Int? =
        Regex(""""$name"\s*:\s*(-?\d+)""").find(chunk)?.groupValues?.get(1)?.toIntOrNull()

    private fun firstQuoted(chunk: String): String? =
        Regex("""^\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1)

    private fun csv(s: String): String = "\"" + s.replace("\"", "\"\"") + "\""

    @Test
    fun runTierBBenchmark() {
        val evalsDir = listOf(File("../evals"), File("evals")).firstOrNull { it.isDirectory } ?: return
        val transcriptsFile = File(evalsDir, "results/tier-b-transcripts.json")
        if (!transcriptsFile.isFile) {
            println("TIER-B: no transcripts file, skipping (no-op)")
            return
        }
        val benchFile = File(evalsDir, "voice_benchmark.json")
        assertTrue(benchFile.isFile)
        // expectations by id
        val expById = mutableMapOf<String, String>()
        for (chunk in benchFile.readText(Charsets.UTF_8).split("{\"id\":").drop(1)) {
            val id = firstQuoted(chunk) ?: continue
            expById[id] = chunk
        }
        val rows = mutableListOf<String>()
        rows += listOf(
            "id", "task", "category", "transcript", "expected_intent", "actual_intent", "intent_pass",
            "expected_contact", "actual_contact", "contact_pass", "expected_message_contains", "actual_message",
            "message_pass", "expected_time", "actual_time", "time_pass", "expected_app", "actual_app", "app_pass",
            "latency_ms",
        ).joinToString(",") { csv(it) }

        var n = 0
        for (chunk in transcriptsFile.readText(Charsets.UTF_8).split("{\"id\":").drop(1)) {
            val id = firstQuoted(chunk) ?: continue
            val transcript = strField(chunk, "stt_transcript") ?: continue
            val expChunk = expById[id] ?: continue
            n++
            val task = strField(expChunk, "task") ?: ""
            val category = strField(expChunk, "category") ?: ""
            val expIntent = strField(expChunk, "intent") ?: "?"
            val expContact = strField(expChunk, "contactQuery")
            val expMsg = strField(expChunk, "messageContains")
            val expHour = intField(expChunk, "hour")
            val expMinute = intField(expChunk, "minute")
            val expApp = strField(expChunk, "app")

            val t0 = System.nanoTime()
            val r = RuleBasedIntentParser().parse(transcript)
            val latencyMs = (System.nanoTime() - t0) / 1_000_000.0
            val actual = when (r) {
                is ParsedIntent.CallContact -> "CallContact"
                is ParsedIntent.SendSms -> "SendSms"
                is ParsedIntent.SendWhatsApp -> "SendWhatsApp"
                is ParsedIntent.SendMessenger -> "SendMessenger"
                is ParsedIntent.SetAlarm -> "SetAlarm"
                is ParsedIntent.ReadMessages -> "ReadMessages"
                is ParsedIntent.OpenApp -> "OpenApp"
                is ParsedIntent.Sos -> "Sos"
                is ParsedIntent.Cancel -> "Cancel"
                is ParsedIntent.Help -> "Help"
                is ParsedIntent.Unknown -> "Unknown"
            }
            val actContact = when (r) {
                is ParsedIntent.CallContact -> r.contactQuery
                is ParsedIntent.SendSms -> r.contactQuery
                is ParsedIntent.SendWhatsApp -> r.contactQuery
                is ParsedIntent.SendMessenger -> r.contactQuery
                else -> null
            }
            val actMsg = when (r) {
                is ParsedIntent.SendSms -> r.message
                is ParsedIntent.SendWhatsApp -> r.message
                is ParsedIntent.SendMessenger -> r.message
                else -> null
            }
            val actTime = if (r is ParsedIntent.SetAlarm) "%02d:%02d".format(r.hour, r.minute) else null
            val expTime = if (expHour != null) "%02d:%02d".format(expHour, expMinute ?: 0) else null
            val actApp = when (r) {
                is ParsedIntent.ReadMessages -> r.app.name
                is ParsedIntent.OpenApp -> r.app.name
                else -> null
            }
            val contactPass = when {
                expContact == null -> -1
                actContact == null -> 0
                actContact.normalizedForMatching() == expContact.normalizedForMatching() -> 1
                else -> 0
            }
            val messagePass = when {
                expMsg == null -> -1
                expMsg.isEmpty() -> if (actMsg != null && actMsg.isEmpty()) 1 else 0
                actMsg == null -> 0
                actMsg.normalizedForMatching().contains(expMsg.normalizedForMatching()) -> 1
                else -> 0
            }
            val timePass = if (expTime == null) -1 else if (actTime == expTime) 1 else 0
            val appPass = if (expApp == null) -1 else if (actApp == expApp) 1 else 0
            rows += listOf(
                csv(id), csv(task), csv(category), csv(transcript), csv(expIntent), csv(actual),
                csv(if (actual == expIntent) "1" else "0"),
                csv(expContact ?: ""), csv(actContact ?: ""), csv(contactPass.toString()),
                csv(expMsg ?: ""), csv(actMsg ?: ""), csv(messagePass.toString()),
                csv(expTime ?: ""), csv(actTime ?: ""), csv(timePass.toString()),
                csv(expApp ?: ""), csv(actApp ?: ""), csv(appPass.toString()),
                csv("%.3f".format(latencyMs)),
            ).joinToString(",")
        }
        val out = File(evalsDir, "results/tier-b-results.csv")
        out.writeText(rows.joinToString("\n") + "\n", Charsets.UTF_8)
        println("TIER-B: scored $n transcripts; results at ${out.absolutePath}")
    }
}
