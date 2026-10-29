package app.dsiacademy.academy.di

import app.dsiacademy.academy.data.content.ContentApi
import app.dsiacademy.academy.data.content.DeliveryApi
import app.dsiacademy.academy.data.content.DeliveryRepository
import app.dsiacademy.academy.data.content.DeliveryRepositoryImpl
import app.dsiacademy.academy.data.content.ContentRepository
import app.dsiacademy.academy.data.content.ContentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentModule {
    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    @Binds
    @Singleton
    abstract fun bindDeliveryRepository(impl: DeliveryRepositoryImpl): DeliveryRepository

    companion object {
        @Provides
        @Singleton
        fun provideContentApi(@Named("content") retrofit: Retrofit): ContentApi =
            retrofit.create(ContentApi::class.java)

        @Provides
        @Singleton
        fun provideDeliveryApi(@Named("content") retrofit: Retrofit): DeliveryApi =
            retrofit.create(DeliveryApi::class.java)
    }
}
