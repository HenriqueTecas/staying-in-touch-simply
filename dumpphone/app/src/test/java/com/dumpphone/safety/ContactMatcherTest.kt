package com.dumpphone.safety

import com.dumpphone.data.Contact
import org.junit.Assert.*
import org.junit.Test

class ContactMatcherTest {
    private val contacts = listOf(
        Contact("1", "Maria Silva", "+351912345678"),
        Contact("2", "Maria Santos", "+351913456789"),
        Contact("3", "José Santos", "+351914567890", nicknames = listOf("Zé")),
        Contact("4", "Francisco Almeida", "+351915678901"),
        Contact("5", "Ana Pereira", "+351916789012", nicknames = listOf("mãezinha")),
    )

    @Test fun `nome completo unico`() {
        val r = ContactMatcher.match("maria silva", contacts)
        assertTrue(r is ContactMatcher.MatchResult.Unique)
        assertEquals("1", (r as ContactMatcher.MatchResult.Unique).contact.id)
    }
    @Test fun `maria ambiguo entre silva e santos`() {
        val r = ContactMatcher.match("maria", contacts)
        assertTrue("esperava Ambiguous mas foi $r", r is ContactMatcher.MatchResult.Ambiguous)
    }
    @Test fun `alcunha ze encontra jose`() {
        val r = ContactMatcher.match("ze", contacts)
        assertTrue(r is ContactMatcher.MatchResult.Unique)
        assertEquals("3", (r as ContactMatcher.MatchResult.Unique).contact.id)
    }
    @Test fun `alcunha chico encontra francisco`() {
        val r = ContactMatcher.match("chico", contacts)
        assertTrue(r is ContactMatcher.MatchResult.Unique)
        assertEquals("4", (r as ContactMatcher.MatchResult.Unique).contact.id)
    }
    @Test fun `alcunha personalizada maezinha`() {
        val r = ContactMatcher.match("maezinha", contacts)
        assertTrue(r is ContactMatcher.MatchResult.Unique)
        assertEquals("5", (r as ContactMatcher.MatchResult.Unique).contact.id)
    }
    @Test fun `sem correspondencia`() {
        assertEquals(ContactMatcher.MatchResult.NotFound, ContactMatcher.match("xpto desconhecido", contacts))
    }
    @Test fun `erro de pronuncia aproximado`() {
        val r = ContactMatcher.match("ana pereiraa", contacts)
        assertTrue(r is ContactMatcher.MatchResult.Unique)
    }
}
