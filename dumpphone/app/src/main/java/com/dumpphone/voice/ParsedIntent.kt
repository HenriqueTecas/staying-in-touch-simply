package com.dumpphone.voice

/** The ONLY tasks the voice parser may produce (Phase 2: constrained intent list). */
sealed interface ParsedIntent {
    data class CallContact(val contactQuery: String) : ParsedIntent
    data class SendSms(val contactQuery: String, val message: String) : ParsedIntent
    data class SendWhatsApp(val contactQuery: String, val message: String) : ParsedIntent
    data class SetAlarm(val hour: Int, val minute: Int, val label: String? = null) : ParsedIntent
    data class ReadMessages(val app: ChatApp) : ParsedIntent
    data class OpenApp(val app: ChatApp) : ParsedIntent
    data object Sos : ParsedIntent
    data object Cancel : ParsedIntent
    data object Help : ParsedIntent
    /** Messenger is not on this phone. Nothing is opened and nothing is sent. */
    data object MessengerRemoved : ParsedIntent
    /** Speech was heard but does not map to a known task. Router must ask, never guess. */
    data class Unknown(val transcript: String) : ParsedIntent
}

enum class ChatApp(val displayNamePt: String, val packageName: String) {
    WHATSAPP("WhatsApp", "com.whatsapp"),
    SMS("Mensagens", ""),
}

interface IntentParser {
    fun parse(transcript: String): ParsedIntent
}

/** Pluggable LLM parser (Phase 2). Implementations must return only [ParsedIntent] values. */
interface LlmIntentParser : IntentParser {
    val isConfigured: Boolean
}

/** Used when no LLM key is configured: honestly delegates to the rule-based parser. */
class UnconfiguredLlmIntentParser(private val fallback: IntentParser) : LlmIntentParser {
    override val isConfigured: Boolean = false
    override fun parse(transcript: String): ParsedIntent = fallback.parse(transcript)
}
