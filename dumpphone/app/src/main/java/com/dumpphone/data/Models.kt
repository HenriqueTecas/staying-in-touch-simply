package com.dumpphone.data

import java.text.Normalizer

/** Portuguese variant. Default is European Portuguese (locked decision, Phase 0). */
enum class PtVariant(val localeTag: String, val displayName: String) {
    PT_PT("pt-PT", "Português (Portugal)"),
    PT_BR("pt-BR", "Português (Brasil)");

    companion object {
        fun fromTag(tag: String?): PtVariant = entries.firstOrNull { it.localeTag == tag } ?: PT_PT
    }
}

data class Contact(
    val id: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val photoUri: String? = null,
    val nicknames: List<String> = emptyList(),
    val isFavourite: Boolean = false,
    /** Shown on the mock screens. Never passed to the dialler. */
    val displayPhone: String? = null,
    /** Short relationship label, e.g. "Filha". */
    val relation: String? = null,
)

fun String.normalizedForMatching(): String {
    val decomposed = Normalizer.normalize(this.lowercase(), Normalizer.Form.NFD)
    return decomposed.replace(Regex("\\p{Mn}+"), "")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    var prev = IntArray(b.length + 1) { it }
    var curr = IntArray(b.length + 1)
    for (i in 1..a.length) {
        curr[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
        }
        val tmp = prev; prev = curr; curr = tmp
    }
    return prev[b.length]
}
