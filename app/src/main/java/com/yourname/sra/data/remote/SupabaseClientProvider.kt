package com.yourname.sra.data.remote

import com.yourname.sra.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Provides a singleton Supabase client instance.
 * 
 * Session Persistence:
 * The Auth module automatically handles session persistence using Android's
 * SharedPreferences. Sessions are stored securely and restored on app restart
 * without requiring any manual session management.
 * 
 * Modules installed:
 * - Auth (GoTrue): Authentication with automatic session persistence
 * - Postgrest: Database operations
 * - Storage: File uploads/downloads
 * - Realtime: Live data synchronization
 * 
 * Fulfills Requirement 3: Session Persistence
 */
object SupabaseClientProvider {

    init {
        require(BuildConfig.SUPABASE_URL.isNotBlank()) {
            "SUPABASE_URL not configured. Add it to local.properties"
        }
        require(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "SUPABASE_ANON_KEY not configured. Add it to local.properties"
        }
    }

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) // Handles automatic session persistence
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}

