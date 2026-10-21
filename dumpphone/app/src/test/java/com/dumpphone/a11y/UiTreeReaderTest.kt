package com.dumpphone.a11y

import org.junit.Assert.*
import org.junit.Test

class UiTreeReaderTest {
    private val tree = UiNodeSnapshot(text = "WhatsApp", children = listOf(
        UiNodeSnapshot(text = "Maria Silva"),
        UiNodeSnapshot(text = "Olá, já chegaste?"),
        UiNodeSnapshot(text = "Sim, estou em casa"),
        UiNodeSnapshot(text = "Escrever uma mensagem", isEditable = true),
    ))

    @Test fun `extrai ultimas mensagens sem chrome`() {
        val msgs = UiTreeReader.extractLatestMessages(tree, 5)
        assertTrue(msgs.contains("Olá, já chegaste?"))
        assertFalse(msgs.contains("Escrever uma mensagem"))
    }
    @Test fun `encontra no por texto`() {
        assertNotNull(UiTreeReader.findNodeByText(tree, "maria"))
        assertNull(UiTreeReader.findNodeByText(tree, "pedro"))
    }
    @Test fun `encontra campo editavel`() {
        assertNotNull(UiTreeReader.findEditable(tree))
    }
}
