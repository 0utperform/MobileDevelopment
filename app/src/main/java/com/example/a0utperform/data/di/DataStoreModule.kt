package com.example.a0utperform.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.local.user.dataStore
import com.example.dicodingeventbottomnav.database.setting.SettingPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {


    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore


    @Provides
    @Singleton
    fun provideUserPreference(dataStore: DataStore<Preferences>): UserPreference =
        UserPreference.getInstance(dataStore)

    @Provides
    @Singleton
    fun provideSettingPreferences(dataStore: DataStore<Preferences>): SettingPreferences =
        SettingPreferences.getInstance(dataStore)
}