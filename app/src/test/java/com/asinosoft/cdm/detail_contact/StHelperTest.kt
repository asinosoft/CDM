package com.asinosoft.cdm.detail_contact

import com.asinosoft.cdm.helpers.StHelper
import org.junit.Assert
import org.junit.Test

class StHelperTest {
    @Test
    fun `recognize and convert phone number`() {
        Assert.assertEquals("+7 913 710-61-93", StHelper.convertNumber("+79137106193"))
        Assert.assertEquals("+7 913 710-61-93", StHelper.convertNumber("79137106193@s.whatsapp.net"))
        Assert.assertEquals("+7 913 710-61-93", StHelper.convertNumber("Message +79137106193"))
    }
}
