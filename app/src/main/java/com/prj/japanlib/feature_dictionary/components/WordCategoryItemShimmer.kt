package com.prj.japanlib.feature_dictionary.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.prj.japanlib.common.components.shimmer

/**
 * A composable that displays a placeholder UI for a list item with a shimmering effect.
 *
 * This is used to indicate a loading state while the actual list item content is being fetched.
 */
@Composable
fun WordCategoryItemShimmer(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2434)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- Shimmer for ProgressCircle (just a gray circle placeholder) ---
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .shimmer()
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title shimmer (topic name)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)   // shorter line
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle shimmer (word count)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)   // even shorter line
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}
