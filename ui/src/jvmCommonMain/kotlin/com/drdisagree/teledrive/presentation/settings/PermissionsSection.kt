package com.drdisagree.teledrive.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.drdisagree.teledrive.core.permissions.AppPermission
import com.drdisagree.teledrive.core.permissions.PermissionChecker
import com.drdisagree.teledrive.presentation.platform.LocalPermissionRequester
import com.drdisagree.teledrive.presentation.platform.LocalSystemScreens
import com.drdisagree.teledrive.resources.Res
import com.drdisagree.teledrive.resources.permission_not_allowed
import com.drdisagree.teledrive.resources.permissions_allowed
import com.drdisagree.teledrive.resources.permissions_grant_via_root
import com.drdisagree.teledrive.resources.permissions_grant_via_root_summary
import com.drdisagree.teledrive.resources.permissions_not_allowed
import com.drdisagree.teledrive.resources.permissions_not_allowed_optional
import kotlinx.coroutines.launch

/**
 * Lists every permission with its current state. Tapping a denied entry asks
 * again; once the system stops showing the dialog it falls back to the app's
 * settings page, which is the only way back from a permanent denial.
 */
@Composable
fun PermissionsSection(
    permissionChecker: PermissionChecker,
    onRequestRoot: (suspend () -> Boolean)? = null,
    onGrantAllViaRoot: (suspend () -> Boolean)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var statuses by remember { mutableStateOf(permissionChecker.statuses()) }
    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                statuses = permissionChecker.statuses()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionRequester = LocalPermissionRequester.current
    val systemScreens = LocalSystemScreens.current
    val rootGranted = statuses[AppPermission.ROOT_ACCESS] == true

    SettingsGroup {
        AppPermission.entries.forEach { permission ->
            val granted = statuses[permission] == true
            add(visible = permissionChecker.isRequestable(permission)) {
                PermissionRow(
                    permission = permission,
                    granted = granted,
                    onClick = {
                        when {
                            permission.isRootAccess -> {
                                if (!granted && onRequestRoot != null) {
                                    scope.launch {
                                        onRequestRoot()
                                        statuses = permissionChecker.statuses()
                                    }
                                }
                            }
                            granted -> systemScreens.openAppSettings()
                            permission.isSpecialAccess -> systemScreens.openAllFilesAccess()
                            else -> permissionRequester.request(listOf(permission)) {
                                statuses = permissionChecker.statuses()
                            }
                        }
                    }
                )
            }
        }
    }

    if (rootGranted && onGrantAllViaRoot != null) {
        Spacer(Modifier.height(16.dp))
        SettingsGroup {
            add {
                SettingsClickRow(
                    title = stringResource(Res.string.permissions_grant_via_root),
                    subtitle = stringResource(Res.string.permissions_grant_via_root_summary),
                    icon = Icons.Filled.Security,
                    onClick = {
                        scope.launch {
                            onGrantAllViaRoot()
                            statuses = permissionChecker.statuses()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    permission: AppPermission,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(permission.titleRes), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (granted) {
                    stringResource(Res.string.permissions_allowed)
                } else if (permission.critical) {
                    stringResource(
                        Res.string.permission_not_allowed,
                        stringResource(permission.rationaleRes)
                    )
                } else {
                    stringResource(Res.string.permissions_not_allowed_optional)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (granted || !permission.critical) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Outlined.Cancel,
            contentDescription = if (granted) stringResource(Res.string.permissions_allowed) else stringResource(
                Res.string.permissions_not_allowed
            ),
            modifier = Modifier.size(22.dp),
            tint = when {
                granted -> MaterialTheme.colorScheme.primary
                permission.critical -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            }
        )
    }
}
