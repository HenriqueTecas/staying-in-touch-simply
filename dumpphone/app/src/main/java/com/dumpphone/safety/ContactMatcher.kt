package com.dumpphone.safety

import com.dumpphone.data.Contact
import com.dumpphone.data.levenshtein
import com.dumpphone.data.normalizedForMatching

/**
 * Portuguese contact matching, including common diminutives/nicknames.
 * A result is [MatchResult.Ambiguous] when the two best candidates are too
 * close — the UI must then ask the user to choose (Phase 4). Never guess.
 */
object ContactMatcher {

    /** Well-known Portuguese nickname -> given-name expansions (normalized, no accents). */
    val nicknameExpansions: Map<String, List<String>> = mapOf(
        "ze" to listOf("jose"),
        "zeca" to listOf("jose"),
        "chico" to listOf("francisco"),
        "tonio" to listOf("antonio"),
        "toni" to listOf("antonio"),
        "nando" to listOf("fernando"),
        "beto" to listOf("alberto", "roberto"),
        "xana" to listOf("alexandra"),
        "sandra" to listOf("alexandra"), // distinct name, kept separate on purpose in tests
        "maria joao" to listOf("maria joao"),
        "mae" to listOf("mae"),
        "pai" to listOf("pai"),
        "avo" to listOf("avo", "avó"),
        "neto" to listOf("neto"),
        "ti" to listOf("tiago"),
        "gui" to listOf("guilherme"),
        "le" to listOf("leonor"),
        "ines" to listOf("ines"),
        "maggie" to listOf("margarida"),
        "guida" to listOf("margarida"),
        "rita" to listOf("margarida", "rita"),
        "joaozinho" to listOf("joao"),
        "manel" to listOf("manuel"),
        "nel" to listOf("manuel"),
        "quim" to listOf("joaquim"),
        "duarte" to listOf("duarte", "eduardo"),
        "duda" to listOf("eduarda", "maria eduarda"),
        "bia" to listOf("beatriz"),
        "tricia" to listOf("patricia"),
        "pat" to listOf("patricia"),
        "lú" to listOf("lucia", "luisa"),
        "lu" to listOf("lucia", "luisa"),
    )

    sealed interface MatchResult {
        data class Unique(val contact: Contact, val score: Double) : MatchResult
        data class Ambiguous(val candidates: List<Contact>) : MatchResult
        data object NotFound : MatchResult
    }

    fun score(queryRaw: String, contact: Contact): Double {
        val query = queryRaw.normalizedForMatching()
        if (query.isBlank()) return 0.0
        val name = contact.displayName.normalizedForMatching()
        val nameTokens = name.split(" ")
        val candidates = mutableListOf(name) + contact.nicknames.map { it.normalizedForMatching() }

        // Nickname expansion: "Zé" should match "José Santos".
        val expanded = nicknameExpansions[query].orEmpty()
        for (given in expanded) {
            if (nameTokens.firstOrNull() == given) return 0.92
        }
        for (nick in contact.nicknames) {
            val n = nick.normalizedForMatching()
            if (n == query) return 0.98
            if (nicknameExpansions[n]?.contains(nameTokens.firstOrNull()) == true && n == query) return 0.92
        }

        var best = 0.0
        for (candidate in candidates) {
            best = maxOf(best, when {
                candidate == query -> 1.0
                candidate.startsWith(query) || query.startsWith(candidate) -> 0.90
                candidate.split(" ").any { it == query } -> 0.88
                candidate.contains(query) || query.contains(candidate) -> 0.80
                else -> {
                    val dist = levenshtein(query, candidate)
                    val maxLen = maxOf(query.length, candidate.length).coerceAtLeast(1)
                    val ratio = 1.0 - dist.toDouble() / maxLen
                    // Also compare against first name only ("maria" vs "maria silva").
                    val firstNameRatio = 1.0 - levenshtein(query, nameTokens.first()).toDouble() /
                        maxOf(query.length, nameTokens.first().length).coerceAtLeast(1)
                    maxOf(if (ratio > 0.72) ratio * 0.85 else 0.0, if (firstNameRatio > 0.72) firstNameRatio * 0.85 else 0.0)
                }
            })
        }
        // First-name-only query against first name.
        if (nameTokens.firstOrNull() == query) best = maxOf(best, 0.88)
        return best
    }

    fun match(query: String, contacts: List<Contact>, threshold: Double = 0.60): MatchResult {
        val scored = contacts.map { it to score(query, it) }
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
        if (scored.isEmpty()) return MatchResult.NotFound
        val top = scored[0]
        val second = scored.getOrNull(1)
        // Ambiguous when a second candidate is nearly as good (e.g. Maria Silva vs Maria Santos).
        if (second != null && second.second >= top.second - 0.07 && second.first.id != top.first.id) {
            return MatchResult.Ambiguous(scored.take(4).map { it.first })
        }
        return MatchResult.Unique(top.first, top.second)
    }
}
