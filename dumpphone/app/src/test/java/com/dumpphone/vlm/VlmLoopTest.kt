package com.dumpphone.vlm

import com.dumpphone.a11y.UiAction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class VlmLoopTest {
    private fun obs(step: Int) = VlmObservation(null, listOf("ecrã"), "com.whatsapp", "goal", step)

    private class FakeClient(private val decisions: List<VlmDecision>) : VlmClient {
        override val isConfigured = true
        override val displayName = "fake"
        var calls = 0
        override suspend fun decide(observation: VlmObservation): Result<VlmDecision> {
            calls++
            return Result.success(decisions[minOf(observation.step, decisions.lastIndex)])
        }
    }

    @Test fun `sem consentimento recusa`() = runTest {
        val loop = VlmLoop(MockVlmClient(), VlmLoopConfig(consentGranted = false), { obs(0) }, { true }, {})
        assertTrue(loop.run("abrir conversa") is VlmLoopResult.Refused)
    }

    @Test fun `mock sem chave pede ao utilizador e nao executa`() = runTest {
        val executed = mutableListOf<UiAction>()
        val loop = VlmLoop(MockVlmClient(), VlmLoopConfig(consentGranted = true), { obs(0) }, { executed += it; true }, {})
        val result = loop.run("fazer algo")
        assertTrue(result is VlmLoopResult.StoppedAndAsked)
        assertTrue(executed.isEmpty())
    }

    @Test fun `baixa confianca para e pergunta`() = runTest {
        val client = FakeClient(listOf(VlmDecision(VlmAction.TapText("Maria"), "Vou tocar.", 0.3)))
        val loop = VlmLoop(client, VlmLoopConfig(consentGranted = true), { obs(0) }, { true }, {})
        assertTrue(loop.run("abrir") is VlmLoopResult.StoppedAndAsked)
    }

    @Test fun `limite de passos respeitado`() = runTest {
        val client = FakeClient(listOf(VlmDecision(VlmAction.Scroll, "Continuo.", 0.95)))
        val loop = VlmLoop(client, VlmLoopConfig(maxSteps = 3, maxModelCalls = 3, consentGranted = true), { obs(0) }, { true }, {})
        val result = loop.run("procurar")
        assertTrue(result is VlmLoopResult.CapReached)
        assertEquals(3, client.calls)
    }

    @Test fun `done termina com sucesso`() = runTest {
        val client = FakeClient(listOf(
            VlmDecision(VlmAction.TapText("Maria"), "A abrir a conversa.", 0.95),
            VlmDecision(VlmAction.Done, "Feito.", 0.99),
        ))
        val executed = mutableListOf<UiAction>()
        var step = 0
        val loop = VlmLoop(client, VlmLoopConfig(consentGranted = true), { obs(step++) }, { executed += it; true }, {})
        val result = loop.run("abrir conversa da Maria")
        assertTrue(result is VlmLoopResult.Completed)
        assertEquals(1, executed.size)
    }

    @Test fun `parse de decisao json`() {
        val d = HttpVlmClient.parseDecision("""{"action":"tap","text":"Maria","narrationPt":"Vou abrir.","confidence":0.9}""")
        assertNotNull(d)
        assertEquals(VlmAction.TapText("Maria"), d!!.action)
        assertNull(HttpVlmClient.parseDecision("""{"action":"explode","confidence":1}"""))
        assertNull(HttpVlmClient.parseDecision("not json"))
    }
}
