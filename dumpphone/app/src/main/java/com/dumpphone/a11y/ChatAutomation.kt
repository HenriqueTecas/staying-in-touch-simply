package com.dumpphone.a11y

import android.content.Context
import com.dumpphone.actions.Tier1Actions
import com.dumpphone.data.Contact
import com.dumpphone.data.DummyMode
import com.dumpphone.data.PtVariant
import com.dumpphone.voice.ChatApp

/**
 * Tier 2 chat flows (Phase 3). Precondition for every send: the Phase 4
 * read-back confirmation has already happened in the UI. This class never
 * confirms on its own and never sends on failure to find the chat.
 */
class ChatAutomation(private val context: Context) {
    private val tier1 = Tier1Actions(context)

    sealed interface Result {
        data object Done : Result
        data class NeedsAccessibility(val reasonPt: String) : Result
        data class Failed(val reasonPt: String) : Result
    }

    fun sendWhatsAppTier2(contact: Contact, message: String, variant: PtVariant): Result {
        if (DummyMode.ENABLED) return Result.Failed(DummyMode.HELD_MESSAGE)
        // Tier 1 first: wa.me pre-fills text; user still taps send in WhatsApp after our confirmation.
        val tier1Result = tier1.whatsApp(contact.phoneNumber, message, variant)
        if (tier1Result is Tier1Actions.LaunchResult.Launched) return Result.Done
        val service = DumpphoneAccessibilityService.instance
            ?: return Result.NeedsAccessibility("Ative o serviço de acessibilidade do Dumpphone nas Definições do telefone.")
        tier1.openApp(ChatApp.WHATSAPP.packageName)
        Thread.sleep(1200)
        if (!service.openChatByName(contact.displayName)) {
            return Result.Failed("Não encontrei a conversa com ${contact.displayName} no WhatsApp.")
        }
        Thread.sleep(800)
        return if (service.typeAndSend(message)) Result.Done
        else Result.Failed("A conversa abriu, mas não consegui enviar. A mensagem não foi enviada.")
    }

    fun sendMessengerTier2(contact: Contact, message: String): Result {
        if (DummyMode.ENABLED) return Result.Failed(DummyMode.HELD_MESSAGE)
        val service = DumpphoneAccessibilityService.instance
        if (service == null) {
            // Graceful Tier 1 fallback: open m.me chat; the user dictates/types there.
            val launched = tier1.messenger(contact.messengerUsername, message)
            return if (launched is Tier1Actions.LaunchResult.Launched)
                Result.Failed("Abri o Messenger. Escreva a mensagem na conversa — sem acessibilidade não consigo escrever por si.")
            else Result.NeedsAccessibility("Ative o serviço de acessibilidade do Dumpphone nas Definições do telefone.")
        }
        tier1.openApp(ChatApp.MESSENGER.packageName)
        Thread.sleep(1200)
        if (!service.openChatByName(contact.displayName)) {
            return Result.Failed("Não encontrei a conversa com ${contact.displayName} no Messenger.")
        }
        Thread.sleep(800)
        return if (service.typeAndSend(message)) Result.Done
        else Result.Failed("A conversa abriu, mas não consegui enviar. A mensagem não foi enviada.")
    }

    fun readLatestMessages(app: ChatApp): Result2 {
        if (DummyMode.ENABLED) return Result2.Failed(DummyMode.HELD_MESSAGE)
        val service = DumpphoneAccessibilityService.instance
            ?: return Result2.NeedsAccessibility("Ative o serviço de acessibilidade do Dumpphone para eu poder ler as mensagens.")
        tier1.openApp(app.packageName)
        Thread.sleep(1500)
        val messages = service.readLatestMessages(app)
        return if (messages.isEmpty()) Result2.Failed("Não consegui ler mensagens neste ecrã. Abra uma conversa e tente outra vez.")
        else Result2.Messages(messages)
    }

    sealed interface Result2 {
        data class Messages(val messages: List<String>) : Result2
        data class NeedsAccessibility(val reasonPt: String) : Result2
        data class Failed(val reasonPt: String) : Result2
    }
}
