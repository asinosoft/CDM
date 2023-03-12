package com.asinosoft.cdm.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class StHelperTest {
    @Test
    fun convertNumber() {
        Locale.setDefault(Locale("ru", "ru"))
        assertEquals("765", StHelper.convertNumber("765"))
        assertEquals("+79137106193", StHelper.convertNumber("+79137106193"))
        assertEquals("+79137106193", StHelper.convertNumber("79137106193@s.whatsapp.net"))
        assertEquals("+79137106193", StHelper.convertNumber("Message +79137106193"))
    }
}
