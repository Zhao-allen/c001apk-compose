package com.example.c001apk.compose.logic.model

enum class HapticStrength(
    val protoValue: Int,
    val label: String,
    val durationMs: Long,
    val standardEffect: Int,
    val compatibleEffect: Int,
) {
    Light(protoValue = 1, label = "轻", durationMs = 6L, standardEffect = 2, compatibleEffect = 5),
    Medium(protoValue = 0, label = "标准", durationMs = 10L, standardEffect = 0, compatibleEffect = 0),
    Strong(protoValue = 2, label = "强", durationMs = 16L, standardEffect = 5, compatibleEffect = 2);

    fun predefinedEffect(compatibilityMode: Boolean): Int =
        if (compatibilityMode) compatibleEffect else standardEffect

    companion object {
        val options = listOf(Light, Medium, Strong)

        fun fromProtoValue(value: Int): HapticStrength =
            entries.firstOrNull { it.protoValue == value } ?: Medium
    }
}
