package com.prj.japanlib.feature_dictionary.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.prj.japanlib.common.components.shimmer

/**
 * A composable that displays a placeholder UI for a theme card with a shimmering effect.
 *
 * This is used to indicate a loading state while the actual theme card content is being fetched.
 */
@Composable
fun ThemeCardShimmer() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121821)),
        modifier = Modifier
            .height(180.dp)
            .width(180.dp)
    ) {
        Column {
            // Shimmer image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer(16.dp)
            )

            Column(Modifier.padding(8.dp)) {
                // Title shimmer line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .shimmer(8.dp)
                )

                Spacer(Modifier.height(6.dp))

                // Subtitle shimmer line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .shimmer(8.dp)
                )
            }
        }
    }
}