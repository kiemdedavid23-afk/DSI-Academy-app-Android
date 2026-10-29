package app.dsiacademy.academy.core.network

import app.dsiacademy.academy.BuildConfig

/**
 * URLs de base des 6 Workers — valeurs identiques à src/app/config/env.js du site.
 * Source de vérité : le site (et CONTRATS-API.md).
 */
object WorkerUrls {
    val PAYMENT: String = BuildConfig.PAYMENT_WORKER_URL
    val CONTENT: String = BuildConfig.CONTENT_WORKER_URL
    val SHOP: String = BuildConfig.SHOP_WORKER_URL
    val MARKETPLACE: String = BuildConfig.MARKETPLACE_WORKER_URL
    val COMMUNITY: String = BuildConfig.COMMUNITY_WORKER_URL
    val ADMIN: String = BuildConfig.ADMIN_WORKER_URL
}
