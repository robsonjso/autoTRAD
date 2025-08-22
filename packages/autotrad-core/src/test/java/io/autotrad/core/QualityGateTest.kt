package io.autotrad.core

import org.junit.Assert.*
import org.junit.Test

class QualityGateTest {

    @Test
    fun `test placeholder preservation - valid`() {
        val source = "Hello, {name}! You have {count} messages."
        val candidate = "Hola, {name}! Tienes {count} mensajes."
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertTrue("Placeholders should be preserved", result)
    }

    @Test
    fun `test placeholder preservation - invalid`() {
        val source = "Hello, {name}! You have {count} messages."
        val candidate = "Hola, {user}! Tienes {count} mensajes." // {name} -> {user}
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertFalse("Placeholders should not be changed", result)
    }

    @Test
    fun `test placeholder preservation - missing placeholder`() {
        val source = "Hello, {name}! You have {count} messages."
        val candidate = "Hola! Tienes {count} mensajes." // missing {name}
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertFalse("All placeholders should be present", result)
    }

    @Test
    fun `test placeholder preservation - extra placeholder`() {
        val source = "Hello, {name}!"
        val candidate = "Hola, {name}! {extra}!" // extra {extra}
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertFalse("No extra placeholders should be added", result)
    }

    @Test
    fun `test button role limit - within limit`() {
        val source = "Hello"
        val candidate = "Hola" // 4 chars, within Button limit (16)
        
        val result = QualityGate.isAcceptable(source, candidate, TextRole.Button)
        assertTrue("Should be within button character limit", result)
    }

    @Test
    fun `test button role limit - exceeds limit`() {
        val source = "Hello"
        val candidate = "Hola, ¿cómo estás?" // 18 chars, exceeds Button limit (16)
        
        val result = QualityGate.isAcceptable(source, candidate, TextRole.Button)
        assertFalse("Should not exceed button character limit", result)
    }

    @Test
    fun `test chip role limit - within limit`() {
        val source = "Hello"
        val candidate = "Hola" // 4 chars, within Chip limit (12)
        
        val result = QualityGate.isAcceptable(source, candidate, TextRole.Chip)
        assertTrue("Should be within chip character limit", result)
    }

    @Test
    fun `test chip role limit - exceeds limit`() {
        val source = "Hello"
        val candidate = "Hola, ¿cómo estás?" // 18 chars, exceeds Chip limit (12)
        
        val result = QualityGate.isAcceptable(source, candidate, TextRole.Chip)
        assertFalse("Should not exceed chip character limit", result)
    }

    @Test
    fun `test label role - no limit`() {
        val source = "Hello"
        val candidate = "Hola, ¿cómo estás?" // any length for Label
        
        val result = QualityGate.isAcceptable(source, candidate, TextRole.Label)
        assertTrue("Should accept any length for Label role", result)
    }

    @Test
    fun `test blacklist patterns - email`() {
        val source = "user@example.com"
        val candidate = "usuario@ejemplo.com" // should not translate email
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertFalse("Should not translate email addresses", result)
    }

    @Test
    fun `test blacklist patterns - code`() {
        val source = "ABC123"
        val candidate = "ABC123" // should preserve code
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertTrue("Should preserve codes", result)
    }

    @Test
    fun `test complex placeholder scenario`() {
        val source = "Welcome {user}, you have {count} items in your {cart}."
        val candidate = "Bienvenido {user}, tienes {count} elementos en tu {cart}."
        
        val result = QualityGate.isAcceptable(source, candidate)
        assertTrue("Complex placeholders should be preserved", result)
    }
}
