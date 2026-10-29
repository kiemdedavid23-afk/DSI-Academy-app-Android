package app.dsiacademy.academy.di

import app.dsiacademy.academy.core.session.SessionStore
import app.dsiacademy.academy.core.session.SessionStoreImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionStore(impl: SessionStoreImpl): SessionStore
}
