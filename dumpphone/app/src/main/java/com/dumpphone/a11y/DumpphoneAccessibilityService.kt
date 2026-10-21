package com.dumpphone.a11y

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dumpphone.voice.ChatApp

/**
 * Tier 2 executor. The single AccessibilityService for the whole app:
 * opens named chats, types the (already confirmed) message, taps send, and
 * reads the latest on-screen messages so the launcher can speak them.
 *
 * Honest limits: WhatsApp/Messenger UI updates can break node matching.
 * Every flow returns a Boolean and the caller falls back to Tier 1 / asks
 * the user instead of tapping blindly. Nothing is sent from here without
 * the caller having run the Phase 4 confirmation first.
 */
class DumpphoneAccessibilityService : AccessibilityService(), UiActionExecutor {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance === this) instance = null
        return super.onUnbind(intent)
    }

    // --- UiActionExecutor ---
    override fun currentPackage(): String? = rootInActiveWindow?.packageName?.toString()

    override fun currentTree(): UiNodeSnapshot? = rootInActiveWindow?.let { toSnapshot(it) }

    override fun perform(action: UiAction): Boolean {
        val root = rootInActiveWindow ?: return false
        return when (action) {
            is UiAction.TapOnText -> findByText(root, action.text)?.let { click(it) } ?: false
            is UiAction.TapNode -> false // bounds-tap needs gesture dispatch; callers use TapOnText
            is UiAction.TypeText -> findEditable(root)?.let { node ->
                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text)
                }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            } ?: false
            UiAction.TapSend -> (findByText(root, "Enviar") ?: findByContentDescription(root, "Send")
                ?: findByContentDescription(root, "Enviar"))?.let { click(it) } ?: false
            UiAction.ScrollForward -> findScrollable(root)?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
            UiAction.Back -> performGlobalAction(GLOBAL_ACTION_BACK)
            UiAction.Home -> performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    // --- High-level flows used by the launcher ---

    /** Open the chat with [contactName] inside WhatsApp/Messenger. Best-effort, verified by tree text. */
    fun openChatByName(contactName: String): Boolean {
        val root = rootInActiveWindow ?: return false
        findByText(root, contactName)?.let { return click(it) }
        // Try the app search: tap search affordance, type the name, then tap the result.
        val search = findByContentDescription(root, "Pesquisar") ?: findByContentDescription(root, "Search")
            ?: findByText(root, "Pesquisar") ?: return false
        if (!click(search)) return false
        Thread.sleep(600)
        val root2 = rootInActiveWindow ?: return false
        val editable = findEditable(root2) ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, contactName)
        }
        if (!editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return false
        Thread.sleep(800)
        val root3 = rootInActiveWindow ?: return false
        return findByText(root3, contactName)?.let { click(it) } ?: false
    }

    fun typeAndSend(message: String): Boolean {
        if (!perform(UiAction.TypeText(message))) return false
        Thread.sleep(300)
        return perform(UiAction.TapSend)
    }

    fun readLatestMessages(app: ChatApp, max: Int = 5): List<String> {
        val tree = currentTree() ?: return emptyList()
        return UiTreeReader.extractLatestMessages(tree, max)
    }

    // --- Node helpers ---
    private fun toSnapshot(node: AccessibilityNodeInfo): UiNodeSnapshot {
        val rect = Rect().also { node.getBoundsInScreen(it) }
        return UiNodeSnapshot(
            text = node.text?.toString() ?: "",
            contentDescription = node.contentDescription?.toString() ?: "",
            className = node.className?.toString() ?: "",
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            bounds = rect.toShortString(),
            children = (0 until node.childCount).mapNotNull { node.getChild(it) }.map { toSnapshot(it) },
        )
    }

    private fun findByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val found = root.findAccessibilityNodeInfosByText(text)
        return found?.firstOrNull { it.isVisibleToUser }
    }

    private fun findByContentDescription(root: AccessibilityNodeInfo, desc: String): AccessibilityNodeInfo? {
        fun walk(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            if (node.contentDescription?.toString()?.contains(desc, ignoreCase = true) == true && node.isVisibleToUser) return node
            for (i in 0 until node.childCount) node.getChild(i)?.let { walk(it)?.let { r -> return r } }
            return null
        }
        return walk(root)
    }

    private fun findEditable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        fun walk(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            if (node.isEditable && node.isVisibleToUser) return node
            for (i in 0 until node.childCount) node.getChild(i)?.let { walk(it)?.let { r -> return r } }
            return null
        }
        return walk(root)
    }

    private fun findScrollable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        fun walk(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) node.getChild(i)?.let { walk(it)?.let { r -> return r } }
            return null
        }
        return walk(root)
    }

    private fun click(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            current = current.parent
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    companion object {
        @Volatile var instance: DumpphoneAccessibilityService? = null
            private set
        val isEnabled: Boolean get() = instance != null
    }
}
