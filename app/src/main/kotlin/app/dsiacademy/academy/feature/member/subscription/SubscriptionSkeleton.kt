package app.dsiacademy.academy.feature.member.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dsiacademy.academy.core.designsystem.components.SkeletonBox

@Composable
fun SubscriptionSkeleton() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // En-tête : cercle + 2 lignes
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(width = 48.dp, height = 48.dp, circle = true)
            Spacer(Modifier.padding(6.dp))
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                SkeletonBox(width = 180.dp, height = 20.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonBox(height = 12.dp, modifier = Modifier.fillMaxWidth(0.8f))
            }
        }
        Spacer(Modifier.height(16.dp))
        // Héros sombre
        SkeletonBox(height = 110.dp, onDark = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        // 3 chiffres
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(height = 56.dp, modifier = Modifier.weight(1f))
            SkeletonBox(height = 56.dp, modifier = Modifier.weight(1f))
            SkeletonBox(height = 56.dp, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        // Action
        SkeletonBox(height = 88.dp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        // Historique
        SkeletonBox(height = 120.dp, modifier = Modifier.fillMaxWidth())
    }
}
