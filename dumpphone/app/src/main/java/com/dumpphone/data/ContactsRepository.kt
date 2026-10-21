package com.dumpphone.data

import android.content.Context

/**
 * Contact list for the home screen. Dummy mode never touches the address book:
 * there is no query, so a granted contacts permission cannot leak real names or numbers.
 */
class ContactsRepository(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") settings: SettingsStore,
) {

    /** Example people only. The address book is not opened. */
    fun loadContacts(): List<Contact> = demoContacts()

    fun favourites(contacts: List<Contact>): List<Contact> {
        val starred = contacts.filter { it.isFavourite }
        // If nobody is starred yet, show the first contacts so the home screen is never empty.
        return if (starred.isNotEmpty()) starred else contacts.take(6)
    }

    companion object {
        /** Sample people from the casa-quentinha screens. Numbers are labels only. */
        fun demoContacts(): List<Contact> = listOf(
            Contact("demo-maria", "Maria Silva", isFavourite = true, nicknames = listOf("filha"), displayPhone = "912 345 678", relation = "Filha"),
            Contact("demo-joao", "João Silva", isFavourite = true, nicknames = listOf("filho"), displayPhone = "913 222 010", relation = "Filho"),
            Contact("demo-ana", "Ana", isFavourite = true, nicknames = listOf("neta", "ana"), displayPhone = "961 100 200", relation = "Neta"),
            Contact("demo-carlos", "Carlos", nicknames = listOf("irmao", "carlos"), displayPhone = "934 555 018", relation = "Irmão"),
            Contact("demo-maria-costa", "Maria Costa", nicknames = listOf("vizinha"), displayPhone = "965 404 221", relation = "Vizinha"),
        )

        fun mockThreads(): List<MockThread> = listOf(
            MockThread("demo-maria", "Maria Silva", "Filha", "09:12", 3, "Mãe, já cheguei a ...", "Mãe, já cheguei a casa. Beijinhos!"),
            MockThread("demo-joao", "João Silva", "Filho", "Ontem", 1, "Amanhã passo aí ...", "Amanhã passo aí depois do almoço."),
            MockThread("demo-ana", "Ana", "Neta", "Ontem", 0, "Avó, veja a fotogr...", "Avó, veja a fotografia da escola."),
            MockThread("demo-familia", "Família", "Grupo · 5 pessoas", "Seg.", 0, "Quem traz o pão n...", "Quem traz o pão no domingo?"),
        )
    }
}
