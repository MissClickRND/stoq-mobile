package bob.colbaskin.stoq.di

import android.content.Context
import bob.colbaskin.stoq.common.userprefs.data.datastore.UserDataStore
import bob.colbaskin.stoq.common.userprefs.data.datastore.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideUserPreferencesSerializer(): UserPreferencesSerializer = UserPreferencesSerializer

    @Provides
    @Singleton
    fun provideUserDataStore(@ApplicationContext context: Context): UserDataStore = UserDataStore(context)
}
