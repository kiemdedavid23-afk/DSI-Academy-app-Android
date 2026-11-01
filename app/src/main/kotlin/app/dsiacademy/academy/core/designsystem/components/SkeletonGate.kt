package app.dsiacademy.academy.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Portail squelette (équivalent useSkeletonGate du site) :
 * - pendant [minVisibleMs] après le passage à loading, on peut encore afficher le squelette ;
 * - si loading et données absentes → squelette ;
 * - courte attente initiale sans contenu si on n'a pas encore décidé.
 */
@Composable
fun SkeletonGate(
    loading: Boolean,
    hasData: Boolean,
    minVisibleMs: Long = 280,
    skeleton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    var showSkeleton by remember { mutableStateOf(loading && !hasData) }
    var skeletonSince by remember { mutableStateOf(0L) }

    LaunchedEffect(loading, hasData) {
        if (loading && !hasData) {
            showSkeleton = true
            skeletonSince = System.currentTimeMillis()
        } else if (showSkeleton) {
            val elapsed = System.currentTimeMillis() - skeletonSince
            val remain = minVisibleMs - elapsed
            if (remain > 0) delay(remain)
            showSkeleton = false
        }
    }

    when {
        showSkeleton -> skeleton()
        hasData || !loading -> content()
        else -> { /* courte attente initiale : rien */ }
    }
}
