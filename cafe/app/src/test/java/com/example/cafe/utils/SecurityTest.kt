package com.example.cafe.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityTest {

    @Test
    fun `sha256Hex es determinista y no devuelve el texto plano`() {
        val hash = sha256Hex("cliente123")
        assertEquals(hash, sha256Hex("cliente123"))
        assertNotEquals(hash, "cliente123")
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in "0123456789abcdef" })
    }

    @Test
    fun `verifyStoredPassword acepta hash y texto plano legado`() {
        val plain = "cliente123"
        assertTrue(verifyStoredPassword(plain, sha256Hex(plain)))
        assertTrue(verifyStoredPassword("admin", "admin"))
        assertFalse(verifyStoredPassword("otra", sha256Hex(plain)))
        assertFalse(verifyStoredPassword("admin", ""))
    }

    @Test
    fun `emailError valida formato y vacios`() {
        assertNull(emailError("admin@cafearoma.com"))
        assertNull(emailError("  juan+tag@dominio.co "))
        assertEquals(null == emailError("sin-arroba"), false)
        assertEquals(null == emailError(""), false)
        assertEquals(null == emailError("a@b"), false)
    }

    @Test
    fun `passwordError exige 8+ caracteres con letras y numeros`() {
        assertNull(passwordError("cliente123"))
        assertEquals(null == passwordError("corta1"), false)
        assertEquals(null == passwordError("solo letras"), false)
        assertEquals(null == passwordError("12345678"), false)
        assertEquals(null == passwordError(""), false)
    }

    @Test
    fun `passwordStrength y strengthLabel crecen con la longitud`() {
        assertEquals(0, passwordStrength(""))
        assertEquals(1, passwordStrength("abc"))
        assertTrue(passwordStrength("abcdefgh") < passwordStrength("abcdefgh1234"))
        assertEquals("", strengthLabel(0))
        assertEquals("Excelente", strengthLabel(4))
    }
}
