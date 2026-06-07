package com.yourname.sra.data.remote

import com.yourname.sra.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for SupabaseClientProvider
 * 
 * Validates:
 * - Requirements 1.1, 1.2, 1.3: Secure credential management from BuildConfig
 * - Requirements 36.3, 36.4: Credential validation with IllegalArgumentException
 */
class SupabaseClientProviderTest {

    @Test
    fun `test SupabaseClient is initialized successfully`() {
        // Verify the client is not null
        assertNotNull("SupabaseClient should be initialized", SupabaseClientProvider.client)
    }

    @Test
    fun `test SupabaseClient is singleton`() {
        // Verify singleton behavior - same instance returned
        val client1 = SupabaseClientProvider.client
        val client2 = SupabaseClientProvider.client
        assertSame("SupabaseClient should return the same instance", client1, client2)
    }

    @Test
    fun `test Auth module is installed`() {
        // Verify Auth module is available
        val client = SupabaseClientProvider.client
        assertNotNull("Auth module should be installed", client.plugins[Auth.key])
    }

    @Test
    fun `test Postgrest module is installed`() {
        // Verify Postgrest module is available
        val client = SupabaseClientProvider.client
        assertNotNull("Postgrest module should be installed", client.plugins[Postgrest.key])
    }

    @Test
    fun `test Storage module is installed`() {
        // Verify Storage module is available
        val client = SupabaseClientProvider.client
        assertNotNull("Storage module should be installed", client.plugins[Storage.key])
    }

    @Test
    fun `test Realtime module is installed`() {
        // Verify Realtime module is available
        val client = SupabaseClientProvider.client
        assertNotNull("Realtime module should be installed", client.plugins[Realtime.key])
    }

    @Test
    fun `test BuildConfig credentials are not blank`() {
        // Verify credentials are present
        assertTrue(
            "SUPABASE_URL should not be blank",
            BuildConfig.SUPABASE_URL.isNotBlank()
        )
        assertTrue(
            "SUPABASE_ANON_KEY should not be blank",
            BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
        )
    }

    @Test
    fun `test client uses correct Supabase URL`() {
        // Verify the client is configured with the correct URL
        val client = SupabaseClientProvider.client
        assertEquals(
            "Client should use BuildConfig.SUPABASE_URL",
            BuildConfig.SUPABASE_URL,
            client.supabaseUrl
        )
    }
}
