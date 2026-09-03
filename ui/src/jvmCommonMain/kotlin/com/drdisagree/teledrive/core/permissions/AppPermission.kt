package com.drdisagree.teledrive.core.permissions

import com.drdisagree.teledrive.resources.Res
import com.drdisagree.teledrive.resources.permission_all_files_rationale
import com.drdisagree.teledrive.resources.permission_all_files_title
import com.drdisagree.teledrive.resources.permission_notifications_rationale
import com.drdisagree.teledrive.resources.permission_notifications_title
import com.drdisagree.teledrive.resources.permission_photos_rationale
import com.drdisagree.teledrive.resources.permission_photos_title
import com.drdisagree.teledrive.resources.permission_root_rationale
import com.drdisagree.teledrive.resources.permission_root_title
import com.drdisagree.teledrive.resources.permission_videos_rationale
import com.drdisagree.teledrive.resources.permission_videos_title
import org.jetbrains.compose.resources.StringResource

/**
 * Permissions the app can ask for, with the reason shown to the user.
 * [critical] marks the ones without which automatic backup cannot work.
 */
enum class AppPermission(
    val titleRes: StringResource,
    val rationaleRes: StringResource,
    val critical: Boolean
) {
    MEDIA_IMAGES(
        titleRes = Res.string.permission_photos_title,
        rationaleRes = Res.string.permission_photos_rationale,
        critical = true
    ),
    MEDIA_VIDEO(
        titleRes = Res.string.permission_videos_title,
        rationaleRes = Res.string.permission_videos_rationale,
        critical = true
    ),
    NOTIFICATIONS(
        titleRes = Res.string.permission_notifications_title,
        rationaleRes = Res.string.permission_notifications_rationale,
        critical = false
    ),
    ALL_FILES(
        titleRes = Res.string.permission_all_files_title,
        rationaleRes = Res.string.permission_all_files_rationale,
        critical = true
    ),
    ROOT_ACCESS(
        titleRes = Res.string.permission_root_title,
        rationaleRes = Res.string.permission_root_rationale,
        critical = false
    );

    /** Special access granted from a system settings screen, not a dialog. */
    val isSpecialAccess: Boolean get() = this == ALL_FILES

    val isRootAccess: Boolean get() = this == ROOT_ACCESS
}
