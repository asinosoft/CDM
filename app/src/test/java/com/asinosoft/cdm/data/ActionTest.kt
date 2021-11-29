package com.asinosoft.cdm.data

import android.net.Uri
import org.junit.Assert
import org.junit.Test

class ActionTest {
    @Test fun distinctOnTypeAndValue() {
        val one = Action(7, Action.Type.PhoneCall, "12345", "Mobile")
        val another = Action(222, Action.Type.PhoneCall, "12345", "Home")

        Assert.assertEquals(one, another)

        val contact = Contact(1, "Test", Uri.EMPTY)
        contact.actions.add(one)
        contact.actions.add(another)

        Assert.assertEquals(1, contact.actions.size)
        Assert.assertEquals(one.id, contact.actions.first().id)
    }
}
