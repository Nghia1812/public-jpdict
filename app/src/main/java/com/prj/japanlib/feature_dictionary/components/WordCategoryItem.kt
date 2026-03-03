package com.prj.japanlib.feature_dictionary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.japanlib.R

@Composable
fun WordCategoryItem(
    modifier: Modifier = Modifier,
    topicList: WordList,
    onClick: (String, String) -> Unit
) {
    val progress = (topicList.count.toFloat()/ topicList.count).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2434)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(topicList.name, topicList.listId) },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressCircle(
                progress = progress,
                percentage = percentage
            )
            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(topicList.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.word_count, topicList.count), color = Color.White, fontSize = 12.sp)
            }

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Arrow", tint = Color.Gray)
        }
    }
}
