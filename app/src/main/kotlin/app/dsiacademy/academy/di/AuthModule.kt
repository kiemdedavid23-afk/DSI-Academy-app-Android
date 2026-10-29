package app.dsiacademy.academy.di

import app.dsiacademy.academy.data.member.AuthRepositoryImpl
import app.dsiacademy.academy.data.member.MemberRepositoryImpl
import app.dsiacademy.academy.data.member.api.AuthApi
import app.dsiacademy.academy.data.member.api.MemberApi
import app.dsiacademy.academy.domain.member.AuthRepository
import app.dsiacademy.academy.domain.member.MemberRepository
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
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMemberRepository(impl: MemberRepositoryImpl): MemberRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(@Named("payment") retrofit: Retrofit): AuthApi =
            retrofit.create(AuthApi::class.java)

        @Provides
        @Singleton
        fun provideMemberApi(@Named("payment") retrofit: Retrofit): MemberApi =
            retrofit.create(MemberApi::class.java)
    }
}
