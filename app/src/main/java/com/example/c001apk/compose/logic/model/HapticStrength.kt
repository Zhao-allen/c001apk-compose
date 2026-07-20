package com.example.c001apk.compose.logic.model

enum class HapticStrength(
    val protoValue: Int,
    val label: String,
    val durationMs: Long,
    val predefinedEffect: Int,
) {
    Light(protoValue = 1, label = "轻", durationMs = 6L, predefinedEffect = 2),
    Medium(protoValue = 0, label = "标准", durationMs = 10L, predefinedEffect = 0),
    Strong(protoValue = 2, label = "强", durationMs = 16L, predefinedEffect = 5);

    companion object {
        val options = listOf(Light, Medium, Strong)

        fun fromProtoValue(value: Int): HapticStrength =
            entries.firstOrNull { it.protoValue == value } ?: Medium
    }
}
