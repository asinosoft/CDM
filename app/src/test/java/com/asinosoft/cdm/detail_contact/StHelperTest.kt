package com.asinosoft.cdm.detail_contact

import com.asinosoft.cdm.helpers.StHelper
import org.junit.Assert
import org.junit.Test
import java.util.* // ktlint-disable no-wildcard-imports

class StHelperTest {
    @Test
    fun `recognize and convert phone number`() {
        Locale.setDefault(Locale("ru", "ru"))
        Assert.assertEquals("765", StHelper.convertNumber("765"))
        Assert.assertEquals("+79137106193", StHelper.convertNumber("+79137106193"))
        Assert.assertEquals("+79137106193", StHelper.convertNumber("79137106193@s.whatsapp.net"))
        Assert.assertEquals("+79137106193", StHelper.convertNumber("Message +79137106193"))
    }
}
