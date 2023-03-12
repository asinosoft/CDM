package com.asinosoft.cdm.helpers

import org.junit.Assert
import org.junit.Test
import java.util.*

class DateHelperTest {
    @Test
    fun age() {
        val birthday = Calendar.getInstance()
        Assert.assertEquals(0, DateHelper.age(birthday.time))

        birthday.set(Calendar.YEAR, birthday.get(Calendar.YEAR) - 1)
        Assert.assertEquals(1, DateHelper.age(birthday.time))

        birthday.add(Calendar.DATE, 1)
        Assert.assertEquals(0, DateHelper.age(birthday.time))

        birthday.add(Calendar.YEAR, -10)
        Assert.assertEquals(10, DateHelper.age(birthday.time))

        birthday.add(Calendar.MONTH, 6)
        Assert.assertEquals(10, DateHelper.age(birthday.time))

        birthday.add(Calendar.MONTH, 6)
        Assert.assertEquals(9, DateHelper.age(birthday.time))
    }
}
