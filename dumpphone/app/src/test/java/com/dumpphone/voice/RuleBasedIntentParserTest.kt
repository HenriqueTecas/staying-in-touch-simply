package com.dumpphone.voice

import org.junit.Assert.*
import org.junit.Test

class RuleBasedIntentParserTest {
    private val parser = RuleBasedIntentParser()

    @Test fun `ligar para contacto`() {
        val r = parser.parse("Ligar para a Maria Silva")
        assertTrue(r is ParsedIntent.CallContact)
        assertEquals("maria silva", (r as ParsedIntent.CallContact).contactQuery)
    }
    @Test fun `liga a com acento pt-PT`() {
        val r = parser.parse("Liga à Maria")
        assertTrue(r is ParsedIntent.CallContact)
        assertEquals("maria", (r as ParsedIntent.CallContact).contactQuery)
    }
    @Test fun `telefonar pt-BR`() {
        val r = parser.parse("Telefonar para o João")
        assertTrue(r is ParsedIntent.CallContact)
    }
    @Test fun `whatsapp com mensagem`() {
        val r = parser.parse("Manda um WhatsApp à Maria a dizer já cheguei")
        assertTrue(r is ParsedIntent.SendWhatsApp)
        r as ParsedIntent.SendWhatsApp
        assertEquals("maria", r.contactQuery)
        assertTrue(r.message.contains("cheguei", ignoreCase = true))
    }
    @Test fun `whatsapp sem mensagem pede ditado`() {
        val r = parser.parse("Enviar WhatsApp para o José")
        assertTrue(r is ParsedIntent.SendWhatsApp)
        assertEquals("", (r as ParsedIntent.SendWhatsApp).message)
    }
    @Test fun `messenger com mensagem`() {
        val r = parser.parse("Manda uma mensagem no Messenger para a Ana a dizer olá")
        assertTrue(r is ParsedIntent.SendMessenger)
    }
    @Test fun `sms explicito`() {
        val r = parser.parse("Manda um SMS para a Maria a dizer estou bem")
        assertTrue(r is ParsedIntent.SendSms)
    }
    @Test fun `despertador sete e meia`() {
        val r = parser.parse("Põe o despertador para as sete e meia")
        assertEquals(ParsedIntent.SetAlarm(7, 30), r)
    }
    @Test fun `despertador digital`() {
        assertEquals(ParsedIntent.SetAlarm(8, 15), parser.parse("Despertador às 8:15"))
    }
    @Test fun `acorda-me as oito`() {
        assertEquals(ParsedIntent.SetAlarm(8, 0), parser.parse("Acorda-me às oito"))
    }
    @Test fun `ler mensagens whatsapp`() {
        assertEquals(ParsedIntent.ReadMessages(ChatApp.WHATSAPP), parser.parse("Ler as mensagens do WhatsApp"))
    }
    @Test fun `ler mensagens messenger`() {
        assertEquals(ParsedIntent.ReadMessages(ChatApp.MESSENGER), parser.parse("Lê as mensagens do Messenger"))
    }
    @Test fun `socorro`() { assertEquals(ParsedIntent.Sos, parser.parse("Socorro!")) }
    @Test fun `cancelar`() { assertEquals(ParsedIntent.Cancel, parser.parse("Cancela")) }
    @Test fun `desconhecido nao adivinha`() {
        assertTrue(parser.parse("Qual é o tempo amanhã?") is ParsedIntent.Unknown)
    }
    @Test fun `parseTime meio-dia`() {
        assertEquals(12 to 0, RuleBasedIntentParser.parseTime("despertador para o meio dia"))
    }
    // --- Benchmark-driven fixes (evals/voice_benchmark.json) ---
    @Test fun `formal por favor ligue`() {
        assertTrue(parser.parse("Por favor, ligue para a Maria Silva") is ParsedIntent.CallContact)
    }
    @Test fun `podes ligar`() {
        assertTrue(parser.parse("Podes ligar à Maria?") is ParsedIntent.CallContact)
    }
    @Test fun `quero falar com sem app e chamada`() {
        val r = parser.parse("Quero falar com a minha filha Maria")
        assertTrue(r is ParsedIntent.CallContact)
        assertEquals("maria", (r as ParsedIntent.CallContact).contactQuery)
    }
    @Test fun `mensagem sem app e sms`() {
        assertTrue(parser.parse("Manda uma mensagem para a Maria a dizer já cheguei") is ParsedIntent.SendSms)
        assertTrue(parser.parse("Quero mandar uma mensagem à Ana a dizer que estou bem") is ParsedIntent.SendSms)
    }
    @Test fun `uatsap e grafia de sms`() {
        assertTrue(parser.parse("Manda um uatsap à Maria a dizer olá") is ParsedIntent.SendWhatsApp)
        assertTrue(parser.parse("Manda um esse-eme-esse para a Maria a dizer olá") is ParsedIntent.SendSms)
    }
    @Test fun `cancela com contexto`() {
        assertEquals(ParsedIntent.Cancel, parser.parse("Não, cancela isso"))
        assertEquals(ParsedIntent.Cancel, parser.parse("Esquece, não faças nada"))
    }
    @Test fun `ajuda-me e sos`() { assertEquals(ParsedIntent.Sos, parser.parse("Estou a cair, ajuda-me")) }
    @Test fun `alarme um alarme nao e uma da manha`() {
        assertEquals(ParsedIntent.SetAlarm(6, 0), parser.parse("Põe um alarme para amanhã às seis da manhã"))
    }
    @Test fun `alarme menos um quarto`() {
        assertEquals(ParsedIntent.SetAlarm(6, 45), parser.parse("Acorda-me às sete menos um quarto"))
    }
    @Test fun `alarme quarenta e cinco`() {
        assertEquals(ParsedIntent.SetAlarm(6, 45), parser.parse("Despertador às seis e quarenta e cinco"))
    }
    @Test fun `sms com dois pontos`() {
        val r = parser.parse("Mensagem para a Maria: estou bem")
        assertTrue(r is ParsedIntent.SendSms)
        r as ParsedIntent.SendSms
        assertEquals("maria", r.contactQuery)
        assertTrue(r.message.contains("estou bem", ignoreCase = true))
    }
    @Test fun `mandou uma mensagem nao e ler`() {
        val r = parser.parse("mandou uma mensagem no messenger para a Ana a dizer olá")
        assertTrue(r is ParsedIntent.SendMessenger)
    }
    @Test fun `o que me mandou e ler`() {
        assertEquals(ParsedIntent.ReadMessages(ChatApp.WHATSAPP), parser.parse("O que é que a Maria me mandou no WhatsApp?"))
    }
}
