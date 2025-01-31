package com.freewillsolutions.scantext

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment

fun Fragment.runOnUiThread(f: () -> Unit) {
    activity?.runOnUiThread { f() }
}

fun bitmapToDrawable(resources: Resources, bitmap: Bitmap): Drawable {
    return BitmapDrawable(resources, bitmap)
}