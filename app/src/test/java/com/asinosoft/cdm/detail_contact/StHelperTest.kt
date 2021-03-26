package com.asinosoft.cdm.detail_contact

import org.junit.Assert
import org.junit.Test
import java.text.ParseException

class StHelperTest {
    @Test
    fun convertNumber_worksWithCommonNumbers() {
        validData.forEach {
            Assert.assertEquals(it.converted, StHelper.convertNumber(it.phone))
        }
    }

    @Test
    fun convertNumber_worksWithInvalidNumbers() {
        // Всё что не подходит под номер телефона - просто игнорируется, функция возвращает "-1"
        invalidData.forEach {
            Assert.assertEquals("-1", StHelper.convertNumber(it))
        }
    }

    @Test
    fun `parseToMillis_don'tWorkWithInvalidDates`() {
        Assert.assertThrows(ParseException::class.java) {
            StHelper.parseToMillis("--01-01")
        }
    }

    data class ValidData (
        val phone: String,
        val converted: String
    )

    private val validData = listOf(
        ValidData("9876543210", "+7 987 654-32-10"), // российский номер
        ValidData("7773335544", "+7 777 333 5544"), // казахский номер
    )

    private val invalidData = listOf(
        "",
        "   ",
        "zz",
        "мой телефон"
    )
}
