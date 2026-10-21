package com.dumpphone.router

import com.dumpphone.voice.ChatApp
import com.dumpphone.voice.ParsedIntent

enum class Tier { TIER1_INTENT, TIER2_ACCESSIBILITY, TIER3_VLM }

data class RouteDecision(
    val tier: Tier,
    val reasonPt: String,
    /** When true, the executor needs the AccessibilityService; UI must prompt to enable it if off. */
    val needsAccessibility: Boolean = false,
)

/**
 * Phase 2/3/5 router — intent-first, accessibility second, VLM last resort.
 * Pure decision logic (no Android calls) so routing is unit-testable.
 */
class IntentRouter(private val vlmConsentGranted: Boolean, private val accessibilityEnabled: Boolean) {

    fun route(intent: ParsedIntent, contactHasPhone: Boolean = true): RouteDecision = when (intent) {
        is ParsedIntent.CallContact, is ParsedIntent.SendSms, is ParsedIntent.SetAlarm,
        is ParsedIntent.OpenApp, is ParsedIntent.Sos ->
            RouteDecision(Tier.TIER1_INTENT, "Resolvido com uma ação direta do telefone.")

        is ParsedIntent.SendWhatsApp ->
            if (contactHasPhone) RouteDecision(Tier.TIER1_INTENT, "WhatsApp aberto com a conversa e o texto já escritos (wa.me).")
            else RouteDecision(Tier.TIER2_ACCESSIBILITY, "Sem número: abrir a conversa pelo nome no WhatsApp.", needsAccessibility = true)

        is ParsedIntent.SendMessenger ->
            // m.me cannot pre-fill text, so typing the confirmed message needs Tier 2.
            RouteDecision(Tier.TIER2_ACCESSIBILITY, "Abrir a conversa no Messenger e escrever o texto confirmado.", needsAccessibility = true)

        is ParsedIntent.ReadMessages ->
            RouteDecision(Tier.TIER2_ACCESSIBILITY, "Ler as mensagens no ecrã e lê-las em voz alta.", needsAccessibility = true)

        is ParsedIntent.Unknown ->
            if (vlmConsentGranted) RouteDecision(Tier.TIER3_VLM, "Não percebi o pedido; só com a sua autorização uso a ajuda visual.", needsAccessibility = true)
            else RouteDecision(Tier.TIER1_INTENT, "Pedido não reconhecido: perguntar outra vez, sem adivinhar.")

        is ParsedIntent.Cancel, is ParsedIntent.Help ->
            RouteDecision(Tier.TIER1_INTENT, "Sem ação externa.")
    }

    companion object {
        fun describeApp(app: ChatApp): String = app.displayNamePt
    }
}
