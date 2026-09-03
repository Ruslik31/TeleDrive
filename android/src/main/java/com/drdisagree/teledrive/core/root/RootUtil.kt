package com.drdisagree.teledrive.core.root

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object RootUtil {

    fun isRootGranted(): Boolean {
        return runCatching {
            Shell.isAppGrantedRoot() == true
        }.getOrDefault(false)
    }

    suspend fun requestRoot(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val shell = Shell.getShell()
            shell.isRoot
        }.getOrDefault(false)
    }

    suspend fun grantPermissionsViaRoot(context: Context): Boolean = withContext(Dispatchers.IO) {
        val packageName = context.packageName
        val commands = listOf(
            "pm grant $packageName android.permission.READ_EXTERNAL_STORAGE",
            "pm grant $packageName android.permission.WRITE_EXTERNAL_STORAGE",
            "pm grant $packageName android.permission.READ_MEDIA_IMAGES",
            "pm grant $packageName android.permission.READ_MEDIA_VIDEO",
            "pm grant $packageName android.permission.POST_NOTIFICATIONS",
            "appops set $packageName MANAGE_EXTERNAL_STORAGE allow",
            "appops set $packageName READ_EXTERNAL_STORAGE allow",
            "appops set $packageName WRITE_EXTERNAL_STORAGE allow"
        )
        runCatching {
            val result = Shell.cmd(*commands.toTypedArray()).exec()
            result.isSuccess
        }.getOrDefault(false)
    }

    suspend fun listDirectories(path: String): List<File> = withContext(Dispatchers.IO) {
        val dir = File(path)
        val javaFiles = runCatching { dir.listFiles() }.getOrNull()
        if (!javaFiles.isNullOrEmpty()) {
            val dirs = javaFiles.filter { it.isDirectory }.sortedBy { it.name.lowercase() }
            if (dirs.isNotEmpty()) {
                return@withContext dirs
            }
        }

        if (isRootGranted()) {
            val sanitized = if (path == "/" || path.isBlank()) "" else path.trimEnd('/')
            val result = Shell.cmd("ls -1A -F \"$sanitized/\"").exec()
            val outputList: List<String> = result.out
            val subDirs = mutableListOf<File>()
            for (line in outputList) {
                val trimmed = line.trim()
                if (trimmed.endsWith("/") || trimmed.endsWith("/@")) {
                    val folderName = trimmed.removeSuffix("/@").removeSuffix("/").trim()
                    if (folderName.isNotBlank() && folderName != "." && folderName != "..") {
                        subDirs.add(File(dir, folderName))
                    }
                }
            }
            if (subDirs.isNotEmpty()) {
                return@withContext subDirs.distinctBy { it.absolutePath }.sortedBy { it.name.lowercase() }
            }
        }

        return@withContext javaFiles?.filter { it.isDirectory } ?: emptyList()
    }

    suspend fun listFilesRecursive(path: String): List<File> = withContext(Dispatchers.IO) {
        val root = File(path)
        val resultList = mutableListOf<File>()

        fun walk(current: File) {
            val children = current.listFiles()
            if (children != null) {
                for (child in children) {
                    if (child.isFile) {
                        resultList.add(child)
                    } else if (child.isDirectory) {
                        walk(child)
                    }
                }
            } else if (isRootGranted()) {
                val lines: List<String> = Shell.cmd("find \"${current.absolutePath}\" -maxdepth 1 -mindepth 1").exec().out
                for (line in lines) {
                    val f = File(line)
                    if (f.isFile) {
                        resultList.add(f)
                    } else if (f.isDirectory && f != current) {
                        walk(f)
                    }
                }
            }
        }

        walk(root)
        resultList
    }
}
