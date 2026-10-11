package bob.colbaskin.stoq.di

import bob.colbaskin.stoq.feature.auth.data.AuthRepositoryImpl
import bob.colbaskin.stoq.feature.auth.domain.AuthRepository
import bob.colbaskin.stoq.common.userprefs.data.UserPreferencesRepositoryImpl
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import bob.colbaskin.stoq.feature.orders.data.MockOrderRepository
import bob.colbaskin.stoq.feature.orders.domain.OrderRepository
import bob.colbaskin.stoq.feature.admin.data.MockAdminRepository
import bob.colbaskin.stoq.feature.admin.domain.AdminRepository
import bob.colbaskin.stoq.feature.inventory.data.MockInventoryRepository
import bob.colbaskin.stoq.feature.inventory.domain.InventoryRepository
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

    @Binds
    @Singleton
    abstract fun bindOrderRepository(implementation: MockOrderRepository): OrderRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(implementation: MockAdminRepository): AdminRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(implementation: MockInventoryRepository): InventoryRepository
}
