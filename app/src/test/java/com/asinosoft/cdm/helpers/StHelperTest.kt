package com.asinosoft.cdm.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.* // ktlint-disable no-wildcard-imports

class StHelperTest {
    @Test
    fun convertNumber() {
        Locale.setDefault(Locale("ru", "ru"))
        assertEquals("765", StHelper.convertNumber("765"))
        assertEquals("+79137106193", StHelper.convertNumber("+79137106193"))
        assertEquals("+79137106193", StHelper.convertNumber("79137106193@s.whatsapp.net"))
        assertEquals("+79137106193", StHelper.convertNumber("Message +79137106193"))
    }

    @Test
    fun getAge() {
        assertEquals(0, StHelper.getAge(""))
        assertEquals(0, StHelper.getAge("invalid date"))

        val ymd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthday = Calendar.getInstance()
        assertEquals(0, StHelper.getAge(ymd.format(birthday.time)))

        birthday.set(Calendar.YEAR, birthday.get(Calendar.YEAR) - 1)
        assertEquals(1, StHelper.getAge(ymd.format(birthday.time)))

        birthday.add(Calendar.DATE, 1)
        assertEquals(0, StHelper.getAge(ymd.format(birthday.time)))

        birthday.add(Calendar.YEAR, -10)
        assertEquals(10, StHelper.getAge(ymd.format(birthday.time)))

        birthday.add(Calendar.MONTH, 6)
        assertEquals(10, StHelper.getAge(ymd.format(birthday.time)))

        birthday.add(Calendar.MONTH, 6)
        assertEquals(9, StHelper.getAge(ymd.format(birthday.time)))
    }
}
