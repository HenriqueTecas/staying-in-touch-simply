package com.dumpphone.voice

import com.dumpphone.data.normalizedForMatching
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tier A benchmark executor (transcript text -> intent parser).
 * Reads evals/voice_benchmark.json, runs [RuleBasedIntentParser] on every case
 * and writes a CSV to evals/results/tier-a-results.csv. This is a benchmark,
 * not a gate: the test only fails if the dataset cannot be loaded/run at all.
 * Scoring lives in evals/benchmark/benchmark.py.
 */
class BenchmarkTierATest {

    private fun strField(chunk: String, name: String): String? {
        val m = Regex(""""$name"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(chunk) ?: return null
        return m.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
    }

    private fun intField(chunk: String, name: String): Int? =
        Regex(""""$name"\s*:\s*(-?\d+)""").find(chunk)?.groupValues?.get(1)?.toIntOrNull()

    private fun intentName(r: ParsedIntent): String = when (r) {
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

    private fun csv(s: String): String = "\"" + s.replace("\"", "\"\"") + "\""

    @Test
    fun runTierABenchmark() {
        val candidates = listOf(
            File("../evals/voice_benchmark.json"),
            File("evals/voice_benchmark.json"),
            File((System.getProperty("dumpphone.evalsDir") ?: "") + "/voice_benchmark.json"),
        )
        val file = candidates.firstOrNull { it.isFile }
        assertTrue("voice_benchmark.json not found in $candidates", file != null)
        val text = file!!.readText(Charsets.UTF_8)
        val chunks = text.split("{\"id\":").drop(1)
        assertTrue("expected >=70 benchmark cases, got ${chunks.size}", chunks.size >= 70)

        val outDir = File(file.parentFile, "results").apply { mkdirs() }
        val rows = mutableListOf<String>()
        rows += listOf(
            "id", "task", "category", "transcript", "expected_intent", "actual_intent", "intent_pass",
            "expected_contact", "actual_contact", "contact_pass", "expected_message_contains", "actual_message",
            "message_pass", "expected_time", "actual_time", "time_pass", "expected_app", "actual_app", "app_pass",
            "latency_ms",
        ).joinToString(",") { csv(it) }

        var pass = 0
        for (chunk in chunks) {
            // The split consumed `"id":`, so the id is the first quoted token of each chunk.
            val id = Regex("""^\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1) ?: continue
            val transcript = strField(chunk, "transcript") ?: continue
            val task = strField(chunk, "task") ?: ""
            val category = strField(chunk, "category") ?: ""
            val expIntent = strField(chunk, "intent") ?: "?"
            val expContact = strField(chunk, "contactQuery")
            val expMsg = strField(chunk, "messageContains")
            val expHour = intField(chunk, "hour")
            val expMinute = intField(chunk, "minute")
            val expApp = strField(chunk, "app")

            val t0 = System.nanoTime()
            val r = RuleBasedIntentParser().parse(transcript)
            val latencyMs = (System.nanoTime() - t0) / 1_000_000.0

            val actual = intentName(r)
            val intentPass = actual == expIntent
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
            // Slot passes are only scored when an expectation exists; -1 = not applicable.
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
            val timePass = when {
                expTime == null -> -1
                actTime == expTime -> 1
                else -> 0
            }
            val appPass = when {
                expApp == null -> -1
                actApp == expApp -> 1
                else -> 0
            }
            val fullPass = intentPass && contactPass != 0 && messagePass != 0 && timePass != 0 && appPass != 0
            if (fullPass) pass++
            rows += listOf(
                csv(id), csv(task), csv(category), csv(transcript), csv(expIntent), csv(actual),
                csv(if (intentPass) "1" else "0"),
                csv(expContact ?: ""), csv(actContact ?: ""), csv(contactPass.toString()),
                csv(expMsg ?: ""), csv(actMsg ?: ""), csv(messagePass.toString()),
                csv(expTime ?: ""), csv(actTime ?: ""), csv(timePass.toString()),
                csv(expApp ?: ""), csv(actApp ?: ""), csv(appPass.toString()),
                csv("%.3f".format(latencyMs)),
            ).joinToString(",")
        }
        val out = File(outDir, "tier-a-results.csv")
        out.writeText(rows.joinToString("\n") + "\n", Charsets.UTF_8)
        println("TIER-A: $pass/${chunks.size} full-pass; results at ${out.absolutePath}")
    }
}
