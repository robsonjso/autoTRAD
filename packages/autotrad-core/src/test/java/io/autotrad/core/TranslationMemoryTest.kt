package io.autotrad.core

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class TranslationMemoryTest {
    
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
    }
    
    @Test
    fun `test translation memory stores and retrieves translations`() = runTest {
        // Given
        val customTranslator = object : Translator {
            override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
                return when (text) {
                    "Hello" -> "Hola"
                    "Goodbye" -> "Adiós"
                    else -> null
                }
            }
        }
        AutoTrad.init(mockContext, translators = listOf(customTranslator))
        
        // When - first translation (should use translator)
        val result1 = AutoTrad.translate("Hello", Locale.ENGLISH)
        
        // Then - should get translated result
        assertEquals("Hola", result1)
        
        // When - second translation of same text (should use TM)
        val result2 = AutoTrad.translate("Hello", Locale.ENGLISH)
        
        // Then - should get same result from TM
        assertEquals("Hola", result2)
    }
    
    @Test
    fun `test locale change triggers new translation`() = runTest {
        // Given
        val customTranslator = object : Translator {
            override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
                return when {
                    text == "Hello" && tgt.language == "es" -> "Hola"
                    text == "Hello" && tgt.language == "fr" -> "Bonjour"
                    else -> null
                }
            }
        }
        AutoTrad.init(mockContext, translators = listOf(customTranslator))
        
        // When - translate to Spanish
        AutoTrad.setLocale("es")
        val spanishResult = AutoTrad.translate("Hello")
        
        // Then
        assertEquals("Hola", spanishResult)
        
        // When - translate to French
        AutoTrad.setLocale("fr")
        val frenchResult = AutoTrad.translate("Hello")
        
        // Then
        assertEquals("Bonjour", frenchResult)
    }
}
