package app.dsiacademy.academy.di

import app.dsiacademy.academy.core.common.LinkPolicy
import app.dsiacademy.academy.core.config.SiteLinks
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BrowserModule {

    @Provides
    @Singleton
    fun provideLinkPolicy(): LinkPolicy =
        LinkPolicy(
            siteUrl = SiteLinks.baseUrl,
            externalHosts = SiteLinks.externalHosts,
        )
}
