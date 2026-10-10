package bob.colbaskin.stoq.di

import bob.colbaskin.stoq.feature.auth.data.AuthRepositoryImpl
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.common.userprefs.data.UserPreferencesRepositoryImpl
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        implementation: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}
