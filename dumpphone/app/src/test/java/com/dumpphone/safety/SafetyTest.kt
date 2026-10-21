package com.dumpphone.safety

import com.dumpphone.voice.ParsedIntent
import org.junit.Assert.*
import org.junit.Test

class SafetyTest {
    @Test fun `enviar whatsapp exige confirmacao`() {
        assertTrue(ConfirmationPolicy.requiresConfirmation(ParsedIntent.SendWhatsApp("maria", "olá"), false))
    }
    @Test fun `chamada exige confirmacao`() {
        assertTrue(ConfirmationPolicy.requiresConfirmation(ParsedIntent.CallContact("maria"), false))
    }
    @Test fun `sos nao espera confirmacao`() {
        assertFalse(ConfirmationPolicy.requiresConfirmation(ParsedIntent.Sos, false))
    }
    @Test fun `prompt de confirmacao em portugues`() {
        val p = ConfirmationPolicy.confirmationPromptPt(ParsedIntent.SendWhatsApp("maria", "Já cheguei"), "Maria Silva")
        assertEquals("Quer enviar \"Já cheguei\" por WhatsApp para a Maria Silva?", p)
    }
    @Test fun `pin hash verifica e rejeita`() {
        val salt = PinHasher.newSaltHex()
        val hash = PinHasher.hash("2580", salt)
        assertTrue(PinHasher.verify("2580", salt, hash))
        assertFalse(PinHasher.verify("1234", salt, hash))
    }
    @Test fun `formato de pin`() {
        assertTrue(PinHasher.isValidPinFormat("1234"))
        assertFalse(PinHasher.isValidPinFormat("123"))
        assertFalse(PinHasher.isValidPinFormat("12a4"))
    }
    @Test fun `action log roundtrip`() {
        val log = ActionLog()
        log.add("Ligou para a Maria Silva.", 1_700_000_000_000)
        val restored = ActionLog()
        restored.load(log.serialized())
        assertEquals(1, restored.all().size)
        assertTrue(restored.all()[0].formatted().contains("Ligou para a Maria"))
    }
}
