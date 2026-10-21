package com.dumpphone.actions

import com.dumpphone.data.PtVariant

/** Phone-number normalization for wa.me / tel: links. Pure logic, unit-tested. */
object PhoneNumbers {
    /** Digits only, with country code. Returns null when a safe number cannot be derived. */
    fun toInternationalDigits(raw: String?, variant: PtVariant): String? {
        if (raw.isNullOrBlank()) return null
        var digits = raw.filter { it.isDigit() }
        if (raw.trim().startsWith("00")) digits = digits.removePrefix("00")
        if (digits.isBlank()) return null
        return when {
            // Already international (PT 351 + 9 digits, BR 55 + 10/11 digits)
            digits.startsWith("351") && digits.length == 12 -> digits
            digits.startsWith("55") && (digits.length == 12 || digits.length == 13) -> digits
            digits.length > 10 && raw.trim().startsWith("+") -> digits
            variant == PtVariant.PT_PT && digits.length == 9 -> "351$digits"
            // pt-BR local numbers need a DDD (area code); 8/9-digit numbers alone are ambiguous.
            variant == PtVariant.PT_BR && (digits.length == 10 || digits.length == 11) -> "55$digits"
            else -> null
        }
    }

    fun isEmergencyNumber(raw: String?): Boolean {
        val d = raw?.filter { it.isDigit() } ?: return false
        return d == "112" || d == "911"
    }
}
