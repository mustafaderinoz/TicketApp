package com.mustafaderinoz.ticketapp.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import io.github.g0dkar.qrcode.QRCode


@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(key1 = content) { generateQrBitmap(content) }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Bilet QR Kodu",
            modifier = modifier,
            filterQuality = FilterQuality.None
        )
    }
}

private fun generateQrBitmap(content: String): Bitmap? = runCatching {
    val pngBytes = QRCode(content)
        .render()
        .getBytes()
    BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)
}.getOrNull()