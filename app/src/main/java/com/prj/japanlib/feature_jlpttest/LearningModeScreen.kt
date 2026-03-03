package com.prj.japanlib.feature_jlpttest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.ClickEventDebouncer

@Composable
fun LearningModeScreen(
    onNavigateToFlashcard: () -> Unit,
    onNavigateToJLPT: () -> Unit
) {
    val mDebouncer = remember { ClickEventDebouncer() }
    LearningModeContent(
        onFlashcardClick = {
            mDebouncer.processClick { onNavigateToFlashcard() }
        },
        onJLPTClick = {
            mDebouncer.processClick { onNavigateToJLPT() }
        }
    )
}

@Composable
fun LearningModeContent(
    onFlashcardClick: () -> Unit,
    onJLPTClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(R.string.screen_mode_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Flashcard Learning Card
        LearningCard(
            icon = ImageVector.vectorResource(R.drawable.ic_flashcard),
            title = stringResource(R.string.flashcard_mode_title),
            description = stringResource(R.string.flashcard_mode_description),
            onClick = onFlashcardClick,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // JLPT Testing Card
        LearningCard(
            icon = ImageVector.vectorResource(R.drawable.ic_test),
            title = stringResource(R.string.test_mode_title),
            description = stringResource(R.string.test_mode_description),
            onClick = onJLPTClick
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
fun LearningCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = "icon flashcard"
            )

            // Title
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFFB0B8C1),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview
@Composable
fun screenPreview() {
    LearningModeContent(
        onFlashcardClick = { },
        onJLPTClick = { }
    )
}