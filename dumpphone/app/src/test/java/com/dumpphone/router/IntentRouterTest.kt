package com.dumpphone.router

import com.dumpphone.voice.ChatApp
import com.dumpphone.voice.ParsedIntent
import org.junit.Assert.*
import org.junit.Test

class IntentRouterTest {
    @Test fun `chamada e tier1`() {
        val d = IntentRouter(vlmConsentGranted = false, accessibilityEnabled = false).route(ParsedIntent.CallContact("maria"))
        assertEquals(Tier.TIER1_INTENT, d.tier)
    }
    @Test fun `whatsapp com numero e tier1`() {
        val d = IntentRouter(false, false).route(ParsedIntent.SendWhatsApp("maria", "olá"), contactHasPhone = true)
        assertEquals(Tier.TIER1_INTENT, d.tier)
    }
    @Test fun `whatsapp sem numero e tier2`() {
        val d = IntentRouter(false, false).route(ParsedIntent.SendWhatsApp("maria", "olá"), contactHasPhone = false)
        assertEquals(Tier.TIER2_ACCESSIBILITY, d.tier)
        assertTrue(d.needsAccessibility)
    }
    @Test fun `ler mensagens e tier2`() {
        val d = IntentRouter(false, false).route(ParsedIntent.ReadMessages(ChatApp.WHATSAPP))
        assertEquals(Tier.TIER2_ACCESSIBILITY, d.tier)
    }
    @Test fun `desconhecido sem consentimento nao vai para vlm`() {
        val d = IntentRouter(vlmConsentGranted = false, accessibilityEnabled = false).route(ParsedIntent.Unknown("tempo?"))
        assertEquals(Tier.TIER1_INTENT, d.tier)
    }
    @Test fun `desconhecido com consentimento pode ir para vlm`() {
        val d = IntentRouter(vlmConsentGranted = true, accessibilityEnabled = true).route(ParsedIntent.Unknown("tempo?"))
        assertEquals(Tier.TIER3_VLM, d.tier)
    }
}
