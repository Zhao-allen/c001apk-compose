package com.example.c001apk.compose.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.performConfiguredHapticFeedback

@Composable
fun rememberHapticClick(
    type: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    strength: HapticStrength? = null,
    onClick: () -> Unit,
): () -> Unit {
    val prefs = LocalUserPreferences.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val hapticStrength = strength ?: prefs.hapticStrength
    return remember(
        context,
        prefs.hapticFeedback,
        prefs.customHaptics,
        prefs.reverseHapticEffects,
        hapticStrength,
        hapticFeedback,
        type,
        onClick,
    ) {
        {
            if (prefs.hapticFeedback) {
                context.performConfiguredHapticFeedback(
                    fallback = { hapticFeedback.performHapticFeedback(type) },
                    enabled = prefs.hapticFeedback,
                    strength = hapticStrength,
                    customHaptics = prefs.customHaptics,
                    reverseEffects = prefs.reverseHapticEffects,
                )
            }
            onClick()
        }
    }
}

@Composable
fun MoreMenuButton(onClick: () -> Unit) {
    IconButton(onClick = rememberHapticClick(onClick = onClick)) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = Icons.Default.MoreVert.name
        )
    }
}
