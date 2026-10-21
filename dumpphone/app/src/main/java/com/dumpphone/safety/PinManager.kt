package com.dumpphone.safety

import java.security.MessageDigest
import java.security.SecureRandom

/** Caregiver PIN hashing (salted SHA-256). Pure logic so it is unit-testable. */
object PinHasher {
    fun newSaltHex(): String {
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(pin: String, saltHex: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest(("$saltHex:$pin").toByteArray(Charsets.UTF_8))
        return hashed.joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, saltHex: String, expectedHashHex: String): Boolean =
        hash(pin, saltHex) == expectedHashHex

    fun isValidPinFormat(pin: String): Boolean = pin.length in 4..8 && pin.all { it.isDigit() }
}

/**
 * Default pilot PIN is `1234`. It MUST be changed by the caregiver on first
 * setup (the settings screen prompts for this). It is documented in the
 * README pilot instructions — it is not a secret, it is a child-lock-grade
 * barrier against accidental taps by the elderly user, not a security boundary.
 */
const val DEFAULT_CAREGIVER_PIN = "1234"
