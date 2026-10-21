package com.dumpphone.actions

import com.dumpphone.data.PtVariant
import org.junit.Assert.*
import org.junit.Test

class PhoneNumbersTest {
    @Test fun `numero pt de 9 digitos ganha 351`() {
        assertEquals("351912345678", PhoneNumbers.toInternationalDigits("912 345 678", PtVariant.PT_PT))
    }
    @Test fun `numero pt ja internacional`() {
        assertEquals("351912345678", PhoneNumbers.toInternationalDigits("+351 912 345 678", PtVariant.PT_PT))
    }
    @Test fun `fixo pt`() {
        assertEquals("351213456789", PhoneNumbers.toInternationalDigits("213 456 789", PtVariant.PT_PT))
    }
    @Test fun `br com ddd ganha 55`() {
        assertEquals("5511912345678", PhoneNumbers.toInternationalDigits("(11) 91234-5678", PtVariant.PT_BR))
    }
    @Test fun `br sem ddd e ambiguo`() {
        assertNull(PhoneNumbers.toInternationalDigits("91234-5678", PtVariant.PT_BR))
    }
    @Test fun `vazio ou invalido`() {
        assertNull(PhoneNumbers.toInternationalDigits("", PtVariant.PT_PT))
        assertNull(PhoneNumbers.toInternationalDigits(null, PtVariant.PT_PT))
    }
    @Test fun `emergencia`() {
        assertTrue(PhoneNumbers.isEmergencyNumber("112"))
        assertFalse(PhoneNumbers.isEmergencyNumber("912345678"))
    }
}
