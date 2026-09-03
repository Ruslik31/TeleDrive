package com.drdisagree.teledrive.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

interface RootHandler {
    suspend fun requestRoot(): Boolean
    suspend fun grantAllPermissionsViaRoot(): Boolean
}

val LocalRootHandler = staticCompositionLocalOf<RootHandler> {
    object : RootHandler {
        override suspend fun requestRoot(): Boolean = false
        override suspend fun grantAllPermissionsViaRoot(): Boolean = false
    }
}
