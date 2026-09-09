package com.example.game.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

object LogoLoader {
    /**
     * Loads the official logo from assets/logo/
     * Looks for logo.png, logo.jpg, or logo.webp.
     * Returns null if not found.
     */
    fun loadLogoBitmap(context: Context): ImageBitmap? {
        val assetManager = context.assets
        val potentialNames = listOf("logo/logo.png", "logo/logo.jpg", "logo/logo.webp")
        for (name in potentialNames) {
            try {
                assetManager.open(name).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        return bitmap.asImageBitmap()
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }
}

@Composable
fun rememberGameLogo(): ImageBitmap? {
    val context = LocalContext.current
    return remember(context) {
        LogoLoader.loadLogoBitmap(context)
    }
}
