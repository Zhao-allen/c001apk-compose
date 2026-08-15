package com.example.c001apk.compose.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.c001apk.compose.logic.model.HapticStrength

fun Context.performConfiguredHapticFeedback(
    fallback: (() -> Unit)? = null,
    enabled: Boolean = CookieUtil.hapticFeedback,
    strength: HapticStrength = CookieUtil.hapticStrength,
    customHaptics: Boolean = CookieUtil.customHaptics,
    reverseEffects: Boolean = CookieUtil.reverseHapticEffects,
) {
    if (!enabled) return

    if (!customHaptics) {
        fallback?.invoke()
        return
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        fallback?.invoke()
        return
    }

    val vibrator = currentVibrator()
    if (vibrator?.hasVibrator() != true) {
        fallback?.invoke()
        return
    }

    val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        VibrationEffect.createPredefined(strength.predefinedEffect(reverseEffects))
    } else {
        VibrationEffect.createOneShot(strength.durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
    }
    vibrator.vibrate(effect)
}

private fun Context.currentVibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
