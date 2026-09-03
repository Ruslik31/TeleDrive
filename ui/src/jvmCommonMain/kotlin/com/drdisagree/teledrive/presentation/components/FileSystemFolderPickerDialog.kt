package com.drdisagree.teledrive.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.drdisagree.teledrive.resources.Res
import com.drdisagree.teledrive.resources.common_cancel
import com.drdisagree.teledrive.resources.common_create
import com.drdisagree.teledrive.resources.common_folder_name
import com.drdisagree.teledrive.resources.common_no_subfolders
import com.drdisagree.teledrive.resources.common_one_level
import com.drdisagree.teledrive.resources.files_new_folder
import com.drdisagree.teledrive.resources.picker_internal_storage
import com.drdisagree.teledrive.resources.picker_select_this_folder
import com.drdisagree.teledrive.resources.picker_system_root
import com.drdisagree.teledrive.resources.picker_title_select_folder
import com.drdisagree.teledrive.resources.picker_use_saf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

data class LocalFolderItem(
    val name: String,
    val path: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileSystemFolderPickerDialog(
    initialPath: String = "/storage/emulated/0",
    listSubfolders: suspend (String) -> List<LocalFolderItem>,
    createSubfolder: (suspend (parentPath: String, name: String) -> Boolean)? = null,
    onUseSaf: (() -> Unit)? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    var subfolders by remember { mutableStateOf<List<LocalFolderItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var naming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadPath(path: String) {
        currentPath = path
        loading = true
        scope.launch {
            subfolders = listSubfolders(path)
            loading = false
        }
    }

    LaunchedEffect(currentPath, listSubfolders) {
        subfolders = listSubfolders(currentPath)
        loading = false
    }

    val parentPath = remember(currentPath) {
        val trimmed = currentPath.trimEnd('/')
        if (trimmed.isEmpty()) null
        else {
            val idx = trimmed.lastIndexOf('/')
            if (idx <= 0) "/" else trimmed.substring(0, idx)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (parentPath != null && currentPath != "/") {
                    IconButton(onClick = { loadPath(parentPath) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_one_level)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    stringResource(Res.string.picker_title_select_folder),
                    modifier = Modifier.weight(1f)
                )
                if (createSubfolder != null) {
                    IconButton(onClick = { naming = true }) {
                        Icon(
                            Icons.Filled.CreateNewFolder,
                            contentDescription = stringResource(Res.string.files_new_folder)
                        )
                    }
                }
            }
        },
        text = {
            Column {
                // Quick jumps bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentPath == "/",
                        onClick = { loadPath("/") },
                        label = { Text(stringResource(Res.string.picker_system_root)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = currentPath.startsWith("/storage/emulated/0"),
                        onClick = { loadPath("/storage/emulated/0") },
                        label = { Text(stringResource(Res.string.picker_internal_storage)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = currentPath == "/data",
                        onClick = { loadPath("/data") },
                        label = { Text("/data") },
                        leadingIcon = {
                            Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = currentPath == "/storage/emulated/0/Download",
                        onClick = { loadPath("/storage/emulated/0/Download") },
                        label = { Text("Download") },
                        leadingIcon = {
                            Icon(Icons.Filled.SystemUpdateAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                // Current path indicator
                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (subfolders.isEmpty() && !loading) {
                    Text(
                        text = stringResource(Res.string.common_no_subfolders),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                        items(subfolders, key = { it.path }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { loadPath(item.path) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (onUseSaf != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = onUseSaf,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(Res.string.picker_use_saf))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentPath) },
                shapes = ButtonDefaults.shapes()
            ) { Text(stringResource(Res.string.picker_select_this_folder)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) { Text(stringResource(Res.string.common_cancel)) }
        }
    )

    if (naming && createSubfolder != null) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { naming = false },
            title = { Text(stringResource(Res.string.files_new_folder)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.common_folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            createSubfolder(currentPath, name.trim())
                            subfolders = listSubfolders(currentPath)
                            naming = false
                        }
                    },
                    enabled = name.isNotBlank(),
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(Res.string.common_create)) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { naming = false },
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(Res.string.common_cancel)) }
            }
        )
    }
}
