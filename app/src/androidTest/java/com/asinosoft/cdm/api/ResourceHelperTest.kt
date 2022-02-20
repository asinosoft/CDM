package com.asinosoft.cdm.api

import androidx.test.platform.app.InstrumentationRegistry
import com.asinosoft.cdm.helpers.getBackgroundUrl
import org.junit.Assert
import org.junit.Test

class ResourceHelperTest {
    @Test
    fun check_all_background_file_presence() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertNotNull(context.getBackgroundUrl(0))
        Assert.assertNotNull(context.getBackgroundUrl(1))
        Assert.assertNotNull(context.getBackgroundUrl(2))
        Assert.assertNotNull(context.getBackgroundUrl(3))
        Assert.assertNotNull(context.getBackgroundUrl(4))
        Assert.assertNotNull(context.getBackgroundUrl(5))
        Assert.assertNotNull(context.getBackgroundUrl(6))
        Assert.assertNotNull(context.getBackgroundUrl(7))
        Assert.assertNotNull(context.getBackgroundUrl(8))
        Assert.assertNotNull(context.getBackgroundUrl(9))
        Assert.assertNull(context.getBackgroundUrl(10))
    }
}
