package com.dumpphone.a11y

/** Minimal, serialisable view of an accessibility node — pure logic, unit-testable without a device. */
data class UiNodeSnapshot(
    val text: String = "",
    val contentDescription: String = "",
    val className: String = "",
    val isClickable: Boolean = false,
    val isEditable: Boolean = false,
    val bounds: String = "",
    val children: List<UiNodeSnapshot> = emptyList(),
) {
    fun flatten(): List<UiNodeSnapshot> = listOf(this) + children.flatMap { it.flatten() }
    fun visibleTexts(): List<String> = flatten().map { it.text.ifBlank { it.contentDescription } }.filter { it.isNotBlank() }
}

object UiTreeReader {
    /** Latest readable message-like lines: long-enough text nodes, last ones first (chat order). */
    fun extractLatestMessages(root: UiNodeSnapshot, max: Int = 5): List<String> =
        root.flatten()
            .map { it.text.trim() }
            .filter { it.length >= 2 }
            // Filter out obvious UI chrome in PT/EN WhatsApp & Messenger.
            .filterNot { it.lowercase() in CHROME_WORDS }
            .takeLast(max)
            .asReversed()

    fun findNodeByText(root: UiNodeSnapshot, query: String): UiNodeSnapshot? {
        val q = query.lowercase()
        return root.flatten().firstOrNull {
            it.text.lowercase().contains(q) || it.contentDescription.lowercase().contains(q)
        }
    }

    fun findEditable(root: UiNodeSnapshot): UiNodeSnapshot? = root.flatten().firstOrNull { it.isEditable }

    private val CHROME_WORDS = setOf(
        "whatsapp", "messenger", "conversas", "chats", "escrever uma mensagem", "type a message",
        "online", "visto", "seen", "enviar", "send", "pesquisar", "search",
    )
}

/** The small action vocabulary Tiers 2–3 may execute. One executor, one audit trail. */
sealed interface UiAction {
    data class TapOnText(val text: String) : UiAction
    data class TapNode(val bounds: String) : UiAction
    data class TypeText(val text: String) : UiAction
    data object TapSend : UiAction
    data object ScrollForward : UiAction
    data object Back : UiAction
    data object Home : UiAction
}

interface UiActionExecutor {
    fun perform(action: UiAction): Boolean
    fun currentTree(): UiNodeSnapshot?
    fun currentPackage(): String?
}
