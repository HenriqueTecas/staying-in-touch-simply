package com.dumpphone.voice

import com.dumpphone.data.normalizedForMatching

/**
 * Rule-based Portuguese intent parser (Phase 2, ships first; LLM is pluggable
 * behind [IntentParser]). Constrained to the fixed launch-task list.
 * Handles pt-PT and pt-BR phrasing ("ligar para", "telefonar", "despertador",
 * "alarme", "põe um despertador", "acorda-me às...").
 */
class RuleBasedIntentParser : IntentParser {

    override fun parse(transcript: String): ParsedIntent {
        val raw = transcript.trim()
        // "Maria: estou bem" — the colon is a message separator, not part of the name.
        // Digit colons ("8:15") are left alone so clock times survive.
        val prepared = raw.replace(Regex("""(?<=\D):\s*"""), " a dizer ")
        // Common STT spellings normalised before matching (benchmark cases W04/S07/M06).
        val t = prepared.normalizedForMatching()
            .replace("esse eme esse", "sms")
            .replace("uatsap", "whatsapp").replace("whatsap", "whatsapp")
            .replace("messenjer", "messenger").replace("messeger", "messenger")
        if (t.isBlank()) return ParsedIntent.Unknown(raw)

        if (t in setOf("cancelar", "cancela", "para", "parar", "não", "nao", "esquece") ||
            t.contains("cancela") || t.startsWith("esquece") || t.contains("nao facas nada") ||
            (t.startsWith("para ") && t.contains("nao"))
        ) return ParsedIntent.Cancel
        if (t.contains("socorro") || t.contains("emergencia") || t.contains("ajuda me") ||
            t.contains("preciso de ajuda") || t.contains("ambulancia")
        ) {
            return ParsedIntent.Sos
        }
        if (t == "ajuda" || t.startsWith("o que podes") || t.startsWith("o que pode")) return ParsedIntent.Help

        // --- Read messages aloud ---
        val mentionsWhatsApp = t.contains("whatsapp")
        val mentionsMessenger = t.contains("messenger") || t.contains("facebook")
        if (((t.contains("ler") || t.contains("le ") || t.contains("ouvir") || t.contains("novas mensagens")) &&
                (t.contains("mensag") || mentionsWhatsApp || mentionsMessenger)) ||
            (t.contains("mandou") && !t.contains("dizer") && !t.contains("manda") &&
                (mentionsWhatsApp || mentionsMessenger))
        ) {
            return ParsedIntent.ReadMessages(
                when { mentionsMessenger -> ChatApp.MESSENGER; mentionsWhatsApp -> ChatApp.WHATSAPP; else -> ChatApp.WHATSAPP }
            )
        }

        // Polite/request prefixes a recogniser keeps: strip so verb checks below see the verb.
        var v = t
        for (prefix in listOf("por favor ", "se faz favor ", "podes ", "poderia ", "podia ", "queria ", "quero ")) {
            if (v.startsWith(prefix)) { v = v.removePrefix(prefix); break }
        }
        v = v.removeSuffix(" por favor").removeSuffix(" se faz favor")

        // "Quero falar com a Maria (no WhatsApp)": open the chat, or call when no app is named.
        if (v.contains("falar com")) {
            if (mentionsWhatsApp) return ParsedIntent.OpenApp(ChatApp.WHATSAPP)
            if (mentionsMessenger) return ParsedIntent.OpenApp(ChatApp.MESSENGER)
            val after = v.substringAfter("falar com ").removePrefix("a ").removePrefix("o ")
                .removePrefix("minha filha ").removePrefix("meu filho ")
                .removePrefix("minha mae ").removePrefix("meu pai ")
                .removePrefix("minha ").removePrefix("meu ")
                .substringBefore(" no ").substringBefore(" pelo ").trim()
            if (after.isNotBlank()) return ParsedIntent.CallContact(after)
        }

        // --- Alarm (incl. reminder phrasing "lembra-me às...", set as an alarm rather than guessed) ---
        if (v.contains("despertador") || v.contains("alarme") || v.contains("acorda") || v.contains("acordar") ||
            v.contains("lembra me") || v.startsWith("lembra")
        ) {
            val time = parseTime(v)
            if (time != null) return ParsedIntent.SetAlarm(time.first, time.second, extractAlarmLabel(raw))
            if (v.contains("despertador") || v.contains("alarme") || v.contains("acorda")) return ParsedIntent.Unknown(raw)
        }

        // --- Send messages (check before plain calls: they also contain "para") ---
        parseSend(v, raw, ChatApp.WHATSAPP)?.let { return it }
        parseSend(v, raw, ChatApp.MESSENGER)?.let { return it }
        if (v.contains("sms") || v.startsWith("mensagem para") || v.startsWith("manda mensagem") ||
            v.startsWith("envia mensagem") || v.startsWith("mandar mensagem") || v.startsWith("enviar mensagem") ||
            (v.contains("mensagem") && !mentionsWhatsApp && !mentionsMessenger &&
                listOf("manda", "envia", "mandar", "enviar", "escreve").any { v.contains(it) })
        ) {
            parseSend(v, raw, ChatApp.SMS)?.let { return it }
        }

        // --- Call ---
        if (v.startsWith("ligar") || v.startsWith("liga ") || v.startsWith("ligue") || v.startsWith("telefonar") ||
            v.startsWith("telefone") || v.startsWith("chamar") || v.startsWith("chama ") ||
            v.startsWith("fazer uma chamada") || v.startsWith("faz uma chamada") || v.startsWith("faca uma chamada")
        ) {
            val contact = extractContactAfterPara(v)
            if (!contact.isNullOrBlank()) return ParsedIntent.CallContact(contact)
            return ParsedIntent.Unknown(raw)
        }

        // --- Open app ---
        if ((t.startsWith("abrir") || t.startsWith("abre") || t.startsWith("abrir o")) && mentionsWhatsApp) {
            return ParsedIntent.OpenApp(ChatApp.WHATSAPP)
        }
        if ((t.startsWith("abrir") || t.startsWith("abre")) && mentionsMessenger) return ParsedIntent.OpenApp(ChatApp.MESSENGER)

        return ParsedIntent.Unknown(raw)
    }

    private fun parseSend(t: String, raw: String, app: ChatApp): ParsedIntent? {
        val isForApp = when (app) {
            ChatApp.WHATSAPP -> t.contains("whatsapp")
            ChatApp.MESSENGER -> t.contains("messenger") || t.contains("facebook")
            ChatApp.SMS -> true
        }
        if (!isForApp) return null
        val sendVerb = listOf("enviar", "envia", "mande", "mandar", "manda", "dizer", "diz").any { t.contains(it) } ||
            t.contains("mensagem")
        if (!sendVerb) return null

        val contact = extractContactAfterPara(t) ?: return null
        val message = extractMessage(raw, t)
        if (message.isBlank()) {
            // "manda um WhatsApp à Maria" with no body: caller prompts for dictation next.
            return when (app) {
                ChatApp.WHATSAPP -> ParsedIntent.SendWhatsApp(contact, "")
                ChatApp.MESSENGER -> ParsedIntent.SendMessenger(contact, "")
                ChatApp.SMS -> ParsedIntent.SendSms(contact, "")
            }
        }
        return when (app) {
            ChatApp.WHATSAPP -> ParsedIntent.SendWhatsApp(contact, message)
            ChatApp.MESSENGER -> ParsedIntent.SendMessenger(contact, message)
            ChatApp.SMS -> ParsedIntent.SendSms(contact, message)
        }
    }

    /**
     * Contact is the words after para/a/à/ao, stopping at dizer/a dizer/mensagem/por/no/na.
     * Works on the normalized transcript, so the returned query is accent-free;
     * [com.dumpphone.safety.ContactMatcher] normalizes the same way.
     */
    internal fun extractContactAfterPara(t: String): String? {
        val markers = listOf(" para a ", " para o ", " para ", " a ", " à ", " ao ")
        var start = -1
        var markerLen = 0
        // Prefer "para" forms over bare " a ".
        for (m in listOf(" para a ", " para o ", " para ")) {
            val idx = t.indexOf(m)
            if (idx >= 0) { start = idx + m.length; markerLen = m.length; break }
        }
        if (start < 0) {
            // "liga à Maria" normalized drops accents, so looks for " a " after verb.
            val idx = t.indexOf(" a ")
            if (idx >= 0) { start = idx + 3 }
        }
        if (start < 0) return null
        var rest = t.substring(start).trim()
        // Hesitant speech repeats the marker: "para a... para a Maria" — strip repetitions.
        while (rest.startsWith("para a ") || rest.startsWith("para o ") || rest.startsWith("para ")) {
            rest = rest.removePrefix("para a ").removePrefix("para o ").removePrefix("para ")
        }
        val stopWords = listOf(" dizer", " a dizer", " dizer-lhe", " mensagem", " por whatsapp", " por messenger", " no whatsapp", " no messenger", " pelo whatsapp", " pelo messenger", " pelo facebook")
        val earliestStop = stopWords.mapNotNull { stop ->
            rest.indexOf(stop).takeIf { it >= 0 }?.let { it to stop }
        }.minByOrNull { it.first }
        if (earliestStop != null) rest = rest.substring(0, earliestStop.first).trim()
        // Strip leading articles left over ("a maria" from "para a maria" handled above).
        rest = rest.removePrefix("a ").removePrefix("o ")
            .removeSuffix(" por favor").removeSuffix(" se faz favor").trim()
        return rest.ifBlank { null }
    }

    /** Message body is what follows "dizer"/"diz"/":" in the ORIGINAL transcript (keeps accents/punctuation). */
    internal fun extractMessage(raw: String, normalized: String): String {
        val lower = raw.lowercase()
        for (marker in listOf("a dizer", "dizer", "diz que", "diz", "diga", ":")) {
            val idx = lower.indexOf(marker)
            if (idx >= 0) {
                val msg = raw.substring(idx + marker.length).trim().trim('"', '\'', '«', '»')
                if (msg.isNotBlank()) return msg
            }
        }
        return ""
    }

    private fun extractAlarmLabel(raw: String): String? {
        val lower = raw.lowercase()
        val idx = lower.indexOf("para ")
        // Only treat trailing text as label if it is not a time expression; keep it simple.
        return null
    }

    companion object {
        private val numberWords = mapOf(
            "zero" to 0, "um" to 1, "uma" to 1, "dois" to 2, "duas" to 2, "tres" to 3, "quatro" to 4,
            "cinco" to 5, "seis" to 6, "sete" to 7, "oito" to 8, "nove" to 9, "dez" to 10,
            "onze" to 11, "doze" to 12, "treze" to 13, "catorze" to 14, "quinze" to 15,
            "dezasseis" to 16, "dezesseis" to 16, "dezassete" to 17, "dezessete" to 17,
            "dezoito" to 18, "dezanove" to 19, "dezenove" to 19, "vinte" to 20,
            "trinta" to 30, "quarenta" to 40, "cinquenta" to 50,
        )

        /** Parses "às sete e meia", "as 7:30", "às oito", "sete e um quarto", "vinte e duas e dez". */
        fun parseTime(normalized: String): Pair<Int, Int>? {
            // Digital HH:MM first.
            Regex("""(\d{1,2})[:h](\d{2})""").find(normalized)?.let {
                val h = it.groupValues[1].toInt(); val m = it.groupValues[2].toInt()
                if (h in 0..23 && m in 0..59) return h to m
            }
            Regex("""\b(\d{1,2})\s*(?:horas\b|h\b)?\s*$""").find(normalized)?.let {
                // bare trailing number only if no word-time found below
            }
            val tokens = normalized.split(" ").filter { it.isNotBlank() }
            // Prefer the number after the time marker "as" (às): a leading "um alarme"
            // is an article, not 01:00 (benchmark case A05).
            val scanStart = tokens.lastIndexOf("as").let { if (it >= 0) it + 1 else 0 }
            var hour: Int? = null
            var hourIndex = -1
            for (i in scanStart until tokens.size) {
                val tok = tokens[i]
                val v = tok.toIntOrNull() ?: numberWords[tok]
                if (v != null && v in 0..23) { hour = v; hourIndex = i; break }
                if (tok == "meia" && normalized.contains("noite")) { hour = 0; hourIndex = i; break } // meia-noite
                if (tok == "meio" && normalized.contains("dia")) { hour = 12; hourIndex = i; break } // meio-dia
            }
            if (hour == null) { // fall back to a full scan (e.g. bare "sete e meia" with no marker)
                for ((i, tok) in tokens.withIndex()) {
                    val v = tok.toIntOrNull() ?: numberWords[tok]
                    if (v != null && v in 0..23) { hour = v; hourIndex = i; break }
                    if (tok == "meia" && normalized.contains("noite")) { hour = 0; hourIndex = i; break }
                    if (tok == "meio" && normalized.contains("dia")) { hour = 12; hourIndex = i; break }
                }
            }
            if (hour == null) return null
            // "sete menos um quarto" = 06:45.
            if (normalized.contains("menos")) {
                return ((hour + 23) % 24) to if (normalized.contains("quarto")) 45 else 50
            }
            var minute = 0
            val rest = tokens.drop(hourIndex + 1).joinToString(" ")
            when {
                rest.contains("meia") -> minute = 30
                rest.contains("quarto") -> minute = 15
                else -> {
                    // "e trinta e cinco" / "e dez": composite minutes add tens + units.
                    val after = tokens.drop(hourIndex + 1).filter { it != "e" && it != "horas" && it != "hora" && it != "da" && it != "manha" && it != "tarde" && it != "noite" }
                    val nums = after.mapNotNull { tok -> (tok.toIntOrNull() ?: numberWords[tok])?.takeIf { it in 0..59 } }
                    if (nums.isNotEmpty()) {
                        minute = nums[0]
                        if (minute % 10 == 0 && minute >= 20 && nums.size > 1 && nums[1] in 1..9) minute += nums[1]
                    }
                }
            }
            // "vinte e duas" = 22: hour word was "vinte", next token adds units.
            if (hourIndex >= 0 && hour in listOf(20) && hourIndex + 2 < tokens.size) {
                // handled naturally because tokens scan found 20 first; check "e dois" pattern for tens.
            }
            // Composite tens: "vinte e dois" -> scan found "vinte", minute logic would take 2 as minute.
            // Fix: if hour is a multiple of ten and next number < 10, it is part of the hour.
            if (hour % 10 == 0 && hour >= 20) {
                val after = tokens.drop(hourIndex + 1).filter { it != "e" }
                val unit = after.firstOrNull()?.let { it.toIntOrNull() ?: numberWords[it] }
                if (unit != null && unit in 1..9) {
                    // Ambiguous with minutes; Portuguese "vinte e duas e dez" has a second "e".
                    val remaining = tokens.drop(hourIndex + 1).count { it == "e" }
                    if (remaining >= 2 || after.size == 1) {
                        hour += unit
                        // recompute minute from tokens after the unit
                        val afterUnit = after.drop(1).filter { it != "e" }
                        minute = 0
                        if (afterUnit.any { it == "meia" }) minute = 30
                        else if (afterUnit.any { it == "quarto" }) minute = 15
                        else afterUnit.firstOrNull()?.let { tok ->
                            val v = tok.toIntOrNull() ?: numberWords[tok]
                            if (v != null && v in 0..59) minute = v
                        }
                    }
                }
            }
            return hour to minute
        }
    }
}
