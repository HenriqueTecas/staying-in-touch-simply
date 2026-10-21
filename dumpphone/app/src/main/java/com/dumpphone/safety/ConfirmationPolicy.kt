package com.dumpphone.safety

import com.dumpphone.data.DummyMode
import com.dumpphone.voice.ParsedIntent

/**
 * Phase 4 safety rule, non-negotiable:
 *  - No message is EVER sent without read-back confirmation.
 *  - Calls to an ambiguous contact always confirm; calls to a unique,
 *    confidently matched contact also confirm in the voice flow (one
 *    confirmation maximum per task, per the v1 success criteria).
 *  - Opening an app / setting an alarm does not need confirmation.
 */
object ConfirmationPolicy {

    fun requiresConfirmation(intent: ParsedIntent, contactWasAmbiguous: Boolean): Boolean = when (intent) {
        is ParsedIntent.SendWhatsApp, is ParsedIntent.SendSms -> true
        is ParsedIntent.CallContact -> true // read-back is cheap; a wrong call is not
        is ParsedIntent.Sos -> false // SOS is one-tap by design; delay defeats it
        is ParsedIntent.MessengerRemoved -> false
        else -> contactWasAmbiguous
    }

    /** Plain-Portuguese read-back, e.g. `Quer enviar "Já cheguei" para a Maria Silva?` */
    fun confirmationPromptPt(intent: ParsedIntent, contactName: String?): String {
        val who = contactName ?: "esse contacto"
        return when (intent) {
            is ParsedIntent.SendWhatsApp -> "Quer enviar \"${intent.message}\" por WhatsApp para a $who?"
            is ParsedIntent.SendSms -> "Quer enviar a mensagem \"${intent.message}\" para a $who?"
            is ParsedIntent.MessengerRemoved -> DummyMode.MESSENGER_REMOVED
            is ParsedIntent.CallContact -> "Quer ligar para a $who?"
            is ParsedIntent.SetAlarm -> "Quer pôr o despertador para as ${"%02d:%02d".format(intent.hour, intent.minute)}?"
            is ParsedIntent.Sos -> "Quer pedir ajuda de emergência agora?"
            else -> "Quer continuar?"
        }
    }

    fun spokenConfirmationPromptPt(intent: ParsedIntent, contactName: String?): String =
        confirmationPromptPt(intent, contactName)
}
