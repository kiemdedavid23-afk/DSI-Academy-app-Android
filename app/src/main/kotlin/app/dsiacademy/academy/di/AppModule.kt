package app.dsiacademy.academy.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Module Hilt racine.
 * Les liaisons domain → data seront ajoutées ici (ou dans des modules par domaine)
 * au fur et à mesure des tâches.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
