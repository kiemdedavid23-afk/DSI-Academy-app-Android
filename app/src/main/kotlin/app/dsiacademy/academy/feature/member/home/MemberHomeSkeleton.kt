package app.dsiacademy.academy.feature.member.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MemberHomeSkeleton() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(RadiusLg))
                .background(Color(0xFFE2E8F0)),
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(RadiusLg))
                .background(Color(0xFFE2E8F0)),
        )
        Spacer(Modifier.height(16.dp))
        repeat(3) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(RadiusLg))
                    .background(Color(0xFFE2E8F0)),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview(name = "Skeleton — clair", showBackground = true)
@Composable
private fun MemberHomeSkeletonPreview() {
    MemberHomeSkeleton()
}
