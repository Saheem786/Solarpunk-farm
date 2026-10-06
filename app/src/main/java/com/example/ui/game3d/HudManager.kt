package com.example.ui.game3d

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State-based HUD Manager that automatically governs visibility of non-essential
 * UI elements (such as animal status cards, crop interaction prompts, navigation docks,
 * and immersive full-screen 3D mode).
 */
class HudManager {
    private val _isNavMenuVisible = MutableStateFlow(false)
    val isNavMenuVisible: StateFlow<Boolean> = _isNavMenuVisible.asStateFlow()

    private val _isContextualActive = MutableStateFlow(false)
    val isContextualActive: StateFlow<Boolean> = _isContextualActive.asStateFlow()

    private val _isImmersiveMode = MutableStateFlow(false)
    val isImmersiveMode: StateFlow<Boolean> = _isImmersiveMode.asStateFlow()

    fun toggleNavMenu(): Boolean {
        _isNavMenuVisible.value = !_isNavMenuVisible.value
        return _isNavMenuVisible.value
    }

    fun hideNavMenu() {
        _isNavMenuVisible.value = false
    }

    fun updateContextualState(hasTarget: Boolean) {
        _isContextualActive.value = hasTarget
    }

    fun toggleImmersiveMode(): Boolean {
        _isImmersiveMode.value = !_isImmersiveMode.value
        return _isImmersiveMode.value
    }

    fun setImmersiveMode(enabled: Boolean) {
        _isImmersiveMode.value = enabled
    }
}
