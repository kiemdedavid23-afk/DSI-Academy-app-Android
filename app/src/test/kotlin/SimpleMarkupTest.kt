package app.dsiacademy.academy.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleMarkupTest {

    @Test
    fun grasSimple() {
        val a = SimpleMarkup.toAnnotatedString("Hello <b>world</b>!")
        assertEquals("Hello world!", a.text)
        assertTrue(a.spanStyles.any { it.item.fontWeight?.weight == 700 })
    }

    @Test
    fun sansBalise() {
        val a = SimpleMarkup.toAnnotatedString("Texte simple")
        assertEquals("Texte simple", a.text)
        assertTrue(a.spanStyles.isEmpty())
    }

    @Test
    fun plusieursGras() {
        val a = SimpleMarkup.toAnnotatedString("<b>A</b> et <b>B</b>")
        assertEquals("A et B", a.text)
        assertEquals(2, a.spanStyles.size)
    }
}
