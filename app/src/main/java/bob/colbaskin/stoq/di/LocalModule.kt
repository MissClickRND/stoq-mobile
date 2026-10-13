package bob.colbaskin.stoq.di

import android.content.Context
import androidx.room.Room
import bob.colbaskin.stoq.common.userprefs.data.datastore.UserDataStore
import bob.colbaskin.stoq.common.userprefs.data.datastore.UserPreferencesSerializer
import bob.colbaskin.stoq.common.drafts.data.StoqDatabase
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

    @Provides
    @Singleton
    fun provideStoqDatabase(@ApplicationContext context: Context): StoqDatabase = Room.databaseBuilder(
        context,
        StoqDatabase::class.java,
        "stoq.db"
    ).build()

    @Provides
    @Singleton
    fun provideDraftDao(database: StoqDatabase) = database.draftDao()
}
