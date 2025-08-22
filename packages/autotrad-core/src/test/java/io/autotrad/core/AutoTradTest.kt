package io.autotrad.core

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class AutoTradTest {
    
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
    }
    
    @Test
    fun `test init sets default locale`() = runTest {
        // Given
        val defaultLocale = Locale.getDefault()
        
        // When
        AutoTrad.init(mockContext)
        
        // Then
        assertEquals(defaultLocale, AutoTrad.currentLocale.value)
    }
    
    @Test
    fun `test setLocale changes current locale`() = runTest {
        // Given
        AutoTrad.init(mockContext)
        val newLocale = Locale.ENGLISH
        
        // When
        AutoTrad.setLocale("en")
        
        // Then
        assertEquals("en", AutoTrad.currentLocale.value.language)
    }
    
    @Test
    fun `test translate with EchoTranslator returns original text`() = runTest {
        // Given
        AutoTrad.init(mockContext, translators = listOf(EchoTranslator()))
        val originalText = "Hello World"
        
        // When
        val result = AutoTrad.translate(originalText)
        
        // Then
        assertEquals(originalText, result)
    }
    
    @Test
    fun `test translate with custom translator`() = runTest {
        // Given
        val customTranslator = object : Translator {
            override suspend fun translate(text: String, src: Locale?, tgt: Locale): String? {
                return when (text) {
                    "Hello" -> "Hola"
                    else -> null
                }
            }
        }
        AutoTrad.init(mockContext, translators = listOf(customTranslator))
        
        // When
        val result = AutoTrad.translate("Hello")
        
        // Then
        assertEquals("Hola", result)
    }
}
