package com.opencloner.app.data.model

sealed class ProfileState {
    object Unprovisioned : ProfileState()
    object Provisioning : ProfileState()
    data class Active(val profileId: Int, val clonedCount: Int, val isPaused: Boolean) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
