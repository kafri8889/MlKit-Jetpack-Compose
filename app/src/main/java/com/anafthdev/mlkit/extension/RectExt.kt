package com.anafthdev.mlkit.extension

import android.graphics.Rect

fun Rect.scale(factor: Float): Rect {
    val diffHorizontal = (right - left) * (factor - 1f)
    val diffVertical = (bottom - top) * (factor - 1f)

    top -= (diffVertical / 2f).toInt()
    bottom += (diffVertical / 2f).toInt()

    left -= (diffHorizontal / 2f).toInt()
    right += (diffHorizontal / 2f).toInt()

    return this
}
