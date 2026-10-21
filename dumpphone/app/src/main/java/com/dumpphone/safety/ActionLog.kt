package com.dumpphone.safety

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** One plain-language log entry, readable by a family member. No message bodies beyond what the user dictated. */
data class ActionLogEntry(val epochMillis: Long, val textPt: String) {
    fun formatted(): String {
        val fmt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "PT"))
        return "${fmt.format(Date(epochMillis))} — $textPt"
    }
    fun serialize(): String = "$epochMillis\t${textPt.replace("\t", " ").replace("\n", " ")}"
    companion object {
        fun deserialize(line: String): ActionLogEntry? {
            val parts = line.split("\t", limit = 2)
            if (parts.size != 2) return null
            val ts = parts[0].toLongOrNull() ?: return null
            return ActionLogEntry(ts, parts[1])
        }
    }
}

/** Bounded in-memory view; persistence is via [com.dumpphone.data.SettingsStore]. */
class ActionLog(private val maxEntries: Int = 200) {
    private val entries = ArrayDeque<ActionLogEntry>()
    fun add(textPt: String, nowMillis: Long = System.currentTimeMillis()) {
        entries.addLast(ActionLogEntry(nowMillis, textPt))
        while (entries.size > maxEntries) entries.removeFirst()
    }
    fun all(): List<ActionLogEntry> = entries.toList().asReversed()
    fun load(serialized: List<String>) {
        entries.clear()
        serialized.mapNotNull { ActionLogEntry.deserialize(it) }.takeLast(maxEntries).forEach { entries.addLast(it) }
    }
    fun serialized(): List<String> = entries.map { it.serialize() }
}
