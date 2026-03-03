package com.prj.japanlib.feature_dictionary

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.R
import com.prj.japanlib.common.components.CustomToast
import com.prj.japanlib.common.components.SingleActionDialog
import com.prj.japanlib.common.components.rememberToastState
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.common.utils.rememberImageCropperLauncher
import com.prj.japanlib.feature_dictionary.components.KanjiDrawingCanvas
import com.prj.japanlib.feature_dictionary.components.SpeechRecognitionOverlay
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.SearchScreenViewModel
import com.prj.japanlib.ui.theme.JapanlibTheme
import com.prj.japanlib.ui.theme.primaryContainerLight
import com.prj.japanlib.uistate.DictionaryUiState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import timber.log.Timber
import java.io.IOException


/**
 * A stateful composable that holds the logic and state for the search screen.
 * It integrates with [SearchScreenViewModel] to handle business logic and passes state down
 * to the stateless [SearchScreenContent] composable.
 *
 * @param onDetectedKanjiClick Callback invoked when a user clicks on a suggested Kanji character.
 * @param onSearchClick Callback invoked when the user initiates a search from the keyboard.
 */
@Composable
fun SearchScreen(
    onDetectedKanjiClick: (String) -> Unit,
    onSearchClick: (KeyboardActionScope.(String) -> Unit)
) {
    val searchScreenViewModel: SearchScreenViewModel = hiltViewModel();
    var searchQuery by remember { mutableStateOf("") }
    val resultKanjiState by searchScreenViewModel.resultKanjiUiStateFlow.collectAsStateWithLifecycle()
    val imageTextUiState by searchScreenViewModel.detectedTextUiStateFlow.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val mDebouncer = remember { ClickEventDebouncer() }
    SearchScreenContent(
        searchQuery = searchQuery,
        resultsDetected = resultKanjiState,
        imageTextUiState = imageTextUiState,
        onDetectedKanjiClick = {
            mDebouncer.processClick { onDetectedKanjiClick(it) }
        },
        onSearch = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onSearchClick(it)
                   },
        onSearchChange = {
            searchQuery = it
            searchScreenViewModel.clearDetectedText()
                         },
        onSaveBitmap = searchScreenViewModel::classifySearch,
        onImageCaptured = searchScreenViewModel::extractText
    )
}


/**
 * A stateless composable that displays the UI for the search screen.
 * It includes a search bar, input method buttons (voice, image), a Kanji drawing canvas,
 * and a list of suggestions.
 *
 * @param searchQuery The current text in the search bar.
 * @param resultsDetected The UI state for Kanji suggestions from the drawing canvas.
 * @param imageTextUiState The UI state for text detected from an image.
 * @param onDetectedKanjiClick Callback for when a suggested Kanji is clicked.
 * @param onSearch Callback for when a search is triggered.
 * @param onSearchChange Callback for when the search query text changes.
 * @param onSaveBitmap Callback to process the bitmap from the drawing canvas.
 * @param onImageCaptured Callback to process the URI of a captured/cropped image.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun SearchScreenContent(
    searchQuery: String,
    resultsDetected: DictionaryUiState<List<String>>,
    imageTextUiState: DictionaryUiState<String>,
    onDetectedKanjiClick: (String) -> Unit,
    onSearch: (KeyboardActionScope.(String) -> Unit),
    onSearchChange: (String) -> Unit,
    onSaveBitmap: (Bitmap) -> Unit,
    onImageCaptured: (String) -> Unit
) {
    // Local state for the content composable
    var resultList by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showSpeechOverlay by remember { mutableStateOf(false) }
    var detectedText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showDialog by remember { mutableStateOf(false) }
    // Ensure 1 toast across screen
    val toastState = rememberToastState()
    var noDetectedTextError = stringResource(R.string.no_detected_text_image)
    val hazeState = remember {  HazeState() }
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showSpeechOverlay = true
        } else {
            showDialog = true
        }
    }

    // Request permisson
    fun requestSpeechRecognition() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showSpeechOverlay = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    // Handle the state for Kanji suggestions from drawing.
    when (resultsDetected) {
        is DictionaryUiState.Success -> {
            isLoading = false
            resultList = resultsDetected.data
        }

        DictionaryUiState.Empty -> {}
        is DictionaryUiState.Error -> {}
        DictionaryUiState.Loading -> {
            isLoading = true
        }
    }

    // Handle the state for text detected from images.
    LaunchedEffect(imageTextUiState) {
        when (imageTextUiState) {
            is DictionaryUiState.Success -> {
                onSearchChange(imageTextUiState.data)
            }
            is DictionaryUiState.Error -> {
                toastState.show(noDetectedTextError)
            }
            else -> {
                // No action needed for Empty or Loading in this context
            }
        }
    }

    if (showDialog) {
        SingleActionDialog(
            showDialog = showDialog,
            title = stringResource(R.string.permission_dialog_title),
            message = stringResource(R.string.micro_permission_dialog_message),
            onDismiss = { showDialog = false }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(5.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            }
    ) {
        // Search Bar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(searchQuery)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_hint),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryContainerLight,
                    unfocusedBorderColor = primaryContainerLight.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    focusedLeadingIconColor = primaryContainerLight,
                    unfocusedLeadingIconColor = primaryContainerLight.copy(alpha = 0.5f),
                    focusedPlaceholderColor = primaryContainerLight,
                    unfocusedPlaceholderColor = primaryContainerLight.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voice and Draw Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center, // Center the group of items
            verticalAlignment = Alignment.CenterVertically
            ) {
            // Voice Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = { requestSpeechRecognition() },
                    modifier = Modifier
                        .size(width = 50.dp, height = 60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mic_ic),
                        contentDescription = "Voice",
                        tint = primaryContainerLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.voice),
                    color = primaryContainerLight,
                    fontSize = 12.sp
                )
            }

            // Image processing
            val cropImage = rememberImageCropperLauncher(
                onCropSuccess = { uri ->
                    try {
                        Timber.d("Detected Text: ${uri.toString()}")
                        onImageCaptured(uri.toString())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                },
                onCropError = { error ->
                    Timber.i("Image crop error: $error")
//                    toastState.show(noDetectedTextError)
                }
            )

            Spacer(modifier = Modifier.width(60.dp))

            // Image Search Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = {
                        cropImage(null)
                    },
                    modifier = Modifier
                        .size(width = 50.dp, height = 60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.camera_ic),
                        contentDescription = "Draw",
                        tint = primaryContainerLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.camera),
                    color = primaryContainerLight,
                    fontSize = 12.sp
                )
            }
        }

        // Draw Kanji Area
        KanjiDrawingCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onSaveBitmap = onSaveBitmap,
            isLoading = isLoading
        )

        // Suggestions Section
        Text(
            stringResource(R.string.suggestions),
            color = primaryContainerLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Suggestion Kanji Characters
        if (resultList.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth()
                ) {
                items(resultList) { kanji ->
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        onClick = { onDetectedKanjiClick(kanji) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                kanji,
                                color = primaryContainerLight,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.kanji_suggestion),
                    color = primaryContainerLight,
                    fontSize = 12.sp
                )
            }
        }
    }
    if (showSpeechOverlay) {
        noDetectedTextError = stringResource(R.string.no_detected_text)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeChild(
                    state = hazeState,
                    style = HazeMaterials.ultraThin()
                )
                .background(Color.White.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            SpeechRecognitionOverlay(
                onDismiss = { showSpeechOverlay = false },
                onTextRecognized = onSearchChange,
                onError = { toastState.show(noDetectedTextError) }
            )
        }

    }
    CustomToast(
        toastState = toastState,
        alignment = Alignment.TopCenter
    )

}

@Preview
@Composable
fun KanjiDrawScreenPreview() {
    JapanlibTheme() {
        SearchScreenContent(
            searchQuery = "",
            resultsDetected = DictionaryUiState.Empty,
            onDetectedKanjiClick = {},
            onSearch = {},
            onSearchChange = {},
            imageTextUiState = DictionaryUiState.Empty,
            onSaveBitmap = {}
        ) { }
    }
}


