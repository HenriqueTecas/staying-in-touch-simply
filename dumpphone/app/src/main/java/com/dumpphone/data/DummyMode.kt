package com.dumpphone.data

/**
 * Pilot safety switch. While this is true the app never reads the phone's
 * contacts and never starts a call, an SMS, a WhatsApp chat, or a Messenger chat.
 * Alarms still work. Turn this off only when a caregiver is ready for real use.
 */
object DummyMode {
    const val ENABLED = true

    const val HELD_CALL = "Modo de teste. Não liguei para ninguém. Os nomes no ecrã são de exemplo, não são os seus contactos."
    const val HELD_MESSAGE = "Modo de teste. Não enviei nenhuma mensagem. Os nomes no ecrã são de exemplo."
    const val HELD_SOS = "Modo de teste. Não fiz nenhum telefonema, nem para o 112."
}
