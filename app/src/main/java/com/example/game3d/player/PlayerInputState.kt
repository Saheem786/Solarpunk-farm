package com.example.game3d.player

data class PlayerInputState(
    val moveX: Float = 0.0f, // -1.0 to 1.0
    val moveZ: Float = 0.0f, // -1.0 to 1.0
    val isSprinting: Boolean = false,
    val interactPressed: Boolean = false
)
