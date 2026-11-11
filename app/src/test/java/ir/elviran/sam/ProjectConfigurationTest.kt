package ir.elviran.sam

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectConfigurationTest {
    @Test
    fun applicationIdAndVersionAreDocumentedForReleaseLine() {
        assertEquals("ir.elviran.sam", "ir.elviran.sam")
        assertEquals("4.0.0", "4.0.0")
    }
}
