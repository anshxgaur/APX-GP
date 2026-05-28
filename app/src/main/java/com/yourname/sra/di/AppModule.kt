package com.yourname.sra.di

import android.content.Context
import androidx.room.Room
import com.yourname.sra.data.local.AppDatabase
import com.yourname.sra.data.local.TaskDao
import com.yourname.sra.data.remote.SupabaseClientProvider
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.NotificationRepository
import com.yourname.sra.data.repository.ProfileRepository
import com.yourname.sra.data.repository.SurveyRepository
import com.yourname.sra.data.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClientProvider.client
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sra_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(supabaseClient: SupabaseClient): AuthRepository {
        return AuthRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        supabaseClient: SupabaseClient,
        taskDao: TaskDao
    ): TaskRepository {
        return TaskRepository(supabaseClient, taskDao)
    }

    @Provides
    @Singleton
    fun provideSurveyRepository(supabaseClient: SupabaseClient): SurveyRepository {
        return SurveyRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(supabaseClient: SupabaseClient): ProfileRepository {
        return ProfileRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(supabaseClient: SupabaseClient): NotificationRepository {
        return NotificationRepository(supabaseClient)
    }
}
