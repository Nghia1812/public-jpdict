package com.prj.japanlib.common.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

/**
 * Composable utility function that provides a launcher for an image cropping activity.
 *
 * This function encapsulates the setup for `rememberLauncherForActivityResult` with `CropImageContract`,
 *
 * @param onCropSuccess A lambda function to be invoked when the image is successfully cropped. It receives the [Uri] of the cropped image.
 * @param onCropError A lambda function to be invoked when the cropping process fails. It receives the [Exception] that occurred. Defaults to an empty lambda.
 * @return A launcher function of type `(Uri?) -> Unit`. This function is called to start the image cropping activity.
 */
@Composable
fun rememberImageCropperLauncher(
    onCropSuccess: (Uri) -> Unit,
    onCropError: (Exception) -> Unit = {}
): (Uri?) -> Unit {
    val imageCropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            // If cropping is successful, invoke the success callback with the resulting URI.
            result.uriContent?.let { uri ->
                onCropSuccess(uri)
            }
        } else {
            // If there's an error, invoke the error callback.
            result.error?.let { error ->
                onCropError(error)
            }
        }
    }

    // Return the function that will be used to launch the cropper.
    return { uri ->
        val cropOptions = CropImageContractOptions(
            uri = null, // Uri as null for a new image from camera
            cropImageOptions = CropImageOptions(
                guidelines = CropImageView.Guidelines.ON,
                aspectRatioX = 1,
                aspectRatioY = 1,
                fixAspectRatio = true,
                cropShape = CropImageView.CropShape.RECTANGLE,
                autoZoomEnabled = true,
                maxZoom = 4,
                outputCompressFormat = Bitmap.CompressFormat.PNG
            )
        )
        // Launch the image cropping activity with the specified options.
        imageCropLauncher.launch(cropOptions)
    }
}