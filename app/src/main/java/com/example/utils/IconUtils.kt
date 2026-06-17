package com.example.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * An authentic Squircle Shape matching the Xiaomi HyperOS / MIUI superellipse aesthetics.
 * Uses cubic bezier curves to smoothly transition from straight edges to rounded corners.
 */
class SquircleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            // Approximate Xiaomi Squircle superellipse (n = 3)
            val r = w * 0.28f // 28% corner radius factor
            reset()
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            cubicTo(w - r * 0.45f, 0f, w, r * 0.45f, w, r)
            lineTo(w, h - r)
            cubicTo(w, h - r * 0.45f, w - r * 0.45f, h, w - r, h)
            lineTo(r, h)
            cubicTo(r * 0.45f, h, 0f, h - r * 0.45f, 0f, h - r)
            lineTo(0f, r)
            cubicTo(0f, r * 0.45f, r * 0.45f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

object IconUtils {
    /**
     * Safely load the app icon from PackageManager, or return null if it fails.
     */
    fun getAppIcon(context: Context, packageName: String, className: String): Drawable? {
        val pm = context.packageManager
        return try {
            // Try to find the specific activity icon first
            val componentName = android.content.ComponentName(packageName, className)
            pm.getActivityIcon(componentName)
        } catch (e: Exception) {
            try {
                // Return general application icon as fallback
                pm.getApplicationIcon(packageName)
            } catch (ex: Exception) {
                null
            }
        }
    }
}
