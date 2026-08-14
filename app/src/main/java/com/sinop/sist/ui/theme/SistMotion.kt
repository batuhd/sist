package com.sinop.sist.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

/**
 * Motion token'ları — tüm animasyonlar buradan referans alır.
 */
object SistMotion {
    const val quick = 150
    const val standard = 250
    const val emphasized = 350
    const val page = 300

    val standardEasing: Easing = FastOutSlowInEasing

    // M3 "emphasized decelerate" eğrisi
    val emphasizedEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val pressEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
