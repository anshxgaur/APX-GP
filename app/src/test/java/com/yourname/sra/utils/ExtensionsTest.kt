package com.yourname.sra.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for Extensions.kt functions
 * Tests error message mapping extension: Throwable.toUserMessage()
 */
class ExtensionsTest {

    @Test
    fun `toUserMessage maps IOException to network error message`() {
        val exception = IOException("Connection failed")
        val result = exception.toUserMessage()
        assertEquals("Network error. Please check your connection.", result)
    }

    @Test
    fun `toUserMessage maps SocketTimeoutException to timeout message`() {
        val exception = SocketTimeoutException("Request timed out")
        val result = exception.toUserMessage()
        assertEquals("Request timed out. Please try again.", result)
    }

    @Test
    fun `toUserMessage maps network keyword in message to network error`() {
        val exception = Exception("network error occurred")
        val result = exception.toUserMessage()
        assertEquals("Network error. Please check your connection.", result)
    }

    @Test
    fun `toUserMessage maps connection keyword in message to network error`() {
        val exception = Exception("connection refused")
        val result = exception.toUserMessage()
        assertEquals("Network error. Please check your connection.", result)
    }

    @Test
    fun `toUserMessage maps timeout keyword in message to timeout error`() {
        val exception = Exception("The request has timed out")
        val result = exception.toUserMessage()
        assertEquals("Request timed out. Please try again.", result)
    }

    @Test
    fun `toUserMessage maps already exists to duplicate email message`() {
        val exception = Exception("User already exists")
        val result = exception.toUserMessage()
        assertEquals("Email already registered", result)
    }

    @Test
    fun `toUserMessage maps duplicate keyword to duplicate email message`() {
        val exception = Exception("Duplicate key error")
        val result = exception.toUserMessage()
        assertEquals("Email already registered", result)
    }

    @Test
    fun `toUserMessage maps unique constraint to duplicate email message`() {
        val exception = Exception("unique constraint violation")
        val result = exception.toUserMessage()
        assertEquals("Email already registered", result)
    }

    @Test
    fun `toUserMessage maps invalid keyword to invalid credentials message`() {
        val exception = Exception("Invalid email or password provided")
        val result = exception.toUserMessage()
        assertEquals("Invalid email or password", result)
    }

    @Test
    fun `toUserMessage maps credentials keyword to invalid credentials message`() {
        val exception = Exception("credentials are incorrect")
        val result = exception.toUserMessage()
        assertEquals("Invalid email or password", result)
    }

    @Test
    fun `toUserMessage maps authentication failed to invalid credentials message`() {
        val exception = Exception("Authentication failed")
        val result = exception.toUserMessage()
        assertEquals("Invalid email or password", result)
    }

    @Test
    fun `toUserMessage maps unauthorized to invalid credentials message`() {
        val exception = Exception("Unauthorized access")
        val result = exception.toUserMessage()
        assertEquals("Invalid email or password", result)
    }

    @Test
    fun `toUserMessage returns exception message for unmapped errors`() {
        val exception = Exception("Some custom error message")
        val result = exception.toUserMessage()
        assertEquals("Some custom error message", result)
    }

    @Test
    fun `toUserMessage returns generic message when exception message is null`() {
        val exception = Exception(null as String?)
        val result = exception.toUserMessage()
        assertEquals("An unexpected error occurred. Please try again.", result)
    }

    @Test
    fun `toUserMessage is case insensitive for network keyword`() {
        val exception = Exception("NETWORK ERROR")
        val result = exception.toUserMessage()
        assertEquals("Network error. Please check your connection.", result)
    }

    @Test
    fun `toUserMessage is case insensitive for invalid keyword`() {
        val exception = Exception("INVALID CREDENTIALS")
        val result = exception.toUserMessage()
        assertEquals("Invalid email or password", result)
    }
}
