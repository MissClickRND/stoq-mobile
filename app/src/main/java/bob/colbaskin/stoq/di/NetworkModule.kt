package bob.colbaskin.stoq.di

import bob.colbaskin.stoq.BuildConfig
import bob.colbaskin.stoq.feature.auth.data.remote.AuthApiService
import bob.colbaskin.stoq.feature.dishes.data.remote.DishesApiService
import bob.colbaskin.stoq.feature.dishes.data.remote.RecommendationsApiService
import bob.colbaskin.stoq.feature.products.data.remote.InventoryApiService
import bob.colbaskin.stoq.feature.products.data.remote.ProductsApiService
import bob.colbaskin.stoq.feature.orders.data.remote.OrdersApiService
import bob.colbaskin.stoq.feature.supplies.data.remote.SuppliesApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDishesApiService(retrofit: Retrofit): DishesApiService = retrofit.create(DishesApiService::class.java)

    @Provides
    @Singleton
    fun provideRecommendationsApiService(retrofit: Retrofit): RecommendationsApiService = retrofit.create(RecommendationsApiService::class.java)

    @Provides
    @Singleton
    fun provideInventoryApiService(retrofit: Retrofit): InventoryApiService = retrofit.create(InventoryApiService::class.java)

    @Provides
    @Singleton
    fun provideProductsApiService(retrofit: Retrofit): ProductsApiService = retrofit.create(ProductsApiService::class.java)

    @Provides
    @Singleton
    fun provideSuppliesApiService(retrofit: Retrofit): SuppliesApiService = retrofit.create(SuppliesApiService::class.java)

    @Provides
    @Singleton
    fun provideOrdersApiService(retrofit: Retrofit): OrdersApiService = retrofit.create(OrdersApiService::class.java)
}
