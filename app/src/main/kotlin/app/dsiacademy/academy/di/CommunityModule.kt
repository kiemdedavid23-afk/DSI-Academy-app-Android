package app.dsiacademy.academy.di

import app.dsiacademy.academy.data.community.CommunityApi
import app.dsiacademy.academy.data.community.CommunityRepository
import app.dsiacademy.academy.data.community.CommunityRepositoryImpl
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
abstract class CommunityModule {
    @Binds
    @Singleton
    abstract fun bindCommunityRepository(impl: CommunityRepositoryImpl): CommunityRepository

    companion object {
        @Provides
        @Singleton
        fun provideCommunityApi(@Named("community") retrofit: Retrofit): CommunityApi =
            retrofit.create(CommunityApi::class.java)
    }
}
